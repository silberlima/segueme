package com.silberlima.segueme.relatorios.api;

import com.silberlima.segueme.relatorios.api.dto.RelatorioResponse;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioResumo;
import org.springframework.stereotype.Component;

@Component
public class RelatorioResponseMapper {

    public RelatorioResponse toResponse(RelatorioResumo resumo) {
        return new RelatorioResponse(
                resumo.id(),
                resumo.tipo().name(),
                resumo.tipo().getTitulo(),
                resumo.status().name(),
                resumo.totalLinhas(),
                resumo.erro(),
                resumo.solicitadoEm(),
                resumo.geradoEm());
    }

    public RelatorioResponse toResponse(Relatorio relatorio) {
        return new RelatorioResponse(
                relatorio.getId(),
                relatorio.getTipo().name(),
                relatorio.getTipo().getTitulo(),
                relatorio.getStatus().name(),
                relatorio.getTotalLinhas(),
                relatorio.getErro(),
                relatorio.getCreatedAt(),
                relatorio.getGeradoEm());
    }
}
