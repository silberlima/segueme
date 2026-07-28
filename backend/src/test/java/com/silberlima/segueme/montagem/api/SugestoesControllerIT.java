package com.silberlima.segueme.montagem.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

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
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/sugestoes} (RF-029/030, RN-08/RN-MONTAGEM-02). */
class SugestoesControllerIT extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

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
    void shouldReturn403WhenParoquiaTriesToSeeSugestoes() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Sugestões Autorização");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);

        mockMvc.perform(get("/api/v1/sugestoes")
                        .param("encontroId", encontro.getId().toString())
                        .param("equipeId", equipe.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListOnlySeguidoresAndFlagCrossParoquiaCandidate() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Sugestões");
        Paroquia outraParoquia = criarParoquia("Outra Paróquia Sugestões");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        pessoaRepository.save(umSeguidor("Seguidor Local", paroquia));
        pessoaRepository.save(umSeguidor("Seguidor de Outra Paróquia", outraParoquia));
        pessoaRepository.save(umaPessoa("Seguimista Não Elegível", paroquia));

        mockMvc.perform(get("/api/v1/sugestoes")
                        .param("encontroId", encontro.getId().toString())
                        .param("equipeId", equipe.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.pessoaNome=='Seguidor Local')].mesmaParoquia").value(true))
                .andExpect(jsonPath("$[?(@.pessoaNome=='Seguidor de Outra Paróquia')].mesmaParoquia").value(false))
                .andExpect(jsonPath("$[?(@.pessoaNome=='Seguidor de Outra Paróquia')].requerEmprestimoSetor")
                        .value(true));
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
