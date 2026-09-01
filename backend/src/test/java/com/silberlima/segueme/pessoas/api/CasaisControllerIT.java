package com.silberlima.segueme.pessoas.api;

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
import com.silberlima.segueme.pessoas.api.dto.CriarCasalRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/casais} (RF-012, RN-PESSOA-03/RN-03). */
class CasaisControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Autowired
    private DioceseRepository dioceseRepository;

    @Autowired
    private SetorRepository setorRepository;

    @Autowired
    private ParoquiaRepository paroquiaRepository;

    @Autowired
    private PessoaRepository pessoaRepository;

    @Test
    void shouldReturn401WhenNoTokenOnListar() throws Exception {
        mockMvc.perform(get("/api/v1/casais")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldVincularCasalAndRejectDuplicateVinculo() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Casal");
        Pessoa pessoa1 = pessoaRepository.save(umaPessoa("Pessoa Um Casal", paroquia));
        Pessoa pessoa2 = pessoaRepository.save(umaPessoa("Pessoa Dois Casal", paroquia));
        Pessoa pessoa3 = pessoaRepository.save(umaPessoa("Pessoa Três Casal", paroquia));
        CriarCasalRequest request = new CriarCasalRequest(pessoa1.getId(), pessoa2.getId(), LocalDate.of(2021, 6, 1));

        mockMvc.perform(post("/api/v1/casais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoa1.nomeCompleto").value("Pessoa Um Casal"))
                .andExpect(jsonPath("$.pessoa2.nomeCompleto").value("Pessoa Dois Casal"));

        CriarCasalRequest duplicado =
                new CriarCasalRequest(pessoa1.getId(), pessoa3.getId(), LocalDate.of(2021, 6, 1));
        mockMvc.perform(post("/api/v1/casais")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicado))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isConflict());
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(1998, 1, 1),
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
                null);
    }
}
