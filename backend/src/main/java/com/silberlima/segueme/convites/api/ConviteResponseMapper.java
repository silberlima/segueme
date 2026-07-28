package com.silberlima.segueme.convites.api;

import com.silberlima.segueme.convites.api.dto.ConviteResponse;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ConviteResponseMapper {

    @Mapping(target = "encontroId", source = "encontro.id")
    @Mapping(target = "equipeId", source = "equipe.id")
    @Mapping(target = "equipeNome", source = "equipe.nome")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    ConviteResponse toResponse(IntegranteEquipe convite);
}
