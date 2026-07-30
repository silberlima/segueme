package com.silberlima.segueme.convites.api;

import com.silberlima.segueme.convites.api.dto.ConviteResponse;
import com.silberlima.segueme.convites.api.dto.EmitirConviteRequest;
import com.silberlima.segueme.convites.api.dto.RecusarConviteRequest;
import com.silberlima.segueme.convites.application.AceitarConviteUseCase;
import com.silberlima.segueme.convites.application.EmitirConviteUseCase;
import com.silberlima.segueme.convites.application.ListarConvitesUseCase;
import com.silberlima.segueme.convites.application.RecusarConviteUseCase;
import com.silberlima.segueme.convites.application.ReenviarConviteUseCase;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Ciclo de convite de uma equipe de serviço (RF-034 a RF-039): emissão, aceite, recusa e
 * reenvio. Leitura liberada a todos os 10 perfis; emissão/aceite/recusa/reenvio restritos à
 * Equipe Comando (RN-CONVITE-01/04) — ver limitação de identidade documentada em
 * {@code docs/pendencias-gerais.md}: estas ações são mediadas por staff, não pela própria pessoa
 * convidada.
 */
@RestController
@RequestMapping("/api/v1/convites")
@Tag(name = "Convites", description = "Ciclo de convite, aceite e recusa das equipes de serviço (RF-034 a RF-039, RN-CONVITE-01 a 04)")
public class ConvitesController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('EQUIPE_COMANDO')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO','SEGUIDOR')";

    private final ListarConvitesUseCase listarConvitesUseCase;
    private final EmitirConviteUseCase emitirConviteUseCase;
    private final AceitarConviteUseCase aceitarConviteUseCase;
    private final RecusarConviteUseCase recusarConviteUseCase;
    private final ReenviarConviteUseCase reenviarConviteUseCase;
    private final ConviteResponseMapper conviteResponseMapper;

    public ConvitesController(
            ListarConvitesUseCase listarConvitesUseCase,
            EmitirConviteUseCase emitirConviteUseCase,
            AceitarConviteUseCase aceitarConviteUseCase,
            RecusarConviteUseCase recusarConviteUseCase,
            ReenviarConviteUseCase reenviarConviteUseCase,
            ConviteResponseMapper conviteResponseMapper) {
        this.listarConvitesUseCase = listarConvitesUseCase;
        this.emitirConviteUseCase = emitirConviteUseCase;
        this.aceitarConviteUseCase = aceitarConviteUseCase;
        this.recusarConviteUseCase = recusarConviteUseCase;
        this.reenviarConviteUseCase = reenviarConviteUseCase;
        this.conviteResponseMapper = conviteResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista os convites de um Encontro, com filtros opcionais de status/equipe/papel (RF-036/037)")
    public ResponseEntity<List<ConviteResponse>> listar(
            @RequestParam UUID encontroId,
            @RequestParam(required = false) StatusIntegrante status,
            @RequestParam(required = false) UUID equipeId,
            @RequestParam(required = false) PapelIntegrante papel) {
        List<ConviteResponse> convites = listarConvitesUseCase.execute(encontroId, status, equipeId, papel).stream()
                .map(conviteResponseMapper::toResponse)
                .toList();
        return ResponseEntity.ok(convites);
    }

    @PostMapping
    @PreAuthorize(PERFIS_ESCRITA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Emite um convite de coordenador (RF-034) ou de integrante (RF-035)")
    public ResponseEntity<ConviteResponse> emitir(
            @RequestParam UUID encontroId, @Valid @RequestBody EmitirConviteRequest request) {
        IntegranteEquipe convite =
                emitirConviteUseCase.execute(encontroId, request.equipeId(), request.pessoaId(), request.papel());
        return ResponseEntity.status(HttpStatus.CREATED).body(conviteResponseMapper.toResponse(convite));
    }

    @PatchMapping("/{conviteId}/aceitar")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Registra o aceite de um convite (RF-036)")
    public ResponseEntity<ConviteResponse> aceitar(@PathVariable UUID conviteId) {
        return ResponseEntity.ok(conviteResponseMapper.toResponse(aceitarConviteUseCase.execute(conviteId)));
    }

    @PatchMapping("/{conviteId}/recusar")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Registra a recusa de um convite com justificativa obrigatória (RF-037, RN-CONVITE-02)")
    public ResponseEntity<ConviteResponse> recusar(
            @PathVariable UUID conviteId, @Valid @RequestBody RecusarConviteRequest request) {
        IntegranteEquipe convite = recusarConviteUseCase.execute(conviteId, request.justificativa());
        return ResponseEntity.ok(conviteResponseMapper.toResponse(convite));
    }

    @PatchMapping("/{conviteId}/reenviar")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Reenvia um convite pendente (RF-039)")
    public ResponseEntity<ConviteResponse> reenviar(@PathVariable UUID conviteId) {
        return ResponseEntity.ok(conviteResponseMapper.toResponse(reenviarConviteUseCase.execute(conviteId)));
    }
}
