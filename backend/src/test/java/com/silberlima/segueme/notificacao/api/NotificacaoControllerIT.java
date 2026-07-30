package com.silberlima.segueme.notificacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

class NotificacaoControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificacaoRepository notificacaoRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/notificacoes"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListOnlyNotificationsOfAuthenticatedUser() throws Exception {
        notificacaoRepository.save(
                new Notificacao("user-a", TipoNotificacao.INFORMATIVA, "Para A", "Mensagem A"));
        notificacaoRepository.save(
                new Notificacao("user-b", TipoNotificacao.INFORMATIVA, "Para B", "Mensagem B"));

        mockMvc.perform(get("/api/v1/notificacoes").with(jwt().jwt(builder -> builder.subject("user-a"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].titulo").value("Para A"));
    }

    @Test
    void shouldMarkAllUnreadNotificationsAsReadForAuthenticatedUser() throws Exception {
        Notificacao notificacao = notificacaoRepository.save(
                new Notificacao("user-c", TipoNotificacao.NEGATIVA, "Convite recusado", "Mensagem C"));

        mockMvc.perform(patch("/api/v1/notificacoes/marcar-lidas")
                        .with(jwt().jwt(builder -> builder.subject("user-c"))))
                .andExpect(status().isNoContent());

        assertThat(notificacaoRepository.findById(notificacao.getId()).orElseThrow().isLida()).isTrue();
    }

    @Test
    void shouldContarSomenteAsNaoLidasDoUsuarioAutenticado() throws Exception {
        Notificacao lida = notificacaoRepository.save(
                new Notificacao("user-contador", TipoNotificacao.INFORMATIVA, "Já lida", "Mensagem"));
        lida.marcarComoLida();
        notificacaoRepository.save(lida);
        notificacaoRepository.save(
                new Notificacao("user-contador", TipoNotificacao.POSITIVA, "Não lida", "Mensagem"));
        notificacaoRepository.save(
                new Notificacao("outro-usuario", TipoNotificacao.POSITIVA, "De outro", "Mensagem"));

        mockMvc.perform(get("/api/v1/notificacoes/nao-lidas")
                        .with(jwt().jwt(builder -> builder.subject("user-contador"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.total").value(1));
    }

    @Test
    void shouldDevolverOsTresTiposAtivosParaQuemNuncaConfigurou() throws Exception {
        mockMvc.perform(get("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("user-sem-preferencia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(3))
                .andExpect(jsonPath("$[?(@.ativo == false)]").doesNotExist());
    }

    @Test
    void shouldPersistirOsTogglesDeNotificacao() throws Exception {
        mockMvc.perform(put("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("user-preferencia")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"preferencias":[{"tipo":"NEGATIVA","ativo":false}]}"""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo == 'NEGATIVA')].ativo").value(false));

        mockMvc.perform(get("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("user-preferencia"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo == 'NEGATIVA')].ativo").value(false))
                .andExpect(jsonPath("$[?(@.tipo == 'POSITIVA')].ativo").value(true));
    }

    @Test
    void shouldNaoAlcancarAsPreferenciasDeOutroUsuario() throws Exception {
        mockMvc.perform(put("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("dono-da-preferencia")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"preferencias":[{"tipo":"INFORMATIVA","ativo":false}]}"""))
                .andExpect(status().isOk());

        // Pendência #34: não existe administração global — cada usuário só enxerga as suas.
        mockMvc.perform(get("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("outro-dono"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[?(@.tipo == 'INFORMATIVA')].ativo").value(true));
    }

    @Test
    void shouldReturn400WhenPreferenciasVemVazio() throws Exception {
        mockMvc.perform(put("/api/v1/notificacoes/preferencias")
                        .with(jwt().jwt(builder -> builder.subject("user-invalido")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"preferencias\":[]}"))
                .andExpect(status().isBadRequest());
    }
}
