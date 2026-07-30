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

/**
 * Cobre {@code /api/v1/pos-encontro/frequencia} e {@code /prioridades} (RF-059, RN-POS-02/03) de
 * ponta a ponta: chamada registrada, frequência apurada e ranking classificado pela régua default
 * (75% Alta / 40% Média).
 */
class AcompanhamentoControllerIT extends AbstractIntegrationTest {

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
        mockMvc.perform(get("/api/v1/pos-encontro/prioridades").param("paroquiaId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn404WhenParoquiaNotFound() throws Exception {
        Paroquia paroquia = criarParoquia();

        mockMvc.perform(get("/api/v1/pos-encontro/frequencia")
                        .param("paroquiaId", UUID.randomUUID().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldRanquearPrioridadePelaFrequenciaApurada() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa assiduo = criarPessoa("Assíduo", paroquia);
        Pessoa faltante = criarPessoa("Faltante", paroquia);

        EncontroPos primeiro = criarEncontroPos(paroquia, LocalDate.now().minusDays(14));
        EncontroPos segundo = criarEncontroPos(paroquia, LocalDate.now().minusDays(7));
        registrarChamada(primeiro, List.of(presenca(assiduo, true), presenca(faltante, false)));
        registrarChamada(segundo, List.of(presenca(assiduo, true), presenca(faltante, false)));

        mockMvc.perform(get("/api/v1/pos-encontro/prioridades")
                        .param("paroquiaId", paroquia.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].pessoaId").value(assiduo.getId().toString()))
                .andExpect(jsonPath("$[0].totalEncontros").value(2))
                .andExpect(jsonPath("$[0].presencas").value(2))
                .andExpect(jsonPath("$[0].percentual").value(100))
                .andExpect(jsonPath("$[0].faixaPrioridade").value("ALTA"))
                .andExpect(jsonPath("$[1].pessoaId").value(faltante.getId().toString()))
                .andExpect(jsonPath("$[1].percentual").value(0))
                .andExpect(jsonPath("$[1].faixaPrioridade").value("BAIXA"));
    }

    @Test
    void shouldRecortarFrequenciaPeloPeriodoInformado() throws Exception {
        Paroquia paroquia = criarParoquia();
        Pessoa pessoa = criarPessoa("Jovem", paroquia);

        EncontroPos antigo = criarEncontroPos(paroquia, LocalDate.now().minusMonths(6));
        EncontroPos recente = criarEncontroPos(paroquia, LocalDate.now().minusDays(3));
        registrarChamada(antigo, List.of(presenca(pessoa, false)));
        registrarChamada(recente, List.of(presenca(pessoa, true)));

        mockMvc.perform(get("/api/v1/pos-encontro/frequencia")
                        .param("paroquiaId", paroquia.getId().toString())
                        .param("de", LocalDate.now().minusDays(30).toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].totalEncontros").value(1))
                .andExpect(jsonPath("$[0].presencas").value(1))
                .andExpect(jsonPath("$[0].percentual").value(100));
    }

    private RegistrarPresencasRequest.PresencaItem presenca(Pessoa pessoa, boolean presente) {
        return new RegistrarPresencasRequest.PresencaItem(pessoa.getId(), presente);
    }

    private void registrarChamada(EncontroPos encontroPos, List<RegistrarPresencasRequest.PresencaItem> presencas)
            throws Exception {
        mockMvc.perform(put("/api/v1/encontros-pos/{id}/presencas", encontroPos.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarPresencasRequest(presencas)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", encontroPos.getParoquia().getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk());
    }

    private Paroquia criarParoquia() {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Acomp " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor Acomp " + UUID.randomUUID(), diocese));
        return paroquiaRepository.save(new Paroquia("Paróquia Acomp " + UUID.randomUUID(), setor));
    }

    private EncontroPos criarEncontroPos(Paroquia paroquia, LocalDate data) {
        return encontroPosRepository.save(new EncontroPos(paroquia, data, null));
    }

    private Pessoa criarPessoa(String nome, Paroquia paroquia) {
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
        ReflectionTestUtils.setField(pessoa, "situacao", SituacaoPessoa.SEGUIMISTA);
        return pessoaRepository.save(pessoa);
    }
}
