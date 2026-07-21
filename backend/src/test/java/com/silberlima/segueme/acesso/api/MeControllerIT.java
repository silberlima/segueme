package com.silberlima.segueme.acesso.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.silberlima.segueme.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

class MeControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturnAuthenticatedUserWithPerfisPermitidos() throws Exception {
        mockMvc.perform(get("/api/v1/me").with(jwt().jwt(builder -> builder
                        .subject("user-1")
                        .claim("email", "coordenador@segueme.org")
                        .claim("name", "Fulano de Tal"))
                .authorities(
                        new SimpleGrantedAuthority("ROLE_PAROQUIA"),
                        new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("user-1"))
                .andExpect(jsonPath("$.email").value("coordenador@segueme.org"))
                .andExpect(jsonPath("$.nome").value("Fulano de Tal"))
                .andExpect(jsonPath("$.perfis.length()").value(2))
                .andExpect(jsonPath("$.perfis[0]").value("PAROQUIA"))
                .andExpect(jsonPath("$.perfis[1]").value("EQUIPE_MONTAGEM"));
    }
}
