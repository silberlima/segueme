package com.silberlima.segueme.montagem.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.montagem.api.dto.RegistrarSubstituicaoRequest;
import com.silberlima.segueme.montagem.api.dto.SubstituicaoResponse;
import com.silberlima.segueme.montagem.application.ListarSubstituicoesUseCase;
import com.silberlima.segueme.montagem.application.RegistrarSubstituicaoUseCase;
import com.silberlima.segueme.montagem.application.dto.DadosSubstituicao;
import com.silberlima.segueme.montagem.domain.Substituicao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Registro e histórico de substituições de integrantes de equipe (RF-032/033), restrito à
 * Equipe Montagem (RN-MONTAGEM-01/05/06).
 */
@RestController
@RequestMapping("/api/v1/substituicoes")
@Tag(name = "Substituições", description = "Registro e histórico de substituições de integrantes de equipe (RF-032/033)")
public class SubstituicoesController {

    private static final String PERFIS_MONTAGEM = "hasAnyRole('EQUIPE_MONTAGEM')";

    private final RegistrarSubstituicaoUseCase registrarSubstituicaoUseCase;
    private final ListarSubstituicoesUseCase listarSubstituicoesUseCase;
    private final MontagemResponseMapper montagemResponseMapper;

    public SubstituicoesController(
            RegistrarSubstituicaoUseCase registrarSubstituicaoUseCase,
            ListarSubstituicoesUseCase listarSubstituicoesUseCase,
            MontagemResponseMapper montagemResponseMapper) {
        this.registrarSubstituicaoUseCase = registrarSubstituicaoUseCase;
        this.listarSubstituicoesUseCase = listarSubstituicoesUseCase;
        this.montagemResponseMapper = montagemResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_MONTAGEM)
    @Operation(summary = "Histórico de substituições de um Encontro, opcionalmente filtrado por equipe (RF-033)")
    public ResponseEntity<PageResponse<SubstituicaoResponse>> listar(
            @RequestParam UUID encontroId,
            @RequestParam(required = false) UUID equipeId,
            @PageableDefault(sort = "dataSubstituicao", direction = Sort.Direction.DESC) Pageable pageable) {
        var pagina = listarSubstituicoesUseCase
                .execute(encontroId, equipeId, pageable)
                .map(montagemResponseMapper::toSubstituicaoResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PostMapping
    @PreAuthorize(PERFIS_MONTAGEM)
    @Operation(summary = "Registra a substituição de um integrante por outro, com justificativa obrigatória (RF-032, RN-MONTAGEM-05)")
    public ResponseEntity<SubstituicaoResponse> registrar(@Valid @RequestBody RegistrarSubstituicaoRequest request) {
        Substituicao substituicao = registrarSubstituicaoUseCase.execute(new DadosSubstituicao(
                request.encontroId(),
                request.equipeId(),
                request.pessoaSaiId(),
                request.motivoSaida(),
                request.pessoaEntraId(),
                request.justificativa()));
        return ResponseEntity.status(HttpStatus.CREATED).body(montagemResponseMapper.toSubstituicaoResponse(substituicao));
    }
}
