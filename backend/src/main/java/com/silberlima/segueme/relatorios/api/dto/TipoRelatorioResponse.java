package com.silberlima.segueme.relatorios.api.dto;

import com.silberlima.segueme.relatorios.domain.TipoRelatorio;

/** Cartão de tipo de relatório exibido na tela Relatórios (RF-056). */
public record TipoRelatorioResponse(String tipo, String titulo, String descricao) {

    public static TipoRelatorioResponse from(TipoRelatorio tipo) {
        return new TipoRelatorioResponse(tipo.name(), tipo.getTitulo(), tipo.getDescricao());
    }
}
