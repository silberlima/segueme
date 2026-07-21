package com.silberlima.segueme.acesso.application.dto;

import java.util.List;

/**
 * Claims já extraídas do JWT pela camada API, desacoplando o use case do tipo
 * {@code org.springframework.security.oauth2.jwt.Jwt}.
 */
public record UsuarioAutenticadoClaims(String id, String email, String nome, List<String> authorities) {
}
