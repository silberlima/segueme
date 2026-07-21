package com.silberlima.segueme.hierarquia.api;

import com.silberlima.segueme.hierarquia.api.dto.DioceseResponse;
import com.silberlima.segueme.hierarquia.api.dto.ParoquiaResumoResponse;
import com.silberlima.segueme.hierarquia.api.dto.SetorResponse;
import com.silberlima.segueme.hierarquia.application.dto.DioceseArvoreResult;
import com.silberlima.segueme.hierarquia.application.dto.ParoquiaResumoResult;
import com.silberlima.segueme.hierarquia.application.dto.SetorArvoreResult;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface DioceseResponseMapper {

    DioceseResponse toResponse(DioceseArvoreResult result);

    SetorResponse toResponse(SetorArvoreResult result);

    ParoquiaResumoResponse toResponse(ParoquiaResumoResult result);
}
