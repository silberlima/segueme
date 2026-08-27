package com.silberlima.segueme.conteudo.api;

import com.silberlima.segueme.conteudo.api.dto.DesignarPalestranteRequest;
import com.silberlima.segueme.conteudo.api.dto.PalestraResponse;
import com.silberlima.segueme.conteudo.application.DesignarPalestranteUseCase;
import com.silberlima.segueme.conteudo.application.ListarPalestrasDoEncontroUseCase;
import com.silberlima.segueme.conteudo.application.dto.PalestraDoEncontro;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Os 7 temas oficiais de um Encontro, em ordem fixa, e o palestrante designado em cada um
 * (RF-051/052, RN-22/RN-CONTEUDO-01). Leitura liberada a todos os 10 perfis (mesma visão de
 * conteúdo das demais telas do Encontro); designação restrita a Paróquia e Equipe Comando — quem
 * exatamente pode editar não é especificado no protótipo (pendência #31 de
 * {@code pendencias-gerais.md}).
 */
@RestController
@RequestMapping("/api/v1/encontros/{encontroId}/palestras")
@Tag(name = "Conteúdo — Palestras", description = "Temas oficiais e palestrantes designados de um Encontro (RF-051/052)")
public class PalestrasController {

    private static final String PERFIS_DESIGNACAO = "hasAnyRole('PAROQUIA','EQUIPE_COMANDO')";
    private static final String PERFIS_LEITURA = "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA',"
            + "'EQUIPE_FICHAS','EQUIPE_MONTAGEM','EQUIPE_COMANDO','EQUIPE_FINANCAS','POS_ENCONTRO')";

    private final ListarPalestrasDoEncontroUseCase listarPalestrasDoEncontroUseCase;
    private final DesignarPalestranteUseCase designarPalestranteUseCase;
    private final PalestraResponseMapper palestraResponseMapper;

    public PalestrasController(
            ListarPalestrasDoEncontroUseCase listarPalestrasDoEncontroUseCase,
            DesignarPalestranteUseCase designarPalestranteUseCase,
            PalestraResponseMapper palestraResponseMapper) {
        this.listarPalestrasDoEncontroUseCase = listarPalestrasDoEncontroUseCase;
        this.designarPalestranteUseCase = designarPalestranteUseCase;
        this.palestraResponseMapper = palestraResponseMapper;
    }

    @GetMapping
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Lista os 7 temas oficiais do Encontro, em ordem fixa, com o palestrante designado (RN-CONTEUDO-01)")
    public List<PalestraResponse> listar(@PathVariable UUID encontroId) {
        List<PalestraDoEncontro> palestras = listarPalestrasDoEncontroUseCase.execute(encontroId);
        return palestras.stream().map(palestraResponseMapper::toResponse).toList();
    }

    @PatchMapping("/{palestraId}/designar")
    @PreAuthorize(PERFIS_DESIGNACAO + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Designa (ou substitui) o palestrante de um tema (UC — Designar palestrante)")
    public PalestraResponse designar(
            @PathVariable UUID encontroId,
            @PathVariable UUID palestraId,
            @Valid @RequestBody DesignarPalestranteRequest request) {
        var designacao = designarPalestranteUseCase.execute(encontroId, palestraId, request.palestranteId());
        return palestraResponseMapper.toResponse(new PalestraDoEncontro(designacao.getPalestra(), designacao.getPalestrante()));
    }
}
