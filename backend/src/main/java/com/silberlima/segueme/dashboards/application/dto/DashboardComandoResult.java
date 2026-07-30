package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import java.util.List;

/**
 * Painel de Comando (`dash_comando`, RF-053): acompanhamento dos convites do Encontro corrente.
 * "Convites enviados" é o total de convites emitidos, incluindo os recusados — é histórico de envio,
 * não de ocupação de vaga.
 */
public record DashboardComandoResult(
        long convitesEnviados,
        long aceites,
        long pendentes,
        long recusas,
        Encontro encontroCorrente,
        List<IntegranteEquipe> convitesRecentes,
        List<AceitesPorEquipe> aceitesPorEquipe) {
}
