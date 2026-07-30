package com.silberlima.segueme.posencontro.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.posencontro.api.dto.AgendarEncontroPosRequest;
import com.silberlima.segueme.posencontro.api.dto.EncontroPosResponse;
import com.silberlima.segueme.posencontro.application.AgendarEncontroPosUseCase;
import com.silberlima.segueme.posencontro.application.ListarEncontrosPosUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
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
 * Agenda dos encontros pós de uma paróquia (RN-POS-01). Quem conduz o acompanhamento é o perfil
 * Pós-Encontro, com a Paróquia também podendo agendar; a leitura alcança ainda a Equipe Montagem,
 * que usa a frequência como fator de sugestão (Fase 6), e os níveis de supervisão territorial.
 */
@RestController
@RequestMapping("/api/v1/encontros-pos")
@Tag(name = "Pós-Encontro — Encontros pós", description = "Agenda dos encontros pós-encontro da paróquia (RF-058, RN-POS-01)")
public class EncontrosPosController {

    private static final String PERFIS_GESTAO = "hasAnyRole('POS_ENCONTRO','PAROQUIA')";
    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_MONTAGEM','POS_ENCONTRO')";

    private final AgendarEncontroPosUseCase agendarEncontroPosUseCase;
    private final ListarEncontrosPosUseCase listarEncontrosPosUseCase;
    private final PosEncontroResponseMapper posEncontroResponseMapper;

    public EncontrosPosController(
            AgendarEncontroPosUseCase agendarEncontroPosUseCase,
            ListarEncontrosPosUseCase listarEncontrosPosUseCase,
            PosEncontroResponseMapper posEncontroResponseMapper) {
        this.agendarEncontroPosUseCase = agendarEncontroPosUseCase;
        this.listarEncontrosPosUseCase = listarEncontrosPosUseCase;
        this.posEncontroResponseMapper = posEncontroResponseMapper;
    }

    @PostMapping
    @PreAuthorize(PERFIS_GESTAO + " and @escopoTerritorial.podeVerParoquia(#request.paroquiaId())")
    @Operation(summary = "Agenda um encontro pós na paróquia, numa data")
    public ResponseEntity<EncontroPosResponse> agendar(@Valid @RequestBody AgendarEncontroPosRequest request) {
        var encontroPos = agendarEncontroPosUseCase.execute(request.paroquiaId(), request.data(), request.tema());
        return ResponseEntity.status(HttpStatus.CREATED).body(posEncontroResponseMapper.toResponse(encontroPos));
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerParoquia(#paroquiaId)")
    @Operation(summary = "Lista os encontros pós de uma paróquia, do mais recente para o mais antigo")
    public ResponseEntity<PageResponse<EncontroPosResponse>> listar(
            @RequestParam UUID paroquiaId, @PageableDefault Pageable pageable) {
        var pagina = listarEncontrosPosUseCase.execute(paroquiaId, pageable).map(posEncontroResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }
}
