package com.silberlima.segueme.posencontro.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.posencontro.api.dto.RegistrarPresencasRequest;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/encontros-pos/{id}/presencas} (RF-058, UC-20, RN-POS-01). */
class PresencasControllerIT extends AbstractIntegrationTest {

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
    private EncontroPosRepository encontroPosRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/encontros-pos/{id}/presencas", UUID.randomUUID()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListarPoolDeAcompanhadosSemMarcacaoAntesDaChamada() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa seguimista = criarPessoa("Seguimista", paroquia, SituacaoPessoa.SEGUIMISTA);
        criarPessoa("Pré-inscrito Fora do Pool", paroquia, SituacaoPessoa.PRE_INSCRITO);
        EncontroPos encontroPos = criarEncontroPos(paroquia);

        mockMvc.perform(get("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pessoaId").value(seguimista.getId().toString()))
                .andExpect(jsonPath("$[0].presente").doesNotExist());
    }

    @Test
    void shouldReturn403WhenRegistrandoSemPerfilAutorizado() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa pessoa = criarPessoa("Jovem", paroquia, SituacaoPessoa.SEGUIMISTA);
        EncontroPos encontroPos = criarEncontroPos(paroquia);

        mockMvc.perform(put("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaChamada(pessoa, true)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRegistrarChamadaEmLoteERemarcarSemDuplicar() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa pessoa = criarPessoa("Jovem Acompanhado", paroquia, SituacaoPessoa.SEGUIMISTA);
        EncontroPos encontroPos = criarEncontroPos(paroquia);

        mockMvc.perform(put("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaChamada(pessoa, true)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].presente").value(true));

        mockMvc.perform(put("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaChamada(pessoa, false)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].presente").value(false));
    }

    @Test
    void shouldRejeitarPessoaDeOutraParoquia() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa forasteiro = criarPessoa("Forasteiro", criarParoquia(), SituacaoPessoa.SEGUIMISTA);
        EncontroPos encontroPos = criarEncontroPos(paroquia);

        mockMvc.perform(put("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaChamada(forasteiro, true)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isConflict());
    }

    private RegistrarPresencasRequest umaChamada(Pessoa pessoa, boolean presente) {
        return new RegistrarPresencasRequest(
                List.of(new RegistrarPresencasRequest.PresencaItem(pessoa.getId(), presente)));
    }

    private Paroquia criarParoquia() {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Presença " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor Presença " + UUID.randomUUID(), diocese));
        return paroquiaRepository.save(new Paroquia("Paróquia Presença " + UUID.randomUUID(), setor));
    }

    private EncontroPos criarEncontroPos(Paroquia paroquia) {
        return encontroPosRepository.save(new EncontroPos(paroquia, LocalDate.now(), null));
    }

    private Pessoa criarPessoa(String nome, Paroquia paroquia, SituacaoPessoa situacao) {
        Pessoa pessoa = new Pessoa(
                nome + " " + UUID.randomUUID(),
                LocalDate.of(2005, 1, 1),
                "(11) 90000-0000",
                EstadoCivil.SOLTEIRO,
                paroquia,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        ReflectionTestUtils.setField(pessoa, "situacao", situacao);
        return pessoaRepository.save(pessoa);
    }
}
