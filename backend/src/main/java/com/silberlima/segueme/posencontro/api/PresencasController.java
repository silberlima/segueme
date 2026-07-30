package com.silberlima.segueme.posencontro.api;

import com.silberlima.segueme.posencontro.api.dto.PresencaResponse;
import com.silberlima.segueme.posencontro.api.dto.RegistrarPresencasRequest;
import com.silberlima.segueme.posencontro.application.ConsultarPresencasUseCase;
import com.silberlima.segueme.posencontro.application.RegistrarPresencasUseCase;
import com.silberlima.segueme.posencontro.application.dto.PresencaRegistro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Lista de chamada de um encontro pós (RF-058, UC-20). O registro é um {@code PUT} em lote e
 * idempotente: reenviar a mesma chamada atualiza as marcações existentes em vez de duplicá-las
 * (RN-POS-01).
 */
@RestController
@RequestMapping("/api/v1/encontros-pos/{encontroPosId}/presencas")
@Tag(name = "Pós-Encontro — Presenças", description = "Registro de presença dos jovens nos encontros pós (RF-058, RN-POS-01)")
public class PresencasController {

    private static final String PERFIS_REGISTRO = "hasAnyRole('POS_ENCONTRO','PAROQUIA')";
    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_MONTAGEM','POS_ENCONTRO')";

    private final ConsultarPresencasUseCase consultarPresencasUseCase;
    private final RegistrarPresencasUseCase registrarPresencasUseCase;
    private final PosEncontroResponseMapper posEncontroResponseMapper;

    public PresencasController(
            ConsultarPresencasUseCase consultarPresencasUseCase,
            RegistrarPresencasUseCase registrarPresencasUseCase,
            PosEncontroResponseMapper posEncontroResponseMapper) {
        this.consultarPresencasUseCase = consultarPresencasUseCase;
        this.registrarPresencasUseCase = registrarPresencasUseCase;
        this.posEncontroResponseMapper = posEncontroResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontroPos(#encontroPosId)")
    @Operation(summary = "Lista de chamada: todos os acompanhados da paróquia e a marcação já registrada de cada um")
    public List<PresencaResponse> listar(@PathVariable UUID encontroPosId) {
        return consultarPresencasUseCase.execute(encontroPosId).stream()
                .map(posEncontroResponseMapper::toResponse)
                .toList();
    }

    @PutMapping
    @PreAuthorize(PERFIS_REGISTRO + " and @escopoTerritorial.podeVerEncontroPos(#encontroPosId)")
    @Operation(summary = "Registra em lote a presença dos jovens no encontro pós (UC — Registrar presença)")
    public List<PresencaResponse> registrar(
            @PathVariable UUID encontroPosId, @Valid @RequestBody RegistrarPresencasRequest request) {
        List<PresencaRegistro> registros = request.presencas().stream()
                .map(item -> new PresencaRegistro(item.pessoaId(), item.presente()))
                .toList();
        return registrarPresencasUseCase.execute(encontroPosId, registros).stream()
                .map(posEncontroResponseMapper::toResponse)
                .toList();
    }
}
