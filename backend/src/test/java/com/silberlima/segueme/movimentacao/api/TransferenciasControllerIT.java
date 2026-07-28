package com.silberlima.segueme.movimentacao.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.movimentacao.api.dto.RecusarTransferenciaRequest;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarTransferenciaRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/transferencias} (RF-040/041, RN-MOVIMENTO-01, RN-17). */
class TransferenciasControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private DioceseRepository dioceseRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private ParoquiaRepository paroquiaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/transferencias")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAprovandoSemRoleSetorial() throws Exception {
        Setor setor = criarSetor("Setor 403");
        Paroquia origem = criarParoquia("Origem 403", setor);
        Paroquia destino = criarParoquia("Destino 403", setor);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", origem));

        String transferenciaJson = mockMvc.perform(post("/api/v1/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarTransferenciaRequest(pessoa.getId(), destino.getId(), "Mudança de cidade")))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID transferenciaId = UUID.fromString(objectMapper.readTree(transferenciaJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/transferencias/{id}/aprovar", transferenciaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRunFullTransferChainAndChangePessoaParoquia() throws Exception {
        Setor setor = criarSetor("Setor Fluxo");
        Paroquia origem = criarParoquia("Origem Fluxo", setor);
        Paroquia destino = criarParoquia("Destino Fluxo", setor);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Migrante", origem));

        // 1. Solicita a transferência.
        String transferenciaJson = mockMvc.perform(post("/api/v1/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarTransferenciaRequest(pessoa.getId(), destino.getId(), "Mudança de cidade")))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID transferenciaId = UUID.fromString(objectMapper.readTree(transferenciaJson).get("id").asText());

        // 2. Setorial aprova.
        mockMvc.perform(patch("/api/v1/transferencias/{id}/aprovar", transferenciaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("APROVADA"));

        // 3. Pessoa passou a pertencer à paróquia de destino.
        Pessoa atualizada = pessoaRepository.findByIdAndDeletedFalse(pessoa.getId()).orElseThrow();
        org.assertj.core.api.Assertions.assertThat(atualizada.getParoquia().getId()).isEqualTo(destino.getId());
    }

    @Test
    void shouldRejectRefusalWithoutJustificativa() throws Exception {
        Setor setor = criarSetor("Setor Recusa");
        Paroquia origem = criarParoquia("Origem Recusa", setor);
        Paroquia destino = criarParoquia("Destino Recusa", setor);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", origem));

        String transferenciaJson = mockMvc.perform(post("/api/v1/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolicitarTransferenciaRequest(pessoa.getId(), destino.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID transferenciaId = UUID.fromString(objectMapper.readTree(transferenciaJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/transferencias/{id}/recusar", transferenciaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecusarTransferenciaRequest(" ")))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldListTransferenciasScopedBySetor() throws Exception {
        Setor setor = criarSetor("Setor Listagem");
        Paroquia origem = criarParoquia("Origem Listagem", setor);
        Paroquia destino = criarParoquia("Destino Listagem", setor);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", origem));

        mockMvc.perform(post("/api/v1/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolicitarTransferenciaRequest(pessoa.getId(), destino.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/transferencias")
                        .param("status", "PENDENTE")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private Setor criarSetor(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome + " " + UUID.randomUUID()));
        return setorRepository.save(new Setor("Setor " + nome + " " + UUID.randomUUID(), diocese));
    }

    private Paroquia criarParoquia(String nome, Setor setor) {
        return paroquiaRepository.save(new Paroquia(nome + " " + UUID.randomUUID(), setor));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
