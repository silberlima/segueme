package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.inscricoes.api.dto.ParticipanteResponse;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ParticipanteResponseMapper {

    @Mapping(target = "fichaInscricaoId", source = "id")
    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "paroquiaNome", source = "pessoa.paroquia.nome")
    ParticipanteResponse toResponse(FichaInscricao ficha);
}
