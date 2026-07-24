package com.silberlima.segueme.inscricoes.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.inscricoes.api.dto.CriarPreInscricaoRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/pre-inscricoes} (RF-022 a RF-024, RN-INSCRICAO-01 a 03). */
class PreInscricoesControllerIT extends AbstractIntegrationTest {

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

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenSeguidorTriesToCreate() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Autorização");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", paroquia));

        mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPreInscricaoRequest(pessoa.getId(), encontro.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldCreatePreInscricaoWhenVagaAvailable() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Vaga");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", paroquia));

        mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPreInscricaoRequest(pessoa.getId(), encontro.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emReserva").value(false))
                .andExpect(jsonPath("$.preInscricao.status").value("AGUARDANDO_FICHA"))
                .andExpect(jsonPath("$.preInscricao.pessoaNome").value("Candidato"));
    }

    @Test
    void shouldFallBackToReservaWhenVagasEsgotadas() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Esgotada");
        Encontro encontro = criarEncontro(paroquia, 1);
        Pessoa primeiro = pessoaRepository.save(umaPessoa("Primeiro Candidato", paroquia));
        Pessoa segundo = pessoaRepository.save(umaPessoa("Segundo Candidato", paroquia));

        mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPreInscricaoRequest(primeiro.getId(), encontro.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emReserva").value(false));

        mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPreInscricaoRequest(segundo.getId(), encontro.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.emReserva").value(true))
                .andExpect(jsonPath("$.cadastroReserva.pessoaNome").value("Segundo Candidato"));
    }

    @Test
    void shouldPreencherFichaAndPromotePessoaToSeguimista() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Ficha");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato Ficha", paroquia));

        String responseJson = mockMvc.perform(post("/api/v1/pre-inscricoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new CriarPreInscricaoRequest(pessoa.getId(), encontro.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        String preInscricaoId = objectMapper.readTree(responseJson).get("preInscricao").get("id").asText();

        mockMvc.perform(post("/api/v1/pre-inscricoes/" + preInscricaoId + "/ficha")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.statusPagamento").value("PENDENTE"));
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Encontro criarEncontro(Paroquia paroquia, int vagas) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor Espiritual", paroquia));
        return encontroRepository.save(new Encontro(
                1,
                "São José",
                diretorEspiritual,
                "Sítio Teste",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                vagas,
                paroquia));
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
}
