package com.silberlima.segueme.montagem.api;

import com.silberlima.segueme.montagem.api.dto.CandidatoSugeridoResponse;
import com.silberlima.segueme.montagem.application.GerarSugestoesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Sugestões Inteligentes de candidatos a uma vaga de equipe (RF-029/030), tela e endpoint
 * exclusivos da Equipe Montagem (RN-08/RN-MONTAGEM-02) — nunca substituem a decisão humana
 * (RN-10/RN-MONTAGEM-04).
 */
@RestController
@RequestMapping("/api/v1/sugestoes")
@Tag(name = "Sugestões", description = "Candidatos ranqueados para uma vaga de equipe, exclusivo da Equipe Montagem (RF-029/030)")
public class SugestoesController {

    private static final String PERFIS_MONTAGEM = "hasAnyRole('EQUIPE_MONTAGEM')";

    private final GerarSugestoesUseCase gerarSugestoesUseCase;
    private final MontagemResponseMapper montagemResponseMapper;

    public SugestoesController(GerarSugestoesUseCase gerarSugestoesUseCase, MontagemResponseMapper montagemResponseMapper) {
        this.gerarSugestoesUseCase = gerarSugestoesUseCase;
        this.montagemResponseMapper = montagemResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_MONTAGEM)
    @Operation(summary = "Gera a lista ranqueada de candidatos a uma vaga de equipe (RF-029/030, visível só à Montagem)")
    public ResponseEntity<List<CandidatoSugeridoResponse>> listar(
            @RequestParam UUID encontroId, @RequestParam UUID equipeId) {
        List<CandidatoSugeridoResponse> candidatos = gerarSugestoesUseCase.execute(encontroId, equipeId).stream()
                .map(montagemResponseMapper::toCandidatoResponse)
                .toList();
        return ResponseEntity.ok(candidatos);
    }
}
