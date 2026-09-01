package com.silberlima.segueme.encontros.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.silberlima.segueme.encontros.api.dto.CriarEncontroRequest;
import com.silberlima.segueme.encontros.domain.PastaDirigente;
import com.silberlima.segueme.encontros.domain.TipoMembroDirigente;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/encontros} (RF-014 a RF-021, RN-16, RN-20, RN-21). */
class EncontrosControllerIT extends AbstractIntegrationTest {

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

    @Autowired
    private CasalRepository casalRepository;

    @Test
    void shouldReturn401WhenNoTokenOnListar() throws Exception {
        mockMvc.perform(get("/api/v1/encontros")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenSeguidorTriesToCreateEncontro() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Autorização");
        CriarEncontroRequest request = umaRequestValida(paroquia);

        mockMvc.perform(post("/api/v1/encontros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreateEncontroWithSequentialNumeroAndComando() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Cadastro");
        CriarEncontroRequest request = umaRequestValida(paroquia);

        mockMvc.perform(post("/api/v1/encontros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.numero").value(1))
                .andExpect(jsonPath("$.status").value("ATIVO"))
                .andExpect(jsonPath("$.equipeComando.coordenadorGeralId").isNotEmpty());
    }

    @Test
    void shouldListOnlyEncontrosOfOwnParoquiaWhenParoquia() throws Exception {
        Paroquia paroquiaA = criarParoquia("Paróquia Escopo A");
        Paroquia paroquiaB = criarParoquia("Paróquia Escopo B");
        criarEncontro(paroquiaA);
        criarEncontro(paroquiaB);

        mockMvc.perform(get("/api/v1/encontros")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaA.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].paroquiaId").value(paroquiaA.getId().toString()));
    }

    @Test
    void shouldReturn404WhenEncontroNotFound() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Consulta");

        mockMvc.perform(get("/api/v1/encontros/" + UUID.randomUUID())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isNotFound());
    }

    private void criarEncontro(Paroquia paroquia) throws Exception {
        mockMvc.perform(post("/api/v1/encontros")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaRequestValida(paroquia)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated());
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private CriarEncontroRequest umaRequestValida(Paroquia paroquia) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor Espiritual", paroquia));
        Pessoa coordenadorGeral = pessoaRepository.save(umaPessoa("Coordenador Geral", paroquia));
        Pessoa pessoa1 = pessoaRepository.save(umaPessoa("Casal Pessoa 1", paroquia));
        Pessoa pessoa2 = pessoaRepository.save(umaPessoa("Casal Pessoa 2", paroquia));
        Casal casal = casalRepository.save(new Casal(pessoa1, pessoa2, LocalDate.of(2020, 1, 1)));

        return new CriarEncontroRequest(
                "São José",
                diretorEspiritual.getId(),
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquia.getId(),
                List.of(new CriarEncontroRequest.MembroDirigenteInput(
                        PastaDirigente.FINANCAS, TipoMembroDirigente.CASAL, null, casal.getId())),
                new CriarEncontroRequest.EquipeComandoInput(coordenadorGeral.getId(), casal.getId()));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
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
