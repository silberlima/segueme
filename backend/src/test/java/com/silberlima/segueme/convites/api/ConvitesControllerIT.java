package com.silberlima.segueme.convites.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.silberlima.segueme.convites.api.dto.EmitirConviteRequest;
import com.silberlima.segueme.convites.api.dto.RecusarConviteRequest;
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
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.notificacao.domain.NotificacaoRepository;
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

/** Cobre {@code /api/v1/convites} (RF-034 a RF-039, RN-CONVITE-01 a 04). */
class ConvitesControllerIT extends AbstractIntegrationTest {

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
    private NotificacaoRepository notificacaoRepository;

    @Test
    void shouldReturn401WhenNoToken() throws Exception {
        mockMvc.perform(get("/api/v1/convites").param("encontroId", UUID.randomUUID().toString()))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn403WhenEmitindoConviteSemRoleComando() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia 403");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa pessoa = pessoaRepository.save(umaPessoa("Candidato", paroquia));

        EmitirConviteRequest request = new EmitirConviteRequest(equipe.getId(), pessoa.getId(), PapelIntegrante.COORDENADOR);

        mockMvc.perform(post("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isForbidden());
    }

    @Test
    void shouldRejectConviteDeIntegranteAntesDoCoordenadorAceitar() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Ordem");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa integrante = pessoaRepository.save(umaPessoa("Integrante", paroquia));

        EmitirConviteRequest request = new EmitirConviteRequest(equipe.getId(), integrante.getId(), PapelIntegrante.INTEGRANTE);

        mockMvc.perform(post("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                // Violação de regra de negócio (RN-CONVITE-01) responde 409 pelo GlobalExceptionHandler.
                .andExpect(status().isConflict());
    }

    @Test
    void shouldRunFullInviteChainAndNotifyIssuerOnRefusal() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Convite");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa coordenador = pessoaRepository.save(umaPessoa("Coordenador", paroquia));
        Pessoa integrante = pessoaRepository.save(umaPessoa("Integrante", paroquia));

        // 1. Comando emite convite de coordenador.
        String conviteCoordenadorJson = mockMvc.perform(post("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EmitirConviteRequest(equipe.getId(), coordenador.getId(), PapelIntegrante.COORDENADOR)))
                        .with(jwt().jwt(builder -> builder.subject("comando-1").claim("paroquia_id", paroquia.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("PENDENTE"))
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID conviteCoordenadorId = UUID.fromString(objectMapper.readTree(conviteCoordenadorJson).get("id").asText());

        // 2. Coordenador aceita.
        mockMvc.perform(patch("/api/v1/convites/{id}/aceitar", conviteCoordenadorId)
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACEITO"));

        // 3. Agora o convite de integrante é aceito pela ordem do fluxo (RN-CONVITE-01).
        String conviteIntegranteJson = mockMvc.perform(post("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EmitirConviteRequest(equipe.getId(), integrante.getId(), PapelIntegrante.INTEGRANTE)))
                        .with(jwt().jwt(builder -> builder.subject("comando-1").claim("paroquia_id", paroquia.getId().toString()))
                                .authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID conviteIntegranteId = UUID.fromString(objectMapper.readTree(conviteIntegranteJson).get("id").asText());

        // 4. Integrante recusa com justificativa — deve notificar quem emitiu o convite (comando-1).
        mockMvc.perform(patch("/api/v1/convites/{id}/recusar", conviteIntegranteId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new RecusarConviteRequest("Mudança de agenda.")))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("RECUSADO"))
                .andExpect(jsonPath("$.justificativaRecusa").value("Mudança de agenda."));

        assertThat(notificacaoRepository
                        .findByDestinatarioIdAndDeletedFalse("comando-1", org.springframework.data.domain.Pageable.unpaged())
                        .getContent())
                .isNotEmpty();

        // 5. Listagem filtrada por status RECUSADO reflete a recusa.
        mockMvc.perform(get("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .param("status", "RECUSADO")
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_MONTAGEM"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void shouldResendPendingConvite() throws Exception {
        Paroquia paroquia = criarParoquia("Paróquia Reenvio");
        Encontro encontro = criarEncontro(paroquia);
        Equipe equipe = equipeRepository.findByDeletedFalseOrderByNomeAsc().get(0);
        Pessoa coordenador = pessoaRepository.save(umaPessoa("Coordenador", paroquia));

        String conviteJson = mockMvc.perform(post("/api/v1/convites")
                        .param("encontroId", encontro.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(
                                new EmitirConviteRequest(equipe.getId(), coordenador.getId(), PapelIntegrante.COORDENADOR)))
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();
        UUID conviteId = UUID.fromString(objectMapper.readTree(conviteJson).get("id").asText());

        mockMvc.perform(patch("/api/v1/convites/{id}/reenviar", conviteId)
                        .with(jwt().jwt(token -> token.claim("paroquia_id", paroquia.getId().toString())).authorities(new SimpleGrantedAuthority("ROLE_EQUIPE_COMANDO"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reenvios").value(1));
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
