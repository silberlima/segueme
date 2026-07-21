package com.silberlima.segueme.api.dto;

import java.util.List;
import org.springframework.data.domain.Page;

/**
 * Envelope de paginação estável para respostas de listagem (skills/03-api-rest.md).
 * {@code org.springframework.data.domain.Page} nunca é serializado diretamente: sua
 * estrutura JSON não tem garantia de estabilidade entre versões do Spring Data.
 */
public record PageResponse<T>(List<T> content, int page, int size, long totalElements, int totalPages) {

    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(), page.getNumber(), page.getSize(), page.getTotalElements(), page.getTotalPages());
    }
}
