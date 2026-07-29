package com.silberlima.segueme.hierarquia.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.hierarquia.api.dto.ParoquiaResponse;
import com.silberlima.segueme.hierarquia.application.ListarParoquiasUseCase;
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
@RequestMapping("/api/v1/paroquias")
@Tag(name = "Hierarquia Territorial", description = "Paróquias do escopo territorial do usuário (RF-055, RN-04)")
public class ParoquiasController {

    private final ListarParoquiasUseCase listarParoquiasUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final ParoquiaResponseMapper paroquiaResponseMapper;

    public ParoquiasController(
            ListarParoquiasUseCase listarParoquiasUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            ParoquiaResponseMapper paroquiaResponseMapper) {
        this.listarParoquiasUseCase = listarParoquiasUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.paroquiaResponseMapper = paroquiaResponseMapper;
    }

    // PAROQUIA e EQUIPE_FICHAS incluídos a partir da Fase 3 (Pessoas): o formulário de
    // Cadastro de Pessoa usa este endpoint para o seletor de paróquia, e são exatamente os
    // perfis autorizados a criar pessoa (PessoasController). O escopo de cada um já era
    // resolvido corretamente pelo EscopoTerritorialResolver — só a role estava faltando aqui.
    // POS_ENCONTRO e EQUIPE_MONTAGEM incluídos a partir da Fase 11 (Pós-Encontro), pelo mesmo
    // motivo: as telas Presenças/Frequência/Prioridades recortam o acompanhamento por paróquia e
    // usam este endpoint no seletor — sem a role, o perfil dono do módulo não carregava a própria tela.
    @GetMapping
    @PreAuthorize("hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FICHAS','POS_ENCONTRO','EQUIPE_MONTAGEM')")
    @Operation(summary = "Lista as paróquias do escopo territorial do usuário autenticado")
    public ResponseEntity<PageResponse<ParoquiaResponse>> listar(
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
        var pagina = listarParoquiasUseCase.execute(escopo, pageable).map(paroquiaResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }
}
