package com.silberlima.segueme.posencontro.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.posencontro.api.dto.AgendarEncontroPosRequest;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/encontros-pos} (RF-058, RN-POS-01). */
class EncontrosPosControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private DioceseRepository dioceseRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private ParoquiaRepository paroquiaRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/encontros-pos").param("paroquiaId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenAgendandoSemPerfilAutorizado() throws Exception {
        Paroquia paroquia = criarParoquia();

        mockMvc.perform(post("/api/v1/encontros-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AgendarEncontroPosRequest(paroquia.getId(), LocalDate.now(), null)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAgendarEncontroPosAndListarNaAgendaDaParoquia() throws Exception {
        Paroquia paroquia = criarParoquia();
        LocalDate data = LocalDate.now().minusDays(7);

        mockMvc.perform(post("/api/v1/encontros-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AgendarEncontroPosRequest(paroquia.getId(), data, "Perseverança")))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data").value(data.toString()))
                .andExpect(jsonPath("$.tema").value("Perseverança"))
                .andExpect(jsonPath("$.paroquiaNome").value(paroquia.getNome()));

        mockMvc.perform(get("/api/v1/encontros-pos")
                        .param("paroquiaId", paroquia.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_POS_ENCONTRO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].data").value(data.toString()));
    }

    @Test
    void shouldRejeitarSegundoEncontroPosNaMesmaData() throws Exception {
        Paroquia paroquia = criarParoquia();
        LocalDate data = LocalDate.now();
        var request = new AgendarEncontroPosRequest(paroquia.getId(), data, null);

        mockMvc.perform(post("/api/v1/encontros-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/encontros-pos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isConflict());
    }

    private Paroquia criarParoquia() {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Pós " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor Pós " + UUID.randomUUID(), diocese));
        return paroquiaRepository.save(new Paroquia("Paróquia Pós " + UUID.randomUUID(), setor));
    }
}
