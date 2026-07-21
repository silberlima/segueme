package com.silberlima.segueme.hierarquia.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.api.dto.DioceseResponse;
import com.silberlima.segueme.hierarquia.application.ListarDiocesesUseCase;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/dioceses")
@Tag(name = "Hierarquia Territorial", description = "Estrutura Nacional/Diocese/Setor/Paróquia (RF-054, RN-04)")
public class DiocesesController {

    private final ListarDiocesesUseCase listarDiocesesUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final DioceseResponseMapper dioceseResponseMapper;

    public DiocesesController(
            ListarDiocesesUseCase listarDiocesesUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            DioceseResponseMapper dioceseResponseMapper) {
        this.listarDiocesesUseCase = listarDiocesesUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.dioceseResponseMapper = dioceseResponseMapper;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('NACIONAL','DIOCESE')")
    @Operation(summary = "Lista a estrutura hierárquica Nacional → Diocese → Setor → Paróquia, escopada ao usuário autenticado")
    public ResponseEntity<PageResponse<DioceseResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        List<Perfil> perfis = Perfil.fromAuthorities(authorities);
        EscopoTerritorial escopo = escopoTerritorialResolver.resolve(
                perfis,
                jwt.getClaimAsString("diocese_id"),
                jwt.getClaimAsString("setor_id"),
                jwt.getClaimAsString("paroquia_id"));
        var pagina = listarDiocesesUseCase.execute(escopo, pageable).map(dioceseResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }
}
