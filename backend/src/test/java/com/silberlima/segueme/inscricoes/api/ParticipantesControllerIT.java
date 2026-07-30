package com.silberlima.segueme.inscricoes.api;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
import com.silberlima.segueme.inscricoes.api.dto.AtualizarPagamentoRequest;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPagamento;
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

/** Cobre {@code /api/v1/participantes} (RF-027). */
class ParticipantesControllerIT extends AbstractIntegrationTest {

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
    private PreInscricaoRepository preInscricaoRepository;

    @Autowired
    private FichaInscricaoRepository fichaInscricaoRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/participantes/funil")
                        .param("encontroId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldConsolidarFunilDoEncontro() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Funil");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa aguardando = pessoaRepository.save(umaPessoa("Aguardando Ficha", paroquia));
        Pessoa confirmado = pessoaRepository.save(umaPessoa("Confirmado", paroquia));
        preInscricaoRepository.save(new PreInscricao(aguardando, encontro));
        PreInscricao preInscricaoConfirmada =
                preInscricaoRepository.save(new PreInscricao(confirmado, encontro));
        preInscricaoConfirmada.confirmar();
        confirmado.confirmarComoSeguimista();
        fichaInscricaoRepository.save(new FichaInscricao(preInscricaoConfirmada, confirmado, encontro));
        preInscricaoRepository.save(preInscricaoConfirmada);
        pessoaRepository.save(confirmado);

        mockMvc.perform(get("/api/v1/participantes/funil")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.preInscritos").value(1))
                .andExpect(jsonPath("$.confirmados").value(1))
                .andExpect(jsonPath("$.seguidores").value(0));
    }

    @Test
    void shouldListarParticipantesAndAtualizarPagamento() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Participantes");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Participante", paroquia));
        PreInscricao preInscricao = preInscricaoRepository.save(new PreInscricao(pessoa, encontro));
        preInscricao.confirmar();
        pessoa.confirmarComoSeguimista();
        FichaInscricao ficha = fichaInscricaoRepository.save(new FichaInscricao(preInscricao, pessoa, encontro));
        preInscricaoRepository.save(preInscricao);
        pessoaRepository.save(pessoa);

        mockMvc.perform(get("/api/v1/participantes")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].statusPagamento").value("PENDENTE"));

        mockMvc.perform(patch("/api/v1/participantes/" + ficha.getId() + "/pagamento")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new AtualizarPagamentoRequest(StatusPagamento.PAGO)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.statusPagamento").value("PAGO"));
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Encontro criarEncontro(Paroquia paroquia, int vagas) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor " + paroquia.getNome(), paroquia));
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
