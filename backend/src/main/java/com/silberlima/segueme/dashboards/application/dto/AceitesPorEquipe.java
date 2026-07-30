package com.silberlima.segueme.dashboards.application.dto;

import java.util.UUID;

/**
 * Barra do gráfico "Aceites por equipe" do Painel de Comando (`dashboards-por-perfil.md` 3.7):
 * quantos dos convidados de cada equipe já aceitaram. Recusas não entram em {@code convidados} —
 * a vaga volta a ficar aberta (RN-MONTAGEM-06).
 */
public record AceitesPorEquipe(UUID equipeId, String equipeNome, long aceitos, long convidados) {
}
