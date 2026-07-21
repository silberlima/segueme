package com.silberlima.segueme.notificacao.api;

import com.silberlima.segueme.notificacao.api.dto.NotificacaoResponse;
import com.silberlima.segueme.notificacao.domain.Notificacao;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface NotificacaoResponseMapper {

    NotificacaoResponse toResponse(Notificacao notificacao);
}
