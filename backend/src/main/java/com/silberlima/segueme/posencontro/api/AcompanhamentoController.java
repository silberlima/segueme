package com.silberlima.segueme.posencontro.api;

import com.silberlima.segueme.posencontro.api.dto.FrequenciaPessoaResponse;
import com.silberlima.segueme.posencontro.application.ConsultarFrequenciaUseCase;
import com.silberlima.segueme.posencontro.application.ListarPrioridadesUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Acompanhamento consolidado do pós-encontro (RF-059): frequência por pessoa num período e ranking
 * de prioridade acumulado (RN-POS-02/03). A Equipe Montagem lê os dois porque é ela quem convida
 * para as equipes — o mesmo dado que o motor de sugestões da Fase 6 pondera em 20% do score.
 */
@RestController
@RequestMapping("/api/v1/pos-encontro")
@Tag(name = "Pós-Encontro — Acompanhamento", description = "Frequência e prioridade dos jovens acompanhados (RF-059, RN-POS-02/03)")
public class AcompanhamentoController {

    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_MONTAGEM','POS_ENCONTRO')";

    private final ConsultarFrequenciaUseCase consultarFrequenciaUseCase;
    private final ListarPrioridadesUseCase listarPrioridadesUseCase;
    private final PosEncontroResponseMapper posEncontroResponseMapper;

    public AcompanhamentoController(
            ConsultarFrequenciaUseCase consultarFrequenciaUseCase,
            ListarPrioridadesUseCase listarPrioridadesUseCase,
            PosEncontroResponseMapper posEncontroResponseMapper) {
        this.consultarFrequenciaUseCase = consultarFrequenciaUseCase;
        this.listarPrioridadesUseCase = listarPrioridadesUseCase;
        this.posEncontroResponseMapper = posEncontroResponseMapper;
    }

    @GetMapping("/frequencia")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Frequência de cada acompanhado da paróquia no período informado (sem filtro: desde o início até hoje)")
    public List<FrequenciaPessoaResponse> frequencia(
            @RequestParam UUID paroquiaId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {
        return consultarFrequenciaUseCase.execute(paroquiaId, de, ate).stream()
                .map(posEncontroResponseMapper::toResponse)
                .toList();
    }

    @GetMapping("/prioridades")
    @PreAuthorize(PERFIS_LEITURA)
    @Operation(summary = "Ranking de prioridade para as próximas equipes, da maior para a menor frequência (RN-POS-02/03)")
    public List<FrequenciaPessoaResponse> prioridades(@RequestParam UUID paroquiaId) {
        return listarPrioridadesUseCase.execute(paroquiaId).stream()
                .map(posEncontroResponseMapper::toResponse)
                .toList();
    }
}
