package com.silberlima.segueme.conteudo.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.conteudo.api.dto.DesignarPalestranteRequest;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
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
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/encontros/{id}/palestras} (RF-051/052, RN-CONTEUDO-01). */
class PalestrasControllerIT extends AbstractIntegrationTest {

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

    @Autowired
    private PalestranteRepository palestranteRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        Encontro encontro = criarEncontro();
        mockMvc.perform(get("/api/v1/encontros/{id}/palestras", encontro.getId())).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListSevenTemasEmOrdemFixa() throws Exception {
        Encontro encontro = criarEncontro();

        mockMvc.perform(get("/api/v1/encontros/{id}/palestras", encontro.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(7))
                .andExpect(jsonPath("$[0].numero").value(1))
                .andExpect(jsonPath("$[0].palestranteId").doesNotExist())
                .andExpect(jsonPath("$[6].numero").value(7));
    }

    @Test
    void shouldReturn403WhenDesignandoSemPerfilAutorizado() throws Exception {
        Encontro encontro = criarEncontro();
        Palestrante palestrante = palestranteRepository.save(Palestrante.externo("Convidado"));
        UUID palestraId = primeiraPalestraId(encontro);

        mockMvc.perform(patch("/api/v1/encontros/{id}/palestras/{palestraId}/designar", encontro.getId(), palestraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DesignarPalestranteRequest(palestrante.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldDesignarPalestranteAndReflectOnListagem() throws Exception {
        Encontro encontro = criarEncontro();
        Palestrante palestrante = palestranteRepository.save(Palestrante.externo("Convidado Designado"));
        UUID palestraId = primeiraPalestraId(encontro);

        mockMvc.perform(patch("/api/v1/encontros/{id}/palestras/{palestraId}/designar", encontro.getId(), palestraId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new DesignarPalestranteRequest(palestrante.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.palestranteId").value(palestrante.getId().toString()))
                .andExpect(jsonPath("$.palestranteNome").value("Convidado Designado"));

        mockMvc.perform(get("/api/v1/encontros/{id}/palestras", encontro.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].palestranteNome").value("Convidado Designado"));
    }

    private UUID primeiraPalestraId(Encontro encontro) throws Exception {
        String json = mockMvc.perform(get("/api/v1/encontros/{id}/palestras", encontro.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andReturn()
                .getResponse()
                .getContentAsString();
        return UUID.fromString(objectMapper.readTree(json).get(0).get("palestraId").asText());
    }

    private Encontro criarEncontro() {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Palestra " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor Palestra " + UUID.randomUUID(), diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia Palestra " + UUID.randomUUID(), setor));
        Pessoa diretorEspiritual = pessoaRepository.save(new Pessoa(
                "Diretor " + UUID.randomUUID(), LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO,
                paroquia, null, null, null, null, null, null, null));
        return encontroRepository.save(new Encontro(
                1, "São José", diretorEspiritual, "Sítio Teste", LocalDate.now().plusDays(30), LocalDate.now().plusDays(32), 40, paroquia));
    }
}
