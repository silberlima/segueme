package com.silberlima.segueme.hierarquia.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/**
 * Cobre {@code GET /api/v1/dioceses} e {@code GET /api/v1/paroquias} juntos (RF-054/055, RN-04).
 * O changelog de Hierarquia semeia dados de demonstração também no banco de teste — por isso as
 * consultas sem escopo (NACIONAL) verificam a presença dos registros do próprio teste em vez de
 * um total exato, e pedem tamanho de página suficiente para caber o seed + os dados de cada teste.
 */
class HierarquiaControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DioceseRepository dioceseRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private ParoquiaRepository paroquiaRepository;

    @Test
    void shouldReturn401WhenNoTokenOnDioceses() throws Exception {
        mockMvc.perform(get("/api/v1/dioceses")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn401WhenNoTokenOnParoquias() throws Exception {
        mockMvc.perform(get("/api/v1/paroquias")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenPerfilHasNoAccessToDioceses() throws Exception {
        mockMvc.perform(get("/api/v1/dioceses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenPerfilHasNoAccessToParoquias() throws Exception {
        mockMvc.perform(get("/api/v1/paroquias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListAllDiocesesWhenNacional() throws Exception {
        Diocese dioceseA = dioceseRepository.save(new Diocese("Diocese Teste Nacional A"));
        criarSetorComParoquia(dioceseA, "Setor A1", "Paróquia A1a");
        dioceseRepository.save(new Diocese("Diocese Teste Nacional B"));

        mockMvc.perform(get("/api/v1/dioceses")
                        .param("size", "100")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome", hasItem("Diocese Teste Nacional A")))
                .andExpect(jsonPath("$.content[*].nome", hasItem("Diocese Teste Nacional B")));
    }

    @Test
    void shouldListOnlyOwnDioceseWhenDiocese() throws Exception {
        Diocese dioceseA = dioceseRepository.save(new Diocese("Diocese Escopo A"));
        criarSetorComParoquia(dioceseA, "Setor Escopo A1", "Paróquia Escopo A1a");
        dioceseRepository.save(new Diocese("Diocese Escopo B"));

        mockMvc.perform(get("/api/v1/dioceses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIOCESE"))
                                .jwt(builder -> builder.claim("diocese_id", dioceseA.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Diocese Escopo A"))
                .andExpect(jsonPath("$.content[0].setores[0].nome").value("Setor Escopo A1"))
                .andExpect(jsonPath("$.content[0].setores[0].paroquias[0].nome").value("Paróquia Escopo A1a"));
    }

    @Test
    void shouldReturn409WhenDioceseClaimMissing() throws Exception {
        mockMvc.perform(get("/api/v1/dioceses")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIOCESE"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldListAllParoquiasWhenNacional() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Paróquias Nacional"));
        Setor setor = setorRepository.save(new Setor("Setor Paróquias Nacional", diocese));
        paroquiaRepository.save(new Paroquia("Paróquia Teste Nacional", setor));

        mockMvc.perform(get("/api/v1/paroquias")
                        .param("size", "100")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome", hasItem("Paróquia Teste Nacional")));
    }

    @Test
    void shouldListOnlyParoquiasOfOwnDioceseWhenDiocese() throws Exception {
        Diocese dioceseA = dioceseRepository.save(new Diocese("Diocese Escopo Paróquia A"));
        Setor setorA = setorRepository.save(new Setor("Setor A", dioceseA));
        paroquiaRepository.save(new Paroquia("Paróquia da Diocese A", setorA));

        Diocese dioceseB = dioceseRepository.save(new Diocese("Diocese Escopo Paróquia B"));
        Setor setorB = setorRepository.save(new Setor("Setor B", dioceseB));
        paroquiaRepository.save(new Paroquia("Paróquia da Diocese B", setorB));

        mockMvc.perform(get("/api/v1/paroquias")
                        .param("size", "100")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_DIOCESE"))
                                .jwt(builder -> builder.claim("diocese_id", dioceseA.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nome", hasItem("Paróquia da Diocese A")))
                .andExpect(jsonPath("$.content[*].dioceseNome", hasItem("Diocese Escopo Paróquia A")));
    }

    @Test
    void shouldListOnlyParoquiasOfOwnSetorWhenSetorial() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Escopo Setorial"));
        Setor setorA = setorRepository.save(new Setor("Setor Escopo A", diocese));
        paroquiaRepository.save(new Paroquia("Paróquia do Setor A", setorA));
        Setor setorB = setorRepository.save(new Setor("Setor Escopo B", diocese));
        paroquiaRepository.save(new Paroquia("Paróquia do Setor B", setorB));

        mockMvc.perform(get("/api/v1/paroquias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setorA.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Paróquia do Setor A"));
    }

    @Test
    void shouldListOwnParoquiaWhenParoquia() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Escopo Paróquia Perfil"));
        Setor setor = setorRepository.save(new Setor("Setor Escopo Paróquia Perfil", diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia do Perfil Paróquia", setor));

        mockMvc.perform(get("/api/v1/paroquias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].nome").value("Paróquia do Perfil Paróquia"));
    }

    @Test
    void shouldReturn200WhenEquipeFichasListsParoquias() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Escopo Fichas"));
        Setor setor = setorRepository.save(new Setor("Setor Escopo Fichas", diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia do Perfil Fichas", setor));

        mockMvc.perform(get("/api/v1/paroquias")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    private void criarSetorComParoquia(Diocese diocese, String nomeSetor, String nomeParoquia) {
        Setor setor = setorRepository.save(new Setor(nomeSetor, diocese));
        paroquiaRepository.save(new Paroquia(nomeParoquia, setor));
    }
}
