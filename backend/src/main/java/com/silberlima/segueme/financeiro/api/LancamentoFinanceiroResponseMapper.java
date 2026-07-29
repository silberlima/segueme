package com.silberlima.segueme.financeiro.api;

import com.silberlima.segueme.financeiro.api.dto.LancamentoFinanceiroResponse;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface LancamentoFinanceiroResponseMapper {

    @Mapping(target = "encontroId", source = "encontro.id")
    @Mapping(target = "encontroNumero", source = "encontro.numero")
    LancamentoFinanceiroResponse toResponse(LancamentoFinanceiro lancamento);
}
