package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.movimentacao.api.dto.RecusarTransferenciaRequest;
import com.silberlima.segueme.movimentacao.api.dto.SolicitarTransferenciaRequest;
import com.silberlima.segueme.movimentacao.api.dto.TransferenciaResponse;
import com.silberlima.segueme.movimentacao.application.AprovarTransferenciaUseCase;
import com.silberlima.segueme.movimentacao.application.ListarTransferenciasUseCase;
import com.silberlima.segueme.movimentacao.application.RecusarTransferenciaUseCase;
import com.silberlima.segueme.movimentacao.application.SolicitarTransferenciaUseCase;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
 * Transferência definitiva de vínculo entre paróquias (RF-040/041, RN-MOVIMENTO-01/RN-17).
 * Solicitação liberada aos perfis de Pessoas/Paróquia; aprovação/recusa restrita ao Setorial.
 */
@RestController
@RequestMapping("/api/v1/transferencias")
@Tag(name = "Transferências", description = "Mudança definitiva de vínculo de uma pessoa entre paróquias (RF-040/041, RN-MOVIMENTO-01)")
public class TransferenciasController {

    private static final String PERFIS_SOLICITAR = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FICHAS')";
    private static final String PERFIS_DECISAO = "hasAnyRole('SETORIAL')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO')";

    private final SolicitarTransferenciaUseCase solicitarTransferenciaUseCase;
    private final AprovarTransferenciaUseCase aprovarTransferenciaUseCase;
    private final RecusarTransferenciaUseCase recusarTransferenciaUseCase;
    private final ListarTransferenciasUseCase listarTransferenciasUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final TransferenciaResponseMapper transferenciaResponseMapper;

    public TransferenciasController(
            SolicitarTransferenciaUseCase solicitarTransferenciaUseCase,
            AprovarTransferenciaUseCase aprovarTransferenciaUseCase,
            RecusarTransferenciaUseCase recusarTransferenciaUseCase,
            ListarTransferenciasUseCase listarTransferenciasUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            TransferenciaResponseMapper transferenciaResponseMapper) {
        this.solicitarTransferenciaUseCase = solicitarTransferenciaUseCase;
        this.aprovarTransferenciaUseCase = aprovarTransferenciaUseCase;
        this.recusarTransferenciaUseCase = recusarTransferenciaUseCase;
        this.listarTransferenciasUseCase = listarTransferenciasUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.transferenciaResponseMapper = transferenciaResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_SOLICITAR)
    @Operation(summary = "Solicita a transferência definitiva de uma pessoa para outra paróquia (RF-040)")
    public ResponseEntity<TransferenciaResponse> solicitar(@Valid @RequestBody SolicitarTransferenciaRequest request) {
        var transferencia =
                solicitarTransferenciaUseCase.execute(request.pessoaId(), request.paroquiaDestinoId(), request.motivo());
        return ResponseEntity.status(HttpStatus.CREATED).body(transferenciaResponseMapper.toResponse(transferencia));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista Transferências do escopo territorial do usuário, com filtro opcional por status/pessoa (RF-041)")
    public ResponseEntity<PageResponse<TransferenciaResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) StatusTransferencia status,
            @RequestParam(required = false) UUID pessoaId,
            @PageableDefault Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarTransferenciasUseCase.execute(escopo, status, pessoaId, pageable)
                .map(transferenciaResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PatchMapping("/{transferenciaId}/aprovar")
    @PreAuthorize(PERFIS_DECISAO)
    @Operation(summary = "Aprova uma transferência pendente: efetiva a mudança de paróquia (RF-041, RN-MOVIMENTO-01)")
    public ResponseEntity<TransferenciaResponse> aprovar(@PathVariable UUID transferenciaId) {
        var transferencia = aprovarTransferenciaUseCase.execute(transferenciaId);
        return ResponseEntity.ok(transferenciaResponseMapper.toResponse(transferencia));
    }

    @PatchMapping("/{transferenciaId}/recusar")
    @PreAuthorize(PERFIS_DECISAO)
    @Operation(summary = "Recusa uma transferência pendente com justificativa obrigatória (RF-041)")
    public ResponseEntity<TransferenciaResponse> recusar(
            @PathVariable UUID transferenciaId, @Valid @RequestBody RecusarTransferenciaRequest request) {
        var transferencia = recusarTransferenciaUseCase.execute(transferenciaId, request.justificativa());
        return ResponseEntity.ok(transferenciaResponseMapper.toResponse(transferencia));
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
