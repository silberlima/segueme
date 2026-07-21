package com.silberlima.segueme.notificacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.silberlima.segueme.notificacao.domain.Notificacao;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
}
