package com.silberlima.segueme.conteudo.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.conteudo.api.dto.CadastrarPalestranteRequest;
import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
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
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/palestrantes} (RF-052, RN-CONTEUDO-02). */
class PalestrantesControllerIT extends AbstractIntegrationTest {

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

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/palestrantes")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenCadastrandoSemPerfilAutorizado() throws Exception {
        mockMvc.perform(post("/api/v1/palestrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastrarPalestranteRequest(TipoPalestrante.EXTERNO, "Convidado", null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCadastrarExterno() throws Exception {
        mockMvc.perform(post("/api/v1/palestrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastrarPalestranteRequest(TipoPalestrante.EXTERNO, "Convidado Externo", null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("EXTERNO"))
                .andExpect(jsonPath("$.pessoaId").doesNotExist());
    }

    @Test
    void shouldCadastrarSeguidorVinculadoAPessoa() throws Exception {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese Palestrante " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor Palestrante " + UUID.randomUUID(), diocese));
        Paroquia paroquia = paroquiaRepository.save(new Paroquia("Paróquia Palestrante " + UUID.randomUUID(), setor));
        Pessoa pessoa = pessoaRepository.save(new Pessoa(
                "Seguidor Palestrante " + UUID.randomUUID(),
                PessoasDeTeste.cpf(),
                LocalDate.of(1990, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null));

        mockMvc.perform(post("/api/v1/palestrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastrarPalestranteRequest(TipoPalestrante.SEGUIDOR, null, pessoa.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.tipo").value("SEGUIDOR"))
                .andExpect(jsonPath("$.pessoaId").value(pessoa.getId().toString()))
                .andExpect(jsonPath("$.nome").value(pessoa.getNomeCompleto()));
    }

    @Test
    void shouldRejectSeguidorSemPessoaId() throws Exception {
        mockMvc.perform(post("/api/v1/palestrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastrarPalestranteRequest(TipoPalestrante.SEGUIDOR, null, null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldListPalestrantesFiltradosPorTipo() throws Exception {
        mockMvc.perform(post("/api/v1/palestrantes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CadastrarPalestranteRequest(TipoPalestrante.CLERO, "Padre Filtro " + UUID.randomUUID(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/palestrantes")
                        .param("tipo", "CLERO")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].tipo").value("CLERO"));
    }
}
