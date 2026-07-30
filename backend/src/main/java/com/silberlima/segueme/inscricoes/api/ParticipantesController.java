package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.inscricoes.api.dto.AtualizarPagamentoRequest;
import com.silberlima.segueme.inscricoes.api.dto.FunilResponse;
import com.silberlima.segueme.inscricoes.api.dto.ParticipanteResponse;
import com.silberlima.segueme.inscricoes.application.ConsultarFunilUseCase;
import com.silberlima.segueme.inscricoes.application.ListarParticipantesUseCase;
import com.silberlima.segueme.inscricoes.application.MarcarPagamentoUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.UUID;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Consolidação do funil de inscrição e lista final de Participantes de um Encontro (RF-027).
 * Leitura restrita a Equipe Fichas e Paróquia (`inscricoes.md` 2.4: "Quem acessa: Equipe Fichas").
 */
@RestController
@RequestMapping("/api/v1/participantes")
@Tag(name = "Participantes", description = "Funil de inscrição e participantes de um Encontro (RF-027)")
public class ParticipantesController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('EQUIPE_FICHAS','PAROQUIA')";

    private final ListarParticipantesUseCase listarParticipantesUseCase;
    private final ConsultarFunilUseCase consultarFunilUseCase;
    private final MarcarPagamentoUseCase marcarPagamentoUseCase;
    private final ParticipanteResponseMapper participanteResponseMapper;

    public ParticipantesController(
            ListarParticipantesUseCase listarParticipantesUseCase,
            ConsultarFunilUseCase consultarFunilUseCase,
            MarcarPagamentoUseCase marcarPagamentoUseCase,
            ParticipanteResponseMapper participanteResponseMapper) {
        this.listarParticipantesUseCase = listarParticipantesUseCase;
        this.consultarFunilUseCase = consultarFunilUseCase;
        this.marcarPagamentoUseCase = marcarPagamentoUseCase;
        this.participanteResponseMapper = participanteResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista os Participantes (fichas confirmadas) de um Encontro, com status de pagamento (RF-027)")
    public ResponseEntity<PageResponse<ParticipanteResponse>> listar(
            @RequestParam UUID encontroId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.ASC) Pageable pageable) {
        var pagina = listarParticipantesUseCase.execute(encontroId, pageable).map(participanteResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @GetMapping("/funil")
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Consolida o funil de inscrição de um Encontro por etapa (RF-027)")
    public ResponseEntity<FunilResponse> funil(@RequestParam UUID encontroId) {
        return ResponseEntity.ok(FunilResponse.from(consultarFunilUseCase.execute(encontroId)));
    }

    @PatchMapping("/{fichaInscricaoId}/pagamento")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Alterna o status de pagamento de uma Ficha de Inscrição confirmada (placeholder até a Fase 9)")
    public ResponseEntity<ParticipanteResponse> atualizarPagamento(
            @PathVariable UUID fichaInscricaoId, @Valid @RequestBody AtualizarPagamentoRequest request) {
        var ficha = marcarPagamentoUseCase.execute(fichaInscricaoId, request.statusPagamento());
        return ResponseEntity.ok(participanteResponseMapper.toResponse(ficha));
    }
}
