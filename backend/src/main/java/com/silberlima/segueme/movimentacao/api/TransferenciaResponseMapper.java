package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.movimentacao.api.dto.TransferenciaResponse;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface TransferenciaResponseMapper {

    @Mapping(target = "pessoaId", source = "pessoa.id")
    @Mapping(target = "pessoaNome", source = "pessoa.nomeCompleto")
    @Mapping(target = "paroquiaOrigemId", source = "paroquiaOrigem.id")
    @Mapping(target = "paroquiaOrigemNome", source = "paroquiaOrigem.nome")
    @Mapping(target = "paroquiaDestinoId", source = "paroquiaDestino.id")
    @Mapping(target = "paroquiaDestinoNome", source = "paroquiaDestino.nome")
    TransferenciaResponse toResponse(Transferencia transferencia);
}
