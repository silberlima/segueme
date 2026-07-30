package com.silberlima.segueme.financeiro.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.financeiro.api.dto.LancamentoFinanceiroResponse;
import com.silberlima.segueme.financeiro.api.dto.RegistrarLancamentoRequest;
import com.silberlima.segueme.financeiro.application.AprovarDespesaUseCase;
import com.silberlima.segueme.financeiro.application.ListarLancamentosUseCase;
import com.silberlima.segueme.financeiro.application.PagarDespesaUseCase;
import com.silberlima.segueme.financeiro.application.RegistrarLancamentoUseCase;
import com.silberlima.segueme.financeiro.domain.SituacaoDespesa;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
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
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Receitas e despesas de um Encontro (RF-047/048/049, RN-FINANCEIRO-01/02). Registro restrito à
 * Equipe Finanças; aprovação/pagamento de despesa liberado também à Paróquia (pendência #12 de
 * {@code pendencias-gerais.md}).
 */
@RestController
@RequestMapping("/api/v1/lancamentos-financeiros")
@Tag(name = "Financeiro — Lançamentos", description = "Receitas e despesas de um Encontro (RF-047 a RF-049)")
public class LancamentosFinanceirosController {

    private static final String PERFIS_LANCAR = "hasAnyRole('EQUIPE_FINANCAS')";
    private static final String PERFIS_DECISAO = "hasAnyRole('EQUIPE_FINANCAS','PAROQUIA')";
    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FINANCAS')";

    private final RegistrarLancamentoUseCase registrarLancamentoUseCase;
    private final ListarLancamentosUseCase listarLancamentosUseCase;
    private final AprovarDespesaUseCase aprovarDespesaUseCase;
    private final PagarDespesaUseCase pagarDespesaUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final LancamentoFinanceiroResponseMapper lancamentoFinanceiroResponseMapper;

    public LancamentosFinanceirosController(
            RegistrarLancamentoUseCase registrarLancamentoUseCase,
            ListarLancamentosUseCase listarLancamentosUseCase,
            AprovarDespesaUseCase aprovarDespesaUseCase,
            PagarDespesaUseCase pagarDespesaUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            LancamentoFinanceiroResponseMapper lancamentoFinanceiroResponseMapper) {
        this.registrarLancamentoUseCase = registrarLancamentoUseCase;
        this.listarLancamentosUseCase = listarLancamentosUseCase;
        this.aprovarDespesaUseCase = aprovarDespesaUseCase;
        this.pagarDespesaUseCase = pagarDespesaUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.lancamentoFinanceiroResponseMapper = lancamentoFinanceiroResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_LANCAR + " and @escopoTerritorial.podeVerEncontro(#request.encontroId())")
    @Operation(summary = "Registra um lançamento (receita ou despesa) de um Encontro (RF-047)")
    public ResponseEntity<LancamentoFinanceiroResponse> registrar(@Valid @RequestBody RegistrarLancamentoRequest request) {
        var lancamento = registrarLancamentoUseCase.execute(
                request.encontroId(), request.tipo(), request.categoria(), request.valor(), request.data());
        return ResponseEntity.status(HttpStatus.CREATED).body(lancamentoFinanceiroResponseMapper.toResponse(lancamento));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista lançamentos financeiros do escopo territorial do usuário (RF-048/049)")
    public ResponseEntity<PageResponse<LancamentoFinanceiroResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) UUID encontroId,
            @RequestParam(required = false) TipoLancamento tipo,
            @RequestParam(required = false) SituacaoDespesa situacao,
            @PageableDefault Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarLancamentosUseCase.execute(escopo, encontroId, tipo, situacao, pageable)
                .map(lancamentoFinanceiroResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PatchMapping("/{lancamentoId}/aprovar")
    @PreAuthorize(PERFIS_DECISAO)
    @Operation(summary = "Aprova uma despesa pendente (RN-FINANCEIRO-02)")
    public ResponseEntity<LancamentoFinanceiroResponse> aprovar(
            @AuthenticationPrincipal Jwt jwt, @PathVariable UUID lancamentoId) {
        var lancamento = aprovarDespesaUseCase.execute(lancamentoId, jwt.getSubject());
        return ResponseEntity.ok(lancamentoFinanceiroResponseMapper.toResponse(lancamento));
    }

    @PatchMapping("/{lancamentoId}/pagar")
    @PreAuthorize(PERFIS_DECISAO)
    @Operation(summary = "Marca uma despesa aprovada como paga (RN-FINANCEIRO-02)")
    public ResponseEntity<LancamentoFinanceiroResponse> pagar(@PathVariable UUID lancamentoId) {
        var lancamento = pagarDespesaUseCase.execute(lancamentoId);
        return ResponseEntity.ok(lancamentoFinanceiroResponseMapper.toResponse(lancamento));
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
