package com.silberlima.segueme.movimentacao.api;

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
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarEmprestimoRequest;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarTransferenciaRequest;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.AbstractIntegrationTest;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;

/** Cobre {@code /api/v1/solicitacoes} — inbox único do Setorial (RF-046). */
class SolicitacoesControllerIT extends AbstractIntegrationTest {

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
        mockMvc.perform(get("/api/v1/solicitacoes")).andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenNotSetorial() throws Exception {
        mockMvc.perform(get("/api/v1/solicitacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldListBothPendingTransferAndLoanForTheSetor() throws Exception {
        Setor setor = criarSetor("Setor Inbox");
        Paroquia origem = criarParoquia("Origem Inbox", setor);
        Paroquia destino = criarParoquia("Destino Inbox", setor);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato Inbox", origem));
        Encontro encontro = criarEncontro(origem);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);

        mockMvc.perform(post("/api/v1/transferencias")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new SolicitarTransferenciaRequest(pessoa.getId(), destino.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_PAROQUIA"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/v1/emprestimos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new SolicitarEmprestimoRequest(encontro.getId(), equipe.getId(), null)))
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))
                                .jwt(builder -> builder.claim("paroquia_id", origem.getId().toString()))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/solicitacoes")
                        .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_SETORIAL"))
                                .jwt(builder -> builder.claim("setor_id", setor.getId().toString()))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[?(@.tipo=='TRANSFERENCIA')]").exists())
                .andExpect(jsonPath("$[?(@.tipo=='EMPRESTIMO')]").exists());
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
