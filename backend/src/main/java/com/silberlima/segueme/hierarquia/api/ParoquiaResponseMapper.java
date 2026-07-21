package com.silberlima.segueme.hierarquia.api;

import com.silberlima.segueme.hierarquia.api.dto.ParoquiaResponse;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import org.mapstruct.Mapping;
import org.mapstruct.Mapper;

/**
 * {@code numSeguidores} e {@code statusEncontroAtual} nascem placeholder (0/null): dependem de
 * Pessoa (Fase 3) e Encontro (Fase 4), que ainda não existem.
 */
@Mapper(componentModel = "spring")
public interface ParoquiaResponseMapper {

    @Mapping(target = "setorId", source = "setor.id")
    @Mapping(target = "setorNome", source = "setor.nome")
    @Mapping(target = "dioceseNome", source = "setor.diocese.nome")
    @Mapping(target = "numSeguidores", constant = "0")
    @Mapping(target = "statusEncontroAtual", ignore = true)
    ParoquiaResponse toResponse(Paroquia paroquia);
}
