package com.silberlima.segueme.acesso.application.dto;

import com.silberlima.segueme.acesso.domain.Perfil;
import java.util.List;

public record MeResult(String id, String email, String nome, List<Perfil> perfis) {
}
