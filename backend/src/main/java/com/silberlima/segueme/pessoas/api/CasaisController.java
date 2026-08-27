package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.pessoas.api.dto.CasalResponse;
import com.silberlima.segueme.pessoas.api.dto.CriarCasalRequest;
import com.silberlima.segueme.pessoas.application.ListarCasaisUseCase;
import com.silberlima.segueme.pessoas.application.VincularCasalUseCase;
import com.silberlima.segueme.pessoas.domain.Casal;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/** Vínculos de casal entre pessoas do cadastro nacional (RF-012, RN-PESSOA-03/RN-03). */
@RestController
@RequestMapping("/api/v1/casais")
@Tag(name = "Casais", description = "Vínculos de casal entre pessoas do cadastro nacional (RF-012)")
public class CasaisController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO')";

    private final ListarCasaisUseCase listarCasaisUseCase;
    private final VincularCasalUseCase vincularCasalUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final CasalResponseMapper casalResponseMapper;

    public CasaisController(
            ListarCasaisUseCase listarCasaisUseCase,
            VincularCasalUseCase vincularCasalUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            CasalResponseMapper casalResponseMapper) {
        this.listarCasaisUseCase = listarCasaisUseCase;
        this.vincularCasalUseCase = vincularCasalUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.casalResponseMapper = casalResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista os casais do escopo territorial do usuário (RF-012)")
    public ResponseEntity<PageResponse<CasalResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarCasaisUseCase.execute(escopo, pageable).map(casalResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PostMapping
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Vincula duas pessoas como casal (RF-012, RN-PESSOA-03/RN-03)")
    public ResponseEntity<CasalResponse> vincular(@Valid @RequestBody CriarCasalRequest request) {
        Casal casal = vincularCasalUseCase.execute(request.pessoa1Id(), request.pessoa2Id(), request.dataVinculo());
        return ResponseEntity.status(HttpStatus.CREATED).body(casalResponseMapper.toResponse(casal));
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
