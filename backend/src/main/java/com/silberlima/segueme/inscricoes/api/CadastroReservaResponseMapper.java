package com.silberlima.segueme.inscricoes.api;

import com.silberlima.segueme.inscricoes.api.dto.CadastroReservaResponse;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CadastroReservaResponseMapper {

    @Mapping(target = "ordem", source = "ordem")
    @Mapping(target = "pessoaId", source = "reserva.pessoa.id")
    @Mapping(target = "pessoaNome", source = "reserva.pessoa.nomeCompleto")
    @Mapping(target = "paroquiaNome", source = "reserva.pessoa.paroquia.nome")
    @Mapping(target = "naFilaDesde", source = "reserva.createdAt")
    @Mapping(target = "status", source = "reserva.status")
    CadastroReservaResponse toResponse(CadastroReserva reserva, int ordem);
}
