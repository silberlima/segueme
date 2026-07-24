package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.inscricoes.api.dto.CriarPreInscricaoRequest;
import com.silberlima.segueme.inscricoes.api.dto.CriarPreInscricaoResponse;
import com.silberlima.segueme.inscricoes.api.dto.FichaInscricaoResponse;
import com.silberlima.segueme.inscricoes.api.dto.PreInscricaoResponse;
import com.silberlima.segueme.inscricoes.application.ConsultarPreInscricaoUseCase;
import com.silberlima.segueme.inscricoes.application.ListarPreInscricoesUseCase;
import com.silberlima.segueme.inscricoes.application.MarcarPreInscricaoEmAnaliseUseCase;
import com.silberlima.segueme.inscricoes.application.PreInscreverUseCase;
import com.silberlima.segueme.inscricoes.application.PreencherFichaUseCase;
import com.silberlima.segueme.inscricoes.application.dto.DadosNovaPreInscricao;
import com.silberlima.segueme.inscricoes.application.dto.ResultadoPreInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
 * Pré-inscrição e Ficha de Inscrição de candidatos a um Encontro (RF-022 a RF-024,
 * RN-INSCRICAO-01 a 03). Escrita restrita à Equipe Fichas (RN-INSCRICAO-01); leitura liberada
 * também a Montagem e Paróquia, que acessam a tela como consulta (`inscricoes.md` 2.1).
 */
@RestController
@RequestMapping("/api/v1/pre-inscricoes")
@Tag(name = "Pré-inscrições", description = "Funil de inscrição de um Encontro (RF-022 a RF-024)")
public class PreInscricoesController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('EQUIPE_FICHAS','EQUIPE_MONTAGEM','PAROQUIA')";

    private final PreInscreverUseCase preInscreverUseCase;
    private final MarcarPreInscricaoEmAnaliseUseCase marcarPreInscricaoEmAnaliseUseCase;
    private final PreencherFichaUseCase preencherFichaUseCase;
    private final ListarPreInscricoesUseCase listarPreInscricoesUseCase;
    private final ConsultarPreInscricaoUseCase consultarPreInscricaoUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final PreInscricaoResponseMapper preInscricaoResponseMapper;
    private final CadastroReservaResponseMapper cadastroReservaResponseMapper;

    public PreInscricoesController(
            PreInscreverUseCase preInscreverUseCase,
            MarcarPreInscricaoEmAnaliseUseCase marcarPreInscricaoEmAnaliseUseCase,
            PreencherFichaUseCase preencherFichaUseCase,
            ListarPreInscricoesUseCase listarPreInscricoesUseCase,
            ConsultarPreInscricaoUseCase consultarPreInscricaoUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            PreInscricaoResponseMapper preInscricaoResponseMapper,
            CadastroReservaResponseMapper cadastroReservaResponseMapper) {
        this.preInscreverUseCase = preInscreverUseCase;
        this.marcarPreInscricaoEmAnaliseUseCase = marcarPreInscricaoEmAnaliseUseCase;
        this.preencherFichaUseCase = preencherFichaUseCase;
        this.listarPreInscricoesUseCase = listarPreInscricoesUseCase;
        this.consultarPreInscricaoUseCase = consultarPreInscricaoUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.preInscricaoResponseMapper = preInscricaoResponseMapper;
        this.cadastroReservaResponseMapper = cadastroReservaResponseMapper;
    }

    @GetMapping("/{id}")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Consulta uma Pré-inscrição por id, usada pela tela de Ficha de Inscrição")
    public ResponseEntity<PreInscricaoResponse> consultar(@PathVariable UUID id) {
        PreInscricao preInscricao = consultarPreInscricaoUseCase.execute(id);
        return ResponseEntity.ok(preInscricaoResponseMapper.toResponse(preInscricao));
    }

    @PostMapping
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Pré-inscreve uma Pessoa num Encontro; entra em reserva se as vagas estiverem esgotadas (RF-022, RN-INSCRICAO-03)")
    public ResponseEntity<CriarPreInscricaoResponse> criar(@Valid @RequestBody CriarPreInscricaoRequest request) {
        ResultadoPreInscricao resultado =
                preInscreverUseCase.execute(new DadosNovaPreInscricao(request.pessoaId(), request.encontroId()));
        CriarPreInscricaoResponse response = resultado.entrouEmReserva()
                ? new CriarPreInscricaoResponse(
                        true, null, cadastroReservaResponseMapper.toResponse(resultado.cadastroReserva(), 0))
                : new CriarPreInscricaoResponse(
                        false, preInscricaoResponseMapper.toResponse(resultado.preInscricao()), null);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista Pré-inscrições do escopo territorial do usuário, com busca por encontro/status/nome (`inscricoes.md` 2.1)")
    public ResponseEntity<PageResponse<PreInscricaoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) UUID encontroId,
            @RequestParam(required = false) StatusPreInscricao status,
            @RequestParam(required = false) String nome,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarPreInscricoesUseCase
                .execute(escopo, encontroId, status, nome, pageable)
                .map(preInscricaoResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PatchMapping("/{id}/em-analise")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Marca uma Pré-inscrição como em análise enquanto a Equipe Fichas revisa o candidato")
    public ResponseEntity<PreInscricaoResponse> marcarEmAnalise(@PathVariable UUID id) {
        PreInscricao preInscricao = marcarPreInscricaoEmAnaliseUseCase.execute(id);
        return ResponseEntity.ok(preInscricaoResponseMapper.toResponse(preInscricao));
    }

    @PostMapping("/{id}/ficha")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Preenche/confirma a Ficha de Inscrição, promovendo a pessoa a Seguimista (RF-023/024, RN-INSCRICAO-02)")
    public ResponseEntity<FichaInscricaoResponse> preencherFicha(@PathVariable UUID id) {
        FichaInscricao ficha = preencherFichaUseCase.execute(id);
        return ResponseEntity.status(HttpStatus.CREATED).body(preInscricaoResponseMapper.toResponse(ficha));
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
