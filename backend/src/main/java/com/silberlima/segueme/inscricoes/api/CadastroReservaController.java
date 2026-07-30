package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.inscricoes.api.dto.CadastroReservaResponse;
import com.silberlima.segueme.inscricoes.api.dto.PreInscricaoResponse;
import com.silberlima.segueme.inscricoes.api.dto.PromoverReservaRequest;
import com.silberlima.segueme.inscricoes.application.ListarCadastroReservaUseCase;
import com.silberlima.segueme.inscricoes.application.PromoverReservaUseCase;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Cadastro de Reserva: fila de prioridade de candidatos além do limite de vagas de um Encontro
 * (RF-025/026, RN-INSCRICAO-03/04, UC-07). Escrita restrita à Equipe Fichas.
 */
@RestController
@RequestMapping("/api/v1/cadastro-reserva")
@Tag(name = "Cadastro de Reserva", description = "Fila de prioridade de candidatos além do limite de vagas (RF-025/026)")
public class CadastroReservaController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('EQUIPE_FICHAS')";
    private static final String PERFIS_LEITURA = "hasAnyRole('EQUIPE_FICHAS','EQUIPE_MONTAGEM','PAROQUIA')";

    private final ListarCadastroReservaUseCase listarCadastroReservaUseCase;
    private final PromoverReservaUseCase promoverReservaUseCase;
    private final CadastroReservaResponseMapper cadastroReservaResponseMapper;
    private final PreInscricaoResponseMapper preInscricaoResponseMapper;

    public CadastroReservaController(
            ListarCadastroReservaUseCase listarCadastroReservaUseCase,
            PromoverReservaUseCase promoverReservaUseCase,
            CadastroReservaResponseMapper cadastroReservaResponseMapper,
            PreInscricaoResponseMapper preInscricaoResponseMapper) {
        this.listarCadastroReservaUseCase = listarCadastroReservaUseCase;
        this.promoverReservaUseCase = promoverReservaUseCase;
        this.cadastroReservaResponseMapper = cadastroReservaResponseMapper;
        this.preInscricaoResponseMapper = preInscricaoResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista o Cadastro de Reserva da paróquia do Encontro, em ordem de chegada (RF-025, RN-INSCRICAO-04)")
    public ResponseEntity<List<CadastroReservaResponse>> listar(@RequestParam UUID encontroId) {
        List<CadastroReserva> reservas = listarCadastroReservaUseCase.execute(encontroId);
        List<CadastroReservaResponse> response = new ArrayList<>();
        int ordem = 1;
        for (CadastroReserva reserva : reservas) {
            response.add(cadastroReservaResponseMapper.toResponse(reserva, ordem++));
        }
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/promover")
    @PreAuthorize(PERFIS_ESCRITA)
    @Operation(summary = "Promove um candidato da reserva a uma Pré-inscrição confirmada no Encontro de destino (RF-026, UC-07)")
    public ResponseEntity<PreInscricaoResponse> promover(
            @PathVariable UUID id, @Valid @RequestBody PromoverReservaRequest request) {
        PreInscricao preInscricao = promoverReservaUseCase.execute(id, request.encontroDestinoId());
        return ResponseEntity.status(HttpStatus.CREATED).body(preInscricaoResponseMapper.toResponse(preInscricao));
    }
}
