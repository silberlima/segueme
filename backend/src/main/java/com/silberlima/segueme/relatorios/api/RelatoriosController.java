package com.silberlima.segueme.relatorios.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.relatorios.api.dto.RelatorioResponse;
import com.silberlima.segueme.relatorios.api.dto.SolicitarRelatorioRequest;
import com.silberlima.segueme.relatorios.api.dto.TipoRelatorioResponse;
import com.silberlima.segueme.relatorios.application.BaixarRelatorioUseCase;
import com.silberlima.segueme.relatorios.application.ListarRelatoriosUseCase;
import com.silberlima.segueme.relatorios.application.ListarTiposDeRelatorioUseCase;
import com.silberlima.segueme.relatorios.application.SolicitarRelatorioUseCase;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Geração de relatórios (RF-056, UC-22). O escopo territorial do relatório vem sempre das claims
 * do solicitante (RN-04) e nunca de parâmetro, como nos painéis da Fase 12 — não há como pedir o
 * relatório de outra diocese, setor ou paróquia.
 *
 * <p>O POST responde {@code 202 Accepted} com o relatório ainda {@code PENDENTE}: a geração roda
 * fora da request (skills/10) e a tela acompanha a lista até o arquivo ficar pronto.
 */
@RestController
@RequestMapping("/api/v1/relatorios")
@Tag(name = "Relatórios", description = "Geração de relatórios por tipo (RF-056, UC-22)")
public class RelatoriosController {

    private final SolicitarRelatorioUseCase solicitarRelatorioUseCase;
    private final ListarRelatoriosUseCase listarRelatoriosUseCase;
    private final ListarTiposDeRelatorioUseCase listarTiposDeRelatorioUseCase;
    private final BaixarRelatorioUseCase baixarRelatorioUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final RelatorioResponseMapper relatorioResponseMapper;

    public RelatoriosController(
            SolicitarRelatorioUseCase solicitarRelatorioUseCase,
            ListarRelatoriosUseCase listarRelatoriosUseCase,
            ListarTiposDeRelatorioUseCase listarTiposDeRelatorioUseCase,
            BaixarRelatorioUseCase baixarRelatorioUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            RelatorioResponseMapper relatorioResponseMapper) {
        this.solicitarRelatorioUseCase = solicitarRelatorioUseCase;
        this.listarRelatoriosUseCase = listarRelatoriosUseCase;
        this.listarTiposDeRelatorioUseCase = listarTiposDeRelatorioUseCase;
        this.baixarRelatorioUseCase = baixarRelatorioUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.relatorioResponseMapper = relatorioResponseMapper;
    }

    @GetMapping("/tipos")
    @Operation(summary = "Tipos de relatório que o perfil do usuário autenticado pode gerar (RF-056)")
    public ResponseEntity<List<TipoRelatorioResponse>> tipos(Authentication authentication) {
        List<TipoRelatorioResponse> tipos = listarTiposDeRelatorioUseCase.execute(perfis(authentication)).stream()
                .map(TipoRelatorioResponse::from)
                .toList();
        return ResponseEntity.ok(tipos);
    }

    @PostMapping
    @Operation(summary = "Solicita a geração de um relatório; a geração é assíncrona (UC-22)")
    public ResponseEntity<RelatorioResponse> solicitar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @Valid @RequestBody SolicitarRelatorioRequest request) {
        Relatorio relatorio = solicitarRelatorioUseCase.execute(
                request.tipo(), jwt.getSubject(), perfis(authentication), escopo(jwt, authentication));
        return ResponseEntity.accepted().body(relatorioResponseMapper.toResponse(relatorio));
    }

    @GetMapping
    @Operation(summary = "Relatórios gerados recentemente pelo usuário autenticado")
    public ResponseEntity<PageResponse<RelatorioResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var pagina = listarRelatoriosUseCase.execute(jwt.getSubject(), pageable)
                .map(relatorioResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @GetMapping("/{id}/conteudo")
    @Operation(summary = "Baixa o arquivo de um relatório concluído do próprio usuário")
    public ResponseEntity<byte[]> baixar(@PathVariable UUID id, @AuthenticationPrincipal Jwt jwt) {
        Relatorio relatorio = baixarRelatorioUseCase.execute(id, jwt.getSubject());
        return ResponseEntity.ok()
                .contentType(new MediaType("text", "csv", StandardCharsets.UTF_8))
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + relatorio.getTipo().nomeDoArquivo() + "\"")
                .body(relatorio.getConteudo().getBytes(StandardCharsets.UTF_8));
    }

    private List<Perfil> perfis(Authentication authentication) {
        return Perfil.fromAuthorities(authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList());
    }

    private EscopoTerritorial escopo(Jwt jwt, Authentication authentication) {
        return escopoTerritorialResolver.resolve(
                perfis(authentication),
                jwt.getClaimAsString("diocese_id"),
                jwt.getClaimAsString("setor_id"),
                jwt.getClaimAsString("paroquia_id"));
    }
}
