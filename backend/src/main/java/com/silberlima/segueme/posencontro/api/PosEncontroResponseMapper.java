package com.silberlima.segueme.posencontro.api;

import com.silberlima.segueme.posencontro.api.dto.EncontroPosResponse;
import com.silberlima.segueme.posencontro.api.dto.FrequenciaPessoaResponse;
import com.silberlima.segueme.posencontro.api.dto.PresencaResponse;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.application.dto.PresencaPessoa;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface PosEncontroResponseMapper {

    @Mapping(target = "paroquiaId", source = "paroquia.id")
    @Mapping(target = "paroquiaNome", source = "paroquia.nome")
    EncontroPosResponse toResponse(EncontroPos encontroPos);

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "situacao", source = "pessoa.situacao")
    PresencaResponse toResponse(PresencaPessoa presencaPessoa);

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "faixaPrioridade", source = "faixa")
    FrequenciaPessoaResponse toResponse(FrequenciaPessoa frequenciaPessoa);
}
