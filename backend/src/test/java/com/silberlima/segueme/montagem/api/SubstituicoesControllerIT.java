package com.silberlima.segueme.montagem.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
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
import com.silberlima.segueme.montagem.api.dto.RegistrarSubstituicaoRequest;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
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

/** Cobre {@code /api/v1/substituicoes} (RF-032/033, RN-MONTAGEM-01/05/06). */
class SubstituicoesControllerIT extends AbstractIntegrationTest {

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
    private EquipeRepository equipeRepository;

    @Autowired
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/substituicoes").param("encontroId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldRegisterSubstituicaoAndListHistory() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Substituição");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoaSai = pessoaRepository.save(umaPessoa("Vai Sair", paroquia));
        Pessoa pessoaEntra = pessoaRepository.save(umaPessoa("Vai Entrar", paroquia));
        integranteEquipeRepository.save(new IntegranteEquipe(encontro, equipe, pessoaSai, PapelIntegrante.INTEGRANTE));

        RegistrarSubstituicaoRequest request = new RegistrarSubstituicaoRequest(
                encontro.getId(), equipe.getId(), pessoaSai.getId(), "Recusou o convite", pessoaEntra.getId(), "Substituto já treinado.");

        mockMvc.perform(post("/api/v1/substituicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.pessoaSaiNome").value("Vai Sair"))
                .andExpect(jsonPath("$.pessoaEntraNome").value("Vai Entrar"))
                .andExpect(jsonPath("$.justificativa").value("Substituto já treinado."));

        mockMvc.perform(get("/api/v1/substituicoes")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void shouldReturn400WhenJustificativaIsBlank() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Justificativa");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoaSai = pessoaRepository.save(umaPessoa("Vai Sair", paroquia));
        Pessoa pessoaEntra = pessoaRepository.save(umaPessoa("Vai Entrar", paroquia));
        integranteEquipeRepository.save(new IntegranteEquipe(encontro, equipe, pessoaSai, PapelIntegrante.INTEGRANTE));

        RegistrarSubstituicaoRequest request = new RegistrarSubstituicaoRequest(
                encontro.getId(), equipe.getId(), pessoaSai.getId(), "Recusou o convite", pessoaEntra.getId(), "  ");

        mockMvc.perform(post("/api/v1/substituicoes")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isBadRequest());
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome + " " + UUID.randomUUID()));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome + " " + UUID.randomUUID(), setor));
    }

    private Encontro criarEncontro(Paroquia paroquia) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor " + UUID.randomUUID(), paroquia));
        return encontroRepository.save(new Encontro(
                1, "São José", diretorEspiritual, "Sítio Teste", LocalDate.now().plusDays(30), LocalDate.now().plusDays(32), 40, paroquia));
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
