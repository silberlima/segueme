package com.silberlima.segueme.movimentacao.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.movimentacao.api.dto.RecusarEmprestimoRequest;
import com.silberlima.segueme.movimentacao.api.dto.SelecionarCandidatosRequest;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarEmprestimoRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/emprestimos} (RF-042 a RF-046, RN-18). */
class EmprestimosControllerIT extends AbstractIntegrationTest {

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
        mockMvc.perform(get("/api/v1/emprestimos")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenSolicitandoSemRoleMontagem() throws Exception {
        Setor setor = criarSetor("Setor 403");
        Paroquia paroquia = criarParoquia("Paróquia 403", setor);
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);

        mockMvc.perform(post("/api/v1/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarEmprestimoRequest(encontro.getId(), equipe.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRunFullLoanChainAndAddCandidateToRoster() throws Exception {
        Setor setor = criarSetor("Setor Empréstimo");
        Paroquia paroquiaDoEncontro = criarParoquia("Paróquia do Encontro", setor);
        Paroquia outraParoquiaDoSetor = criarParoquia("Outra Paróquia do Setor", setor);
        Encontro encontro = criarEncontro(paroquiaDoEncontro);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa candidatoElegivel = pessoaRepository.save(umSeguidor("Elegível", outraParoquiaDoSetor));
        pessoaRepository.save(umSeguidor("Mesma Paróquia", paroquiaDoEncontro));

        // 1. Montagem solicita o empréstimo.
        String emprestimoJson = mockMvc.perform(post("/api/v1/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarEmprestimoRequest(encontro.getId(), equipe.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM")).jwt(builder -> builder.claim("paroquia_id", paroquiaDoEncontro.getId().toString()))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("SOLICITADO"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID emprestimoId = UUID.fromString(objectMapper.readTree(emprestimoJson).get("id").asText());

        // 2. Setorial lista candidatos: só o de outra paróquia do mesmo setor aparece.
        mockMvc.perform(get("/api/v1/emprestimos/{id}/candidatos", emprestimoId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].pessoaNome").value("Elegível"));

        // 3. Setorial seleciona e envia o candidato à Montagem.
        mockMvc.perform(patch("/api/v1/emprestimos/{id}/enviar-candidatos", emprestimoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SelecionarCandidatosRequest(List.of(candidatoElegivel.getId()))))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUGESTOES_ENVIADAS"))
                .andExpect(jsonPath("$.candidatos.length()").value(1));

        // 4. Montagem efetiva: candidato passa a integrar o roster da equipe.
        mockMvc.perform(patch("/api/v1/emprestimos/{id}/efetivar", emprestimoId)
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM")).jwt(builder -> builder.claim("paroquia_id", paroquiaDoEncontro.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("EFETIVADO"))
                .andExpect(jsonPath("$.candidatos[0].status").value("EFETIVADO"));

        assertThat(integranteEquipeRepository.findByEncontro_IdAndEquipe_Id(encontro.getId(), equipe.getId()))
                .anyMatch(integrante -> integrante.getPessoa().getId().equals(candidatoElegivel.getId()));
    }

    @Test
    void shouldRefuseSolicitadoLoanWithJustificativa() throws Exception {
        Setor setor = criarSetor("Setor Recusa Empréstimo");
        Paroquia paroquia = criarParoquia("Paróquia Recusa Empréstimo", setor);
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);

        String emprestimoJson = mockMvc.perform(post("/api/v1/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarEmprestimoRequest(encontro.getId(), equipe.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM")).jwt(builder -> builder.claim("paroquia_id", paroquia.getId().toString()))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID emprestimoId = UUID.fromString(objectMapper.readTree(emprestimoJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/emprestimos/{id}/recusar", emprestimoId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecusarEmprestimoRequest("Sem candidatos no setor.")))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADO"));
    }

    private Setor criarSetor(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome + " " + UUID.randomUUID()));
        return setorRepository.save(new Setor("Setor " + nome + " " + UUID.randomUUID(), diocese));
    }

    private Paroquia criarParoquia(String nome, Setor setor) {
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
