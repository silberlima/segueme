package com.silberlima.segueme.financeiro.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.api.dto.RegistrarLancamentoRequest;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
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
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/lancamentos-financeiros} e {@code /api/v1/financeiro/resumo} (RF-047 a RF-049, RN-FINANCEIRO-01/02/04). */
class LancamentosFinanceirosControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    /** `data` do lançamento é um LocalDate: sem o módulo de datas o request nem chega a ser serializado. */
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
        mockMvc.perform(get("/api/v1/lancamentos-financeiros")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenRegistrandoSemRoleEquipeFinancas() throws Exception {
        Setor setor = criarSetor("Setor 403");
        Paroquia paroquia = criarParoquia("Paróquia 403", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/lancamentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarLancamentoRequest(
                                encontro.getId(), TipoLancamento.RECEITA, "Doações", new BigDecimal("100.00"), LocalDate.now())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectRegistroWithNonPositiveValor() throws Exception {
        Setor setor = criarSetor("Setor Validação");
        Paroquia paroquia = criarParoquia("Paróquia Validação", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/lancamentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarLancamentoRequest(
                                encontro.getId(), TipoLancamento.DESPESA, "Alimentação", BigDecimal.ZERO, LocalDate.now())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldRunFullExpenseChainAndComputeSaldo() throws Exception {
        Setor setor = criarSetor("Setor Fluxo Financeiro");
        Paroquia paroquia = criarParoquia("Paróquia Fluxo Financeiro", setor);
        Encontro encontro = criarEncontro(paroquia);

        // 1. Registra uma receita.
        mockMvc.perform(post("/api/v1/lancamentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarLancamentoRequest(
                                encontro.getId(), TipoLancamento.RECEITA, "Doações", new BigDecimal("300.00"), LocalDate.now())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("RECEITA"));

        // 2. Registra uma despesa (nasce pendente).
        String despesaJson = mockMvc.perform(post("/api/v1/lancamentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarLancamentoRequest(
                                encontro.getId(), TipoLancamento.DESPESA, "Alimentação", new BigDecimal("120.00"), LocalDate.now())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("PENDENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID despesaId = UUID.fromString(objectMapper.readTree(despesaJson).get("id").asText());

        // 3. Equipe Finanças aprova a despesa.
        mockMvc.perform(patch("/api/v1/lancamentos-financeiros/{id}/aprovar", despesaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("APROVADA"))
                .andExpect(jsonPath("$.aprovadoPor").exists());

        // 4. Paróquia paga a despesa aprovada.
        mockMvc.perform(patch("/api/v1/lancamentos-financeiros/{id}/pagar", despesaId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.situacao").value("PAGA"));

        // 5. Saldo do resumo reflete receita - despesa.
        mockMvc.perform(get("/api/v1/financeiro/resumo")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalReceitas").value(300.00))
                .andExpect(jsonPath("$.totalDespesas").value(120.00))
                .andExpect(jsonPath("$.saldo").value(180.00));
    }

    @Test
    void shouldListLancamentosScopedByParoquia() throws Exception {
        Setor setor = criarSetor("Setor Listagem Financeiro");
        Paroquia paroquia = criarParoquia("Paróquia Listagem Financeiro", setor);
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(post("/api/v1/lancamentos-financeiros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RegistrarLancamentoRequest(
                                encontro.getId(), TipoLancamento.RECEITA, "Doações", new BigDecimal("50.00"), LocalDate.now())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FINANCAS")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/lancamentos-financeiros")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
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
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
