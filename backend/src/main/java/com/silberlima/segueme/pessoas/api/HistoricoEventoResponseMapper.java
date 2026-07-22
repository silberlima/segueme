package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.pessoas.api.dto.HistoricoEventoResponse;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface HistoricoEventoResponseMapper {

    HistoricoEventoResponse toResponse(HistoricoEvento historicoEvento);
}
