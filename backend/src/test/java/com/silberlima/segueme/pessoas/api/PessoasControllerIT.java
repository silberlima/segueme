package com.silberlima.segueme.pessoas.api;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
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
import com.silberlima.segueme.pessoas.api.dto.CriarPessoaRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/pessoas} (RF-007 a RF-011, RN-PESSOA-01 a 05). */
class PessoasControllerIT extends AbstractIntegrationTest {

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
        mockMvc.perform(get("/api/v1/pessoas")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenSeguidorTriesToCreatePessoa() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Autorização");
        CriarPessoaRequest request = umaRequestValida(paroquia.getId());

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreatePessoaWithPreInscritoSituacaoWhenParoquia() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Cadastro");
        CriarPessoaRequest request = umaRequestValida(paroquia.getId());

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.situacao").value("PRE_INSCRITO"))
                .andExpect(jsonPath("$.nomeCompleto").value("Pessoa de Teste"));
    }

    @Test
    void shouldListOnlyPessoasOfOwnParoquiaWhenParoquia() throws Exception {
        Paroquia paroquiaA = criarParoquia("Paróquia Escopo A");
        Paroquia paroquiaB = criarParoquia("Paróquia Escopo B");
        pessoaRepository.save(umaPessoa("Pessoa da Paróquia A", paroquiaA));
        pessoaRepository.save(umaPessoa("Pessoa da Paróquia B", paroquiaB));

        mockMvc.perform(get("/api/v1/pessoas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquiaA.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[*].nomeCompleto", hasItem("Pessoa da Paróquia A")));
    }

    @Test
    void shouldConsultarPerfilCompletoAndHistorico() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Perfil");
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Pessoa Perfil Completo", paroquia));

        mockMvc.perform(get("/api/v1/pessoas/" + pessoa.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Pessoa Perfil Completo"));

        mockMvc.perform(get("/api/v1/pessoas/" + pessoa.getId() + "/historico")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isOk());
    }

    /* Quem cuida de gente: Nacional fora, Seguidor só sobre si (decisão do PO — ver o Javadoc do controller). */

    @Test
    void shouldReturn403WhenNacionalListsPessoas() throws Exception {
        mockMvc.perform(get("/api/v1/pessoas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenNacionalCreatesPessoa() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Nacional Barrado");

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaRequestValida(paroquia.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenSeguidorListsPessoas() throws Exception {
        mockMvc.perform(get("/api/v1/pessoas")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldReturn403WhenSeguidorReadsSomeoneElsesProfile() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Alheia");
        Pessoa outra = pessoaRepository.save(umaPessoa("Pessoa Alheia", paroquia));

        mockMvc.perform(get("/api/v1/pessoas/" + outra.getId())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    /* "Meus dados": o alvo vem do token, nunca da requisição. */

    @Test
    void shouldReadAndUpdateOwnDataResolvedFromToken() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Meus Dados");
        Pessoa eu = pessoaRepository.save(umaPessoaDoUsuario("Eu Mesmo", paroquia, "sub-meus-dados"));
        pessoaRepository.save(umaPessoa("Outra Pessoa", paroquia));

        mockMvc.perform(get("/api/v1/pessoas/me")
                        .with(jwt().jwt(builder -> builder.subject("sub-meus-dados"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(eu.getId().toString()))
                .andExpect(jsonPath("$.nomeCompleto").value("Eu Mesmo"));

        mockMvc.perform(put("/api/v1/pessoas/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"telefone\":\"(11) 98888-8888\",\"alergias\":\"Amendoim\"}")
                        .with(jwt().jwt(builder -> builder.subject("sub-meus-dados"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("(11) 98888-8888"))
                .andExpect(jsonPath("$.alergias").value("Amendoim"))
                // O que não é autoeditável continua como estava.
                .andExpect(jsonPath("$.nomeCompleto").value("Eu Mesmo"));

        mockMvc.perform(get("/api/v1/pessoas/me/historico")
                        .with(jwt().jwt(builder -> builder.subject("sub-meus-dados"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturn404WhenUserHasNoPessoaLinked() throws Exception {
        mockMvc.perform(get("/api/v1/pessoas/me")
                        .with(jwt().jwt(builder -> builder.subject("sub-sem-vinculo"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isNotFound());
    }

    /** Todo usuário é seguidor: "meus dados" vale para qualquer perfil, não só para o Seguidor. */
    @Test
    void shouldAllowAnyProfileToReadOwnData() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Nacional Tem Dados");
        pessoaRepository.save(umaPessoaDoUsuario("Nacional Pessoa", paroquia, "sub-nacional"));

        mockMvc.perform(get("/api/v1/pessoas/me")
                        .with(jwt().jwt(builder -> builder.subject("sub-nacional"))
                                .authorities(new SimpleGrantedAuthority("ROLE_NACIONAL"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nomeCompleto").value("Nacional Pessoa"));
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome,
                LocalDate.of(2000, 1, 1),
                "(11) 90000-0000",
                EstadoCivil.SOLTEIRO,
                paroquia,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private Pessoa umaPessoaDoUsuario(String nome, Paroquia paroquia, String usuarioId) {
        Pessoa pessoa = umaPessoa(nome, paroquia);
        org.springframework.test.util.ReflectionTestUtils.setField(pessoa, "usuarioId", usuarioId);
        return pessoa;
    }

    private CriarPessoaRequest umaRequestValida(java.util.UUID paroquiaId) {
        return new CriarPessoaRequest(
                "Pessoa de Teste",
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of());
    }
}
