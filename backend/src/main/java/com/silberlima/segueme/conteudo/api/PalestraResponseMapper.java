package com.silberlima.segueme.conteudo.api;

import com.silberlima.segueme.conteudo.api.dto.PalestraResponse;
import com.silberlima.segueme.conteudo.application.dto.PalestraDoEncontro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PalestraResponseMapper {

    @Mapping(target = "palestraId", source = "palestra.id")
    @Mapping(target = "numero", source = "palestra.numero")
    @Mapping(target = "tema", source = "palestra.tema")
    @Mapping(target = "palestranteId", source = "palestrante.id")
    @Mapping(target = "palestranteNome", source = "palestrante.nome")
    @Mapping(target = "palestranteTipo", source = "palestrante.tipo")
    PalestraResponse toResponse(PalestraDoEncontro palestraDoEncontro);
}
