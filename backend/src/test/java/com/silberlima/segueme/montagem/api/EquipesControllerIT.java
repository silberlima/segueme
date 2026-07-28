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
import com.silberlima.segueme.montagem.api.dto.AdicionarIntegranteRequest;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/equipes} (RF-028, RF-031, RN-06, RN-MONTAGEM-01/07). */
class EquipesControllerIT extends AbstractIntegrationTest {

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

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/equipes").param("encontroId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListSeededCatalogWithAllVagasOpenWhenNoIntegrante() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Equipes");
        Encontro encontro = criarEncontro(paroquia);

        mockMvc.perform(get("/api/v1/equipes")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].preenchidos").value(0))
                .andExpect(jsonPath("$[0].vagasAbertas").value(org.hamcrest.Matchers.greaterThan(0)));
    }

    @Test
    void shouldReturn403WhenSeguidorTriesToAddIntegrante() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Autorização");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoa = pessoaRepository.save(umSeguidor("Candidato", paroquia));

        mockMvc.perform(post("/api/v1/equipes/" + equipe.getId() + "/integrantes")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AdicionarIntegranteRequest(pessoa.getId(), PapelIntegrante.INTEGRANTE)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SEGUIDOR"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldAddIntegranteAsPendenteWhenMontagem() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Montagem");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoa = pessoaRepository.save(umSeguidor("Candidato Elegível", paroquia));

        mockMvc.perform(post("/api/v1/equipes/" + equipe.getId() + "/integrantes")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AdicionarIntegranteRequest(pessoa.getId(), PapelIntegrante.INTEGRANTE)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andExpect(jsonPath("$.pessoaId").value(pessoa.getId().toString()));

        mockMvc.perform(get("/api/v1/equipes/" + equipe.getId())
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preenchidos").value(1))
                .andExpect(jsonPath("$.integrantes.length()").value(1));
    }

    @Test
    void shouldReturn409WhenCandidateIsFromAnotherParoquia() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Origem");
        Paroquia outraParoquia = criarParoquia("Outra Paróquia");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoaForasteira = pessoaRepository.save(umSeguidor("Forasteiro", outraParoquia));

        mockMvc.perform(post("/api/v1/equipes/" + equipe.getId() + "/integrantes")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new AdicionarIntegranteRequest(pessoaForasteira.getId(), PapelIntegrante.INTEGRANTE)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isConflict());
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

    private Pessoa umSeguidor(String nome, Paroquia paroquia) {
        Pessoa pessoa = umaPessoa(nome, paroquia);
        ReflectionTestUtils.setField(pessoa, "situacao", SituacaoPessoa.SEGUIDOR);
        return pessoa;
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
