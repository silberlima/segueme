package com.silberlima.segueme.financeiro.api;

import com.silberlima.segueme.financeiro.api.dto.CampanhaResponse;
import com.silberlima.segueme.financeiro.domain.Campanha;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CampanhaResponseMapper {

    @Mapping(target = "encontroId", source = "encontro.id")
    @Mapping(target = "encontroNumero", source = "encontro.numero")
    CampanhaResponse toResponse(Campanha campanha);
}
