package com.silberlima.segueme.conteudo.api;

import com.silberlima.segueme.conteudo.api.dto.PalestranteResponse;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PalestranteResponseMapper {

    @Mapping(target = "pessoaId", source = "pessoa.id")
    PalestranteResponse toResponse(Palestrante palestrante);
}
