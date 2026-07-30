package com.silberlima.segueme.relatorios.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.StatusRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

/**
 * Cobre a tela Relatórios de ponta a ponta no backend (RF-056, UC-22), incluindo a geração
 * assíncrona: o POST devolve na hora e o arquivo aparece pouco depois, gerado fora da request.
 */
class RelatoriosControllerIT extends AbstractIntegrationTest {

    private static final long ESPERA_MAXIMA_MS = 15_000;
    private static final long INTERVALO_MS = 100;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private RelatorioRepository relatorioRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListarSomenteOsTiposQueOPerfilPodeGerar() throws Exception {
        mockMvc.perform(get("/api/v1/relatorios/tipos").with(usuarioNacional("tipos-nacional")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo == 'PESSOAS')]").exists())
                .andExpect(jsonPath("$[?(@.tipo == 'ENCONTROS')]").exists())
                .andExpect(jsonPath("$[?(@.tipo == 'FINANCEIRO')]").doesNotExist());
    }

    @Test
    void shouldReturn409WhenPerfilNaoPodeGerarOTipo() throws Exception {
        mockMvc.perform(post("/api/v1/relatorios")
                        .with(usuarioNacional("nacional-financeiro"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"FINANCEIRO\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldAceitarOPedidoSemEsperarAGeracao() throws Exception {
        mockMvc.perform(post("/api/v1/relatorios")
                        .with(usuarioNacional("nacional-aceite"))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"PESSOAS\"}"))
                .andExpect(status().isAccepted())
                .andExpect(jsonPath("$.tipo").value("PESSOAS"))
                .andExpect(jsonPath("$.tipoTitulo").value("Pessoas"))
                .andExpect(jsonPath("$.status").value("PENDENTE"));
    }

    @Test
    void shouldGerarOArquivoDepoisDoCommitEDisponibilizarParaDownload() throws Exception {
        String solicitante = "nacional-download";
        UUID relatorioId = solicitar(solicitante, TipoRelatorio.PESSOAS);

        assertThat(esperarConclusao(relatorioId)).isEqualTo(StatusRelatorio.CONCLUIDO);

        mockMvc.perform(get("/api/v1/relatorios/{id}/conteudo", relatorioId).with(usuarioNacional(solicitante)))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("Nome;Paróquia")));
    }

    @Test
    void shouldReturn409WhenRelatorioAindaNaoFoiGerado() throws Exception {
        Relatorio pendente = relatorioRepository.save(
                new Relatorio(TipoRelatorio.PESSOAS, "nacional-pendente", new EscopoTerritorial.Nacional()));

        mockMvc.perform(get("/api/v1/relatorios/{id}/conteudo", pendente.getId())
                        .with(usuarioNacional("nacional-pendente")))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn404WhenRelatorioEDeOutroUsuario() throws Exception {
        Relatorio deOutro = relatorioRepository.save(
                new Relatorio(TipoRelatorio.PESSOAS, "dono-do-relatorio", new EscopoTerritorial.Nacional()));

        mockMvc.perform(get("/api/v1/relatorios/{id}/conteudo", deOutro.getId())
                        .with(usuarioNacional("intruso")))
                .andExpect(status().isNotFound());
    }

    @Test
    void shouldListarSomenteOsRelatoriosDoProprioSolicitante() throws Exception {
        relatorioRepository.save(
                new Relatorio(TipoRelatorio.PESSOAS, "lista-usuario-a", new EscopoTerritorial.Nacional()));
        relatorioRepository.save(
                new Relatorio(TipoRelatorio.ENCONTROS, "lista-usuario-b", new EscopoTerritorial.Nacional()));

        mockMvc.perform(get("/api/v1/relatorios").with(usuarioNacional("lista-usuario-a")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].tipo").value("PESSOAS"));
    }

    private UUID solicitar(String solicitante, TipoRelatorio tipo) throws Exception {
        String corpo = mockMvc.perform(post("/api/v1/relatorios")
                        .with(usuarioNacional(solicitante))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"tipo\":\"" + tipo.name() + "\"}"))
                .andExpect(status().isAccepted())
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(JsonPath.read(corpo, "$.id"));
    }

    /**
     * Espera o thread de geração terminar. O teste não pode assumir que o arquivo já existe assim
     * que o POST responde — é exatamente isso que a fase entrega: a request não espera pela geração.
     */
    private StatusRelatorio esperarConclusao(UUID relatorioId) throws InterruptedException {
        long limite = System.currentTimeMillis() + ESPERA_MAXIMA_MS;
        while (System.currentTimeMillis() < limite) {
            Optional<StatusRelatorio> status =
                    relatorioRepository.findByIdAndDeletedFalse(relatorioId).map(Relatorio::getStatus);
            if (status.isPresent() && status.get() != StatusRelatorio.PENDENTE) {
                return status.get();
            }
            Thread.sleep(INTERVALO_MS);
        }
        return StatusRelatorio.PENDENTE;
    }

    private RequestPostProcessor usuarioNacional(String subject) {
        return jwt().jwt(builder -> builder.subject(subject))
                .authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"));
    }
}
