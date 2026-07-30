package com.silberlima.segueme.montagem.api;

import com.silberlima.segueme.montagem.api.dto.AdicionarIntegranteRequest;
import com.silberlima.segueme.montagem.api.dto.EquipeDetalheResponse;
import com.silberlima.segueme.montagem.api.dto.EquipeResumoResponse;
import com.silberlima.segueme.montagem.api.dto.IntegranteResponse;
import com.silberlima.segueme.montagem.application.AdicionarIntegranteUseCase;
import com.silberlima.segueme.montagem.application.ConsultarEquipeUseCase;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
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
 * Consulta e composição das equipes de serviço de um Encontro (RF-028, RF-031). Leitura liberada
 * a todos os 10 perfis (visão somente leitura fora da Equipe Montagem); edição restrita à Equipe
 * Montagem (RN-07/RN-MONTAGEM-01).
 */
@RestController
@RequestMapping("/api/v1/equipes")
@Tag(name = "Equipes", description = "Composição das equipes de serviço de um Encontro (RF-028, RF-031, RN-06, RN-MONTAGEM-01/07)")
public class EquipesController {

    private static final String PERFIS_ESCRITA = "hasAnyRole('EQUIPE_MONTAGEM')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO','SEGUIDOR')";

    private final ListarEquipesUseCase listarEquipesUseCase;
    private final ConsultarEquipeUseCase consultarEquipeUseCase;
    private final AdicionarIntegranteUseCase adicionarIntegranteUseCase;
    private final MontagemResponseMapper montagemResponseMapper;

    public EquipesController(
            ListarEquipesUseCase listarEquipesUseCase,
            ConsultarEquipeUseCase consultarEquipeUseCase,
            AdicionarIntegranteUseCase adicionarIntegranteUseCase,
            MontagemResponseMapper montagemResponseMapper) {
        this.listarEquipesUseCase = listarEquipesUseCase;
        this.consultarEquipeUseCase = consultarEquipeUseCase;
        this.adicionarIntegranteUseCase = adicionarIntegranteUseCase;
        this.montagemResponseMapper = montagemResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista as equipes de serviço de um Encontro, com vagas preenchidas/abertas de cada uma (RF-028)")
    public ResponseEntity<List<EquipeResumoResponse>> listar(@RequestParam UUID encontroId) {
        List<EquipeResumoResponse> equipes = listarEquipesUseCase.execute(encontroId).stream()
                .map(montagemResponseMapper::toResumoResponse)
                .toList();
        return ResponseEntity.ok(equipes);
    }

    @GetMapping("/{equipeId}")
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Exibe o detalhe de uma equipe de serviço: integrantes, vagas e pré-requisito (RF-028)")
    public ResponseEntity<EquipeDetalheResponse> consultar(
            @PathVariable UUID equipeId, @RequestParam UUID encontroId) {
        var composicao = consultarEquipeUseCase.execute(encontroId, equipeId);
        return ResponseEntity.ok(montagemResponseMapper.toDetalheResponse(composicao));
    }

    @PostMapping("/{equipeId}/integrantes")
    @PreAuthorize(PERFIS_ESCRITA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Adiciona um candidato (sugerido ou não) à equipe (RF-031, restrito à Equipe Montagem)")
    public ResponseEntity<IntegranteResponse> adicionarIntegrante(
            @PathVariable UUID equipeId,
            @RequestParam UUID encontroId,
            @Valid @RequestBody AdicionarIntegranteRequest request) {
        IntegranteEquipe integrante =
                adicionarIntegranteUseCase.execute(encontroId, equipeId, request.pessoaId(), request.papel());
        return ResponseEntity.status(HttpStatus.CREATED).body(montagemResponseMapper.toIntegranteResponse(integrante));
    }
}
