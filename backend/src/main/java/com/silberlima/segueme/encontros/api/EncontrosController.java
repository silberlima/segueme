package com.silberlima.segueme.encontros.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.encontros.api.dto.CriarEncontroRequest;
import com.silberlima.segueme.encontros.api.dto.EncontroResponse;
import com.silberlima.segueme.encontros.api.dto.EncontroResumoResponse;
import com.silberlima.segueme.encontros.application.ConsultarEncontroUseCase;
import com.silberlima.segueme.encontros.application.CriarEncontroUseCase;
import com.silberlima.segueme.encontros.application.ListarEncontrosUseCase;
import com.silberlima.segueme.encontros.application.dto.DadosNovoEncontro;
import com.silberlima.segueme.encontros.application.dto.EncontroDetalhado;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.StatusEncontro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Criação e consulta de Encontros (RF-014 a RF-021, RN-16, RN-20, RN-21). Escrita restrita ao
 * perfil Paróquia (RF-015: "Quem acessa: perfil Paróquia"); leitura liberada a Paróquia e
 * superiores ("perfis com visão de encontro" — `gerenciamento-de-encontros.md`).
 */
@RestController
@RequestMapping("/api/v1/encontros")
@Tag(name = "Encontros", description = "Criação e consulta de Encontros (RF-014 a RF-021, RN-16, RN-20, RN-21)")
public class EncontrosController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('PAROQUIA')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA')";

    private final CriarEncontroUseCase criarEncontroUseCase;
    private final ListarEncontrosUseCase listarEncontrosUseCase;
    private final ConsultarEncontroUseCase consultarEncontroUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final EncontroResponseMapper encontroResponseMapper;

    public EncontrosController(
            CriarEncontroUseCase criarEncontroUseCase,
            ListarEncontrosUseCase listarEncontrosUseCase,
            ConsultarEncontroUseCase consultarEncontroUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            EncontroResponseMapper encontroResponseMapper) {
        this.criarEncontroUseCase = criarEncontroUseCase;
        this.listarEncontrosUseCase = listarEncontrosUseCase;
        this.consultarEncontroUseCase = consultarEncontroUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.encontroResponseMapper = encontroResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Cria um encontro via wizard de 5 passos, gerando Equipe Dirigente e Equipe Comando (RF-015 a RF-020)")
    public ResponseEntity<EncontroResponse> criar(@Valid @RequestBody CriarEncontroRequest request) {
        Encontro encontro = criarEncontroUseCase.execute(new DadosNovoEncontro(
                request.padroeiro(),
                request.diretorEspiritualId(),
                request.local(),
                request.dataInicio(),
                request.dataTermino(),
                request.vagas(),
                request.paroquiaId(),
                request.equipeDirigente().stream()
                        .map(membro -> new DadosNovoEncontro.MembroDirigente(
                                membro.pasta(), membro.tipoMembro(), membro.pessoaId(), membro.casalId()))
                        .toList(),
                request.equipeComando().coordenadorGeralId(),
                request.equipeComando().casalCoordenadorId()));
        EncontroDetalhado detalhado = consultarEncontroUseCase.execute(encontro.getId());
        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(detalhado));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Lista encontros do escopo territorial do usuário, com busca por número/status/paróquia (RF-014)")
    public ResponseEntity<PageResponse<EncontroResumoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            Authentication authentication,
            @RequestParam(required = false) Integer numero,
            @RequestParam(required = false) StatusEncontro status,
            @RequestParam(required = false) UUID paroquiaId,
            @PageableDefault(sort = "numero", direction = Sort.Direction.DESC) Pageable pageable) {
        EscopoTerritorial escopo = resolverEscopo(jwt, authentication);
        var pagina = listarEncontrosUseCase
                .execute(escopo, numero, status, paroquiaId, pageable)
                .map(encontroResponseMapper::toResumoResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @GetMapping("/{id}")
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#id)")
    @Operation(summary = "Exibe os Detalhes de um encontro, incluindo Equipe Dirigente e Equipe Comando (RF-021)")
    public ResponseEntity<EncontroResponse> consultar(@PathVariable UUID id) {
        EncontroDetalhado detalhado = consultarEncontroUseCase.execute(id);
        return ResponseEntity.ok(toResponse(detalhado));
    }

    private EncontroResponse toResponse(EncontroDetalhado detalhado) {
        return encontroResponseMapper.toResponse(
                detalhado.encontro(), detalhado.membrosDirigente(), detalhado.equipeComando());
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
