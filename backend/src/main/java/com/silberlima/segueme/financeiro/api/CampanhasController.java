package com.silberlima.segueme.financeiro.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.financeiro.api.dto.CampanhaResponse;
import com.silberlima.segueme.financeiro.api.dto.CriarCampanhaRequest;
import com.silberlima.segueme.financeiro.api.dto.RegistrarArrecadacaoRequest;
import com.silberlima.segueme.financeiro.application.CriarCampanhaUseCase;
import com.silberlima.segueme.financeiro.application.EncerrarCampanhaUseCase;
import com.silberlima.segueme.financeiro.application.ListarCampanhasUseCase;
import com.silberlima.segueme.financeiro.application.RegistrarArrecadacaoUseCase;
import com.silberlima.segueme.financeiro.domain.StatusCampanha;
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

/** Campanhas de arrecadação de um Encontro (RF-050, RN-FINANCEIRO-03). */
@RestController
@RequestMapping("/api/v1/campanhas")
@Tag(name = "Financeiro — Campanhas", description = "Campanhas de arrecadação de um Encontro (RF-050)")
public class CampanhasController {

    private static final String PERFIS_GESTAO = "hasAnyRole('EQUIPE_FINANCAS')";
    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FINANCAS')";

    private final CriarCampanhaUseCase criarCampanhaUseCase;
    private final ListarCampanhasUseCase listarCampanhasUseCase;
    private final RegistrarArrecadacaoUseCase registrarArrecadacaoUseCase;
    private final EncerrarCampanhaUseCase encerrarCampanhaUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final CampanhaResponseMapper campanhaResponseMapper;

    public CampanhasController(
            CriarCampanhaUseCase criarCampanhaUseCase,
            ListarCampanhasUseCase listarCampanhasUseCase,
            RegistrarArrecadacaoUseCase registrarArrecadacaoUseCase,
            EncerrarCampanhaUseCase encerrarCampanhaUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            CampanhaResponseMapper campanhaResponseMapper) {
        this.criarCampanhaUseCase = criarCampanhaUseCase;
        this.listarCampanhasUseCase = listarCampanhasUseCase;
        this.registrarArrecadacaoUseCase = registrarArrecadacaoUseCase;
        this.encerrarCampanhaUseCase = encerrarCampanhaUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.campanhaResponseMapper = campanhaResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_GESTAO)
    @Operation(summary = "Cria uma campanha de arrecadação de um Encontro (RF-050)")
    public ResponseEntity<CampanhaResponse> criar(@Valid @RequestBody CriarCampanhaRequest request) {
        var campanha = criarCampanhaUseCase.execute(request.encontroId(), request.nome(), request.meta());
        return ResponseEntity.status(HttpStatus.CREATED).body(campanhaResponseMapper.toResponse(campanha));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista campanhas do escopo territorial do usuário (RF-050)")
    public ResponseEntity<PageResponse<CampanhaResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) UUID encontroId,
            @RequestParam(required = false) StatusCampanha status,
            @PageableDefault Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarCampanhasUseCase.execute(escopo, encontroId, status, pageable)
                .map(campanhaResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PatchMapping("/{campanhaId}/arrecadacao")
    @PreAuthorize(PERFIS_GESTAO)
    @Operation(summary = "Registra um valor arrecadado numa campanha ativa")
    public ResponseEntity<CampanhaResponse> registrarArrecadacao(
            @PathVariable UUID campanhaId, @Valid @RequestBody RegistrarArrecadacaoRequest request) {
        var campanha = registrarArrecadacaoUseCase.execute(campanhaId, request.valor());
        return ResponseEntity.ok(campanhaResponseMapper.toResponse(campanha));
    }

    @PatchMapping("/{campanhaId}/encerrar")
    @PreAuthorize(PERFIS_GESTAO)
    @Operation(summary = "Encerra uma campanha, preservando o resultado final")
    public ResponseEntity<CampanhaResponse> encerrar(@PathVariable UUID campanhaId) {
        var campanha = encerrarCampanhaUseCase.execute(campanhaId);
        return ResponseEntity.ok(campanhaResponseMapper.toResponse(campanha));
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
