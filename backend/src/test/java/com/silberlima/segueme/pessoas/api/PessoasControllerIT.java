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
import com.silberlima.segueme.pessoas.api.dto.AtualizarMeusDadosRequest;
import com.silberlima.segueme.pessoas.api.dto.CriarPessoaRequest;
import com.silberlima.segueme.pessoas.api.dto.EnderecoRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
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

        String novoCpf = PessoasDeTeste.cpf();
        mockMvc.perform(put("/api/v1/pessoas/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaEdicaoValida(novoCpf, LocalDate.of(1999, 5, 20))))
                        .with(jwt().jwt(builder -> builder.subject("sub-meus-dados"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.telefone").value("(11) 98888-8888"))
                .andExpect(jsonPath("$.alergias").value("Amendoim"))
                .andExpect(jsonPath("$.cpf").value(novoCpf))
                .andExpect(jsonPath("$.dataNascimento").value("1999-05-20"))
                .andExpect(jsonPath("$.instagram").value("eu.mesmo"))
                .andExpect(jsonPath("$.endereco.cidade").value("Rio de Janeiro"))
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

    /** Corrigir o próprio CPF não pode virar porta para assumir o de outra pessoa (RN-PESSOA-01). */
    @Test
    void shouldReturn409WhenOwnDataTakesAnotherPessoasCpf() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia CPF Alheio");
        pessoaRepository.save(umaPessoaDoUsuario("Eu De Novo", paroquia, "sub-cpf-alheio"));
        Pessoa outra = pessoaRepository.save(umaPessoa("Dona do CPF", paroquia));

        mockMvc.perform(put("/api/v1/pessoas/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                umaEdicaoValida(outra.getCpf(), LocalDate.of(1999, 5, 20))))
                        .with(jwt().jwt(builder -> builder.subject("sub-cpf-alheio"))
                                .authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isConflict());
    }

    /* CPF: chave natural do cadastro nacional único (RN-PESSOA-01). */

    @Test
    void shouldReturn409WhenCpfAlreadyExists() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia CPF Duplicado");
        String cpf = PessoasDeTeste.cpf();

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaRequestValida(paroquia.getId(), cpf)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(umaRequestValida(paroquia.getId(), cpf)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isConflict());
    }

    @Test
    void shouldReturn400WhenCpfCheckDigitsAreWrong() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia CPF Inválido");

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                umaRequestValida(paroquia.getId(), "529.982.247-00")))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.cpf").exists());
    }

    /** Endereço é obrigatório: cadastro sem ele não passa da validação. */
    @Test
    void shouldReturn400WhenEnderecoIsMissing() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Sem Endereço");
        String corpo = objectMapper.writeValueAsString(umaRequestValida(paroquia.getId()))
                .replaceFirst("\"endereco\":\\{.*?\\}", "\"endereco\":null");

        mockMvc.perform(post("/api/v1/pessoas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(corpo)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.endereco").exists());
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

    private Pessoa umaPessoaDoUsuario(String nome, Paroquia paroquia, String usuarioId) {
        Pessoa pessoa = umaPessoa(nome, paroquia);
        org.springframework.test.util.ReflectionTestUtils.setField(pessoa, "usuarioId", usuarioId);
        return pessoa;
    }

    private CriarPessoaRequest umaRequestValida(java.util.UUID paroquiaId) {
        return umaRequestValida(paroquiaId, PessoasDeTeste.cpf());
    }

    private CriarPessoaRequest umaRequestValida(java.util.UUID paroquiaId, String cpf) {
        return new CriarPessoaRequest(
                "Pessoa de Teste",
                cpf,
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                null,
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                umEnderecoRequest(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of());
    }

    private EnderecoRequest umEnderecoRequest() {
        return new EnderecoRequest(
                "01310-100", "Avenida Paulista", "1000", null, "Bela Vista", "São Paulo", "SP");
    }

    private AtualizarMeusDadosRequest umaEdicaoValida(String cpf, LocalDate dataNascimento) {
        return new AtualizarMeusDadosRequest(
                cpf,
                dataNascimento,
                "(11) 98888-8888",
                "@eu.mesmo",
                new EnderecoRequest(
                        "20040-020", "Avenida Rio Branco", "156", "Sala 8", "Centro", "Rio de Janeiro", "RJ"),
                "Amendoim",
                null,
                null,
                null,
                null,
                null);
    }
}
