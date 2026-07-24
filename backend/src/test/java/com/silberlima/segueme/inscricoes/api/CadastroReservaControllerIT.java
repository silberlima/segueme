package com.silberlima.segueme.inscricoes.api;

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
import com.silberlima.segueme.inscricoes.api.dto.PromoverReservaRequest;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
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

/** Cobre {@code /api/v1/cadastro-reserva} (RF-025/026, RN-INSCRICAO-03/04). */
class CadastroReservaControllerIT extends AbstractIntegrationTest {

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
    private CadastroReservaRepository cadastroReservaRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/cadastro-reserva").param("encontroId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldListReservaInArrivalOrder() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Reserva");
        Encontro encontro = criarEncontro(paroquia, 10);
        Pessoa primeiro = pessoaRepository.save(umaPessoa("Primeiro", paroquia));
        Pessoa segundo = pessoaRepository.save(umaPessoa("Segundo", paroquia));
        cadastroReservaRepository.save(new CadastroReserva(primeiro, encontro));
        cadastroReservaRepository.save(new CadastroReserva(segundo, encontro));

        mockMvc.perform(get("/api/v1/cadastro-reserva")
                        .param("encontroId", encontro.getId().toString())
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].ordem").value(1))
                .andExpect(jsonPath("$[0].pessoaNome").value("Primeiro"))
                .andExpect(jsonPath("$[1].ordem").value(2));
    }

    @Test
    void shouldPromoverCandidatoToPreInscricao() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Promocao");
        Encontro encontroOrigem = criarEncontro(paroquia, 10);
        Encontro encontroDestino = criarEncontro(paroquia, 10);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato Reserva", paroquia));
        CadastroReserva reserva = cadastroReservaRepository.save(new CadastroReserva(pessoa, encontroOrigem));

        mockMvc.perform(post("/api/v1/cadastro-reserva/" + reserva.getId() + "/promover")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new PromoverReservaRequest(encontroDestino.getId())))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_FICHAS"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.encontroId").value(encontroDestino.getId().toString()))
                .andExpect(jsonPath("$.status").value("AGUARDANDO_FICHA"));
    }

    private Paroquia criarParoquia(String nome) {
        Diocese diocese = dioceseRepository.save(new Diocese("Diocese " + nome));
        Setor setor = setorRepository.save(new Setor("Setor " + nome, diocese));
        return paroquiaRepository.save(new Paroquia(nome, setor));
    }

    private Encontro criarEncontro(Paroquia paroquia, int vagas) {
        Pessoa diretorEspiritual = pessoaRepository.save(umaPessoa("Diretor " + paroquia.getNome(), paroquia));
        return encontroRepository.save(new Encontro(
                encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquia.getId())
                        .map(e -> e.getNumero() + 1)
                        .orElse(1),
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
