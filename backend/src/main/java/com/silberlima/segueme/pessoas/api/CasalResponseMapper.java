package com.silberlima.segueme.pessoas.api;

import com.silberlima.segueme.pessoas.api.dto.CasalResponse;
import com.silberlima.segueme.pessoas.domain.Casal;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring", uses = PessoaResponseMapper.class)
public interface CasalResponseMapper {

    CasalResponse toResponse(Casal casal);
}
