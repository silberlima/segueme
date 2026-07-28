package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.movimentacao.api.dto.SolicitacaoResponse;
import com.silberlima.segueme.movimentacao.application.ListarSolicitacoesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Inbox único do Setorial (RF-046, `transferencias-e-emprestimos.md` 2.3): Transferências e
 * Empréstimos pendentes de decisão, combinados. Somente leitura — as ações (Autorizar/Aprovar,
 * Recusar) usam diretamente os endpoints de {@link TransferenciasController}/
 * {@link EmprestimosController}, já que Solicitação não é um agregado próprio.
 */
@RestController
@RequestMapping("/api/v1/solicitacoes")
@Tag(name = "Solicitações", description = "Inbox único do Setorial: Transferências e Empréstimos pendentes de decisão (RF-046)")
public class SolicitacoesController {

    private static final String PERFIS_SETORIAL = "hasAnyRole('SETORIAL')";

    private final ListarSolicitacoesUseCase listarSolicitacoesUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final SolicitacaoResponseMapper solicitacaoResponseMapper;

    public SolicitacoesController(
            ListarSolicitacoesUseCase listarSolicitacoesUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            SolicitacaoResponseMapper solicitacaoResponseMapper) {
        this.listarSolicitacoesUseCase = listarSolicitacoesUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.solicitacaoResponseMapper = solicitacaoResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_SETORIAL)
    @Operation(summary = "Lista o inbox combinado de Transferências e Empréstimos pendentes do setor (RF-046)")
    public ResponseEntity<List<SolicitacaoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        List<SolicitacaoResponse> solicitacoes = listarSolicitacoesUseCase.execute(escopo).stream()
                .map(solicitacaoResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(solicitacoes);
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
