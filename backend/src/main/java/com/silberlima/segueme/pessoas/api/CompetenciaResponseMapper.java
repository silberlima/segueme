package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.pessoas.api.dto.CompetenciaResponse;
import com.silberlima.segueme.pessoas.application.dto.CompetenciaComContagem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CompetenciaResponseMapper {

    @Mapping(target = "id", source = "competencia.id")
    @Mapping(target = "nome", source = "competencia.nome")
    CompetenciaResponse toResponse(CompetenciaComContagem competenciaComContagem);
}
