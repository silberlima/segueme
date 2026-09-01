package com.silberlima.segueme.financeiro.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.api.dto.CriarCampanhaRequest;
import com.silberlima.segueme.financeiro.api.dto.RegistrarArrecadacaoRequest;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/campanhas} (RF-050, RN-FINANCEIRO-03). */
class CampanhasControllerIT extends AbstractIntegrationTest {

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

    @Autowired
    private EncontroRepository encontroRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/campanhas")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenCriandoSemRoleEquipeFinancas() throws Exception {
        Setor setor = criarSetor("Setor Campanha 403");
        Paroquia paroquia = criarParoquia("Paróquia Campanha 403", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/campanhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarCampanhaRequest(encontro.getId(), "Festa Junina", new BigDecimal("1000.00"))))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectCriacaoWithNonPositiveMeta() throws Exception {
        Setor setor = criarSetor("Setor Campanha Validação");
        Paroquia paroquia = criarParoquia("Paróquia Campanha Validação", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/campanhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new CriarCampanhaRequest(encontro.getId(), "Festa Junina", BigDecimal.ZERO)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRunFullCampanhaChain() throws Exception {
        Setor setor = criarSetor("Setor Fluxo Campanha");
        Paroquia paroquia = criarParoquia("Paróquia Fluxo Campanha", setor);
        Encontro encontro = criarEncontro(paroquia);

        String campanhaJson = mockMvc.perform(post("/api/v1/campanhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarCampanhaRequest(encontro.getId(), "Festa Junina", new BigDecimal("1000.00"))))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("ATIVA"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID campanhaId = UUID.fromString(objectMapper.readTree(campanhaJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/campanhas/{id}/arrecadacao", campanhaId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarArrecadacaoRequest(new BigDecimal("300.00"))))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valorArrecadado").value(300.00));

        mockMvc.perform(patch("/api/v1/campanhas/{id}/encerrar", campanhaId)
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ENCERRADA"));
    }

    @Test
    void shouldListCampanhasScopedByParoquia() throws Exception {
        Setor setor = criarSetor("Setor Listagem Campanha");
        Paroquia paroquia = criarParoquia("Paróquia Listagem Campanha", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/campanhas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarCampanhaRequest(encontro.getId(), "Rifa Solidária", new BigDecimal("500.00"))))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/campanhas")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
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

    private Encontro criarEncontro(Paroquia paroquia) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor " + UUID.randomUUID(), paroquia));
        return encontroRepository.save(new Encontro(
                1, "São José", diretorEspiritual, "Sítio Teste", LocalDate.now().plusDays(30), LocalDate.now().plusDays(32), 40, paroquia));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
