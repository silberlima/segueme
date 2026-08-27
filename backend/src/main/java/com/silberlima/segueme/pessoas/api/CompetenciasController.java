package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.pessoas.api.dto.CompetenciaResponse;
import com.silberlima.segueme.pessoas.api.dto.PessoaResumoResponse;
import com.silberlima.segueme.pessoas.application.ListarCompetenciasUseCase;
import com.silberlima.segueme.pessoas.application.ListarPessoasPorCompetenciaUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Catálogo nacional de competências/dons (RF-013), apoio à composição de equipes na Fase 6. */
@RestController
@RequestMapping("/api/v1/competencias")
@Tag(name = "Competências", description = "Catálogo nacional de dons/habilidades (RF-013)")
public class CompetenciasController {

    private static final String PERFIS_LEITURA = "hasAnyRole('DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO')";

    private final ListarCompetenciasUseCase listarCompetenciasUseCase;
    private final ListarPessoasPorCompetenciaUseCase listarPessoasPorCompetenciaUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final CompetenciaResponseMapper competenciaResponseMapper;
    private final PessoaResponseMapper pessoaResponseMapper;

    public CompetenciasController(
            ListarCompetenciasUseCase listarCompetenciasUseCase,
            ListarPessoasPorCompetenciaUseCase listarPessoasPorCompetenciaUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            CompetenciaResponseMapper competenciaResponseMapper,
            PessoaResponseMapper pessoaResponseMapper) {
        this.listarCompetenciasUseCase = listarCompetenciasUseCase;
        this.listarPessoasPorCompetenciaUseCase = listarPessoasPorCompetenciaUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.competenciaResponseMapper = competenciaResponseMapper;
        this.pessoaResponseMapper = pessoaResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista o catálogo nacional de competências com a contagem de pessoas de cada uma (RF-013)")
    public ResponseEntity<List<CompetenciaResponse>> listar() {
        List<CompetenciaResponse> competencias = listarCompetenciasUseCase.execute().stream()
                .map(competenciaResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(competencias);
    }

    @GetMapping("/{id}/pessoas")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista as pessoas do escopo territorial do usuário que possuem a competência (RF-013)")
    public ResponseEntity<PageResponse<PessoaResumoResponse>> pessoasPorCompetencia(
            @PathVariable UUID id,
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @PageableDefault(sort = "nomeCompleto", direction = Sort.Direction.ASC) Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarPessoasPorCompetenciaUseCase
                .execute(escopo, id, pageable)
                .map(pessoaResponseMapper::toResumoResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
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
