package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.movimentacao.api.dto.CandidatoElegivelResponse;
import com.silberlima.segueme.movimentacao.api.dto.EmprestimoResponse;
import com.silberlima.segueme.movimentacao.api.dto.RecusarEmprestimoRequest;
import com.silberlima.segueme.movimentacao.api.dto.SelecionarCandidatosRequest;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarEmprestimoRequest;
import com.silberlima.segueme.movimentacao.application.EfetivarEmprestimoUseCase;
import com.silberlima.segueme.movimentacao.application.ListarCandidatosDoEmprestimoUseCase;
import com.silberlima.segueme.movimentacao.application.ListarCandidatosEmprestimoUseCase;
import com.silberlima.segueme.movimentacao.application.ListarEmprestimosUseCase;
import com.silberlima.segueme.movimentacao.application.RecusarEmprestimoUseCase;
import com.silberlima.segueme.movimentacao.application.SelecionarEEnviarCandidatosUseCase;
import com.silberlima.segueme.movimentacao.application.SolicitarEmprestimoUseCase;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Empréstimo temporário de um Seguidor de outra paróquia numa equipe do Encontro corrente
 * (RF-042 a RF-046, RN-18): fluxo bidirecional Montagem ⇄ Setorial.
 */
@RestController
@RequestMapping("/api/v1/emprestimos")
@Tag(name = "Empréstimos", description = "Uso temporário de um seguidor de outra paróquia numa equipe do Encontro corrente (RF-042 a RF-046, RN-18)")
public class EmprestimosController {

    private static final String PERFIS_MONTAGEM = "hasAnyRole('EQUIPE_MONTAGEM')";
    private static final String PERFIS_SETORIAL = "hasAnyRole('SETORIAL')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO','SEGUIDOR')";

    private final SolicitarEmprestimoUseCase solicitarEmprestimoUseCase;
    private final ListarEmprestimosUseCase listarEmprestimosUseCase;
    private final ListarCandidatosEmprestimoUseCase listarCandidatosEmprestimoUseCase;
    private final ListarCandidatosDoEmprestimoUseCase listarCandidatosDoEmprestimoUseCase;
    private final SelecionarEEnviarCandidatosUseCase selecionarEEnviarCandidatosUseCase;
    private final EfetivarEmprestimoUseCase efetivarEmprestimoUseCase;
    private final RecusarEmprestimoUseCase recusarEmprestimoUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final EmprestimoResponseMapper emprestimoResponseMapper;

    public EmprestimosController(
            SolicitarEmprestimoUseCase solicitarEmprestimoUseCase,
            ListarEmprestimosUseCase listarEmprestimosUseCase,
            ListarCandidatosEmprestimoUseCase listarCandidatosEmprestimoUseCase,
            ListarCandidatosDoEmprestimoUseCase listarCandidatosDoEmprestimoUseCase,
            SelecionarEEnviarCandidatosUseCase selecionarEEnviarCandidatosUseCase,
            EfetivarEmprestimoUseCase efetivarEmprestimoUseCase,
            RecusarEmprestimoUseCase recusarEmprestimoUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            EmprestimoResponseMapper emprestimoResponseMapper) {
        this.solicitarEmprestimoUseCase = solicitarEmprestimoUseCase;
        this.listarEmprestimosUseCase = listarEmprestimosUseCase;
        this.listarCandidatosEmprestimoUseCase = listarCandidatosEmprestimoUseCase;
        this.listarCandidatosDoEmprestimoUseCase = listarCandidatosDoEmprestimoUseCase;
        this.selecionarEEnviarCandidatosUseCase = selecionarEEnviarCandidatosUseCase;
        this.efetivarEmprestimoUseCase = efetivarEmprestimoUseCase;
        this.recusarEmprestimoUseCase = recusarEmprestimoUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.emprestimoResponseMapper = emprestimoResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_MONTAGEM)
    @Operation(summary = "Solicita um empréstimo de seguidor de outra paróquia ao Setorial (RF-042)")
    public ResponseEntity<EmprestimoResponse> solicitar(@Valid @RequestBody SolicitarEmprestimoRequest request) {
        Emprestimo emprestimo =
                solicitarEmprestimoUseCase.execute(request.encontroId(), request.equipeId(), request.competenciaRequeridaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(emprestimo));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista Empréstimos do escopo territorial do usuário, com filtro opcional por encontro/equipe/status")
    public ResponseEntity<List<EmprestimoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) UUID encontroId,
            @RequestParam(required = false) UUID equipeId,
            @RequestParam(required = false) StatusEmprestimo status) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        List<EmprestimoResponse> emprestimos = listarEmprestimosUseCase.execute(escopo, encontroId, equipeId, status).stream()
                .map(this::toResponse)
                .toList();
        return ResponseEntity.ok(emprestimos);
    }

    @GetMapping("/{emprestimoId}/candidatos")
    @PreAuthorize(PERFIS_SETORIAL)
    @Operation(summary = "Pool de candidatos elegíveis a um empréstimo, filtrável por paróquia/habilidade (RF-043, RN-MOVIMENTO-05)")
    public ResponseEntity<List<CandidatoElegivelResponse>> candidatos(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @PathVariable UUID emprestimoId,
            @RequestParam(required = false) UUID paroquiaId,
            @RequestParam(required = false) UUID competenciaId) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        List<CandidatoElegivelResponse> candidatos = listarCandidatosEmprestimoUseCase
                .execute(emprestimoId, escopo, paroquiaId, competenciaId).stream()
                .map(emprestimoResponseMapper::toElegivelResponse)
                .toList();
        return ResponseEntity.ok(candidatos);
    }

    @PatchMapping("/{emprestimoId}/enviar-candidatos")
    @PreAuthorize(PERFIS_SETORIAL)
    @Operation(summary = "Setorial seleciona candidatos e os envia à Montagem (RF-043/044, RN-MOVIMENTO-05)")
    public ResponseEntity<EmprestimoResponse> enviarCandidatos(
            @PathVariable UUID emprestimoId, @Valid @RequestBody SelecionarCandidatosRequest request) {
        Emprestimo emprestimo = selecionarEEnviarCandidatosUseCase.execute(emprestimoId, request.pessoaIds());
        return ResponseEntity.ok(toResponse(emprestimo));
    }

    @PatchMapping("/{emprestimoId}/efetivar")
    @PreAuthorize(PERFIS_MONTAGEM)
    @Operation(summary = "Montagem efetiva os candidatos sugeridos, adicionando-os ao roster da equipe (RF-045, RN-18)")
    public ResponseEntity<EmprestimoResponse> efetivar(@PathVariable UUID emprestimoId) {
        Emprestimo emprestimo = efetivarEmprestimoUseCase.execute(emprestimoId);
        return ResponseEntity.ok(toResponse(emprestimo));
    }

    @PatchMapping("/{emprestimoId}/recusar")
    @PreAuthorize(PERFIS_SETORIAL)
    @Operation(summary = "Setorial recusa uma solicitação de empréstimo ainda sem candidatos enviados (RF-046)")
    public ResponseEntity<EmprestimoResponse> recusar(
            @PathVariable UUID emprestimoId, @Valid @RequestBody RecusarEmprestimoRequest request) {
        Emprestimo emprestimo = recusarEmprestimoUseCase.execute(emprestimoId, request.justificativa());
        return ResponseEntity.ok(toResponse(emprestimo));
    }

    private EmprestimoResponse toResponse(Emprestimo emprestimo) {
        var candidatos = listarCandidatosDoEmprestimoUseCase.execute(emprestimo.getId());
        return emprestimoResponseMapper.toResponse(emprestimo, candidatos);
    }

    private EscopoTerritorial resolverEscopo(Jwt jwt, Authentication authentication) {
        List<String> authorities =
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        List<Perfil> perfis = Perfil.fromAuthorities(authorities);
        return escopoTerritorialResolver.resolve(
                perfis,
                jwt.getClaimAsString("diocese_id"),
                jwt.getClaimAsString("setor_id"),
                jwt.getClaimAsString("paroquia_id"));
    }
}
