package com.silberlima.segueme.acesso.api.dto;

import com.silberlima.segueme.acesso.application.dto.MeResult;
import java.util.List;

public record MeResponse(String id, String email, String nome, List<String> perfis) {

    public static MeResponse from(MeResult result) {
        List<String> perfis = result.perfis().stream().map(Enum::name).toList();
        return new MeResponse(result.id(), result.email(), result.nome(), perfis);
    }
}
