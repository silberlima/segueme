package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.financeiro.api.dto.CampanhaResponse;
import com.silberlima.segueme.financeiro.api.dto.ResumoFinanceiroResponse;
import com.silberlima.segueme.montagem.api.dto.EquipeResumoResponse;
import java.util.List;

/**
 * Painel da Paróquia (`dash_paroquia`, RF-053). {@code encontroCorrente} nulo = a paróquia ainda não
 * criou nenhum Encontro; nesse estado progresso, pendências e financeiro vêm zerados.
 * {@code diasParaProximoEncontro} nulo = o Encontro corrente já começou.
 */
public record DashboardParoquiaResponse(
        long seguidoresAtivos,
        long encontrosRealizados,
        Integer diasParaProximoEncontro,
        long totalPendencias,
        EncontroDoPainelResponse encontroCorrente,
        long inscricoesConfirmadas,
        List<EquipeResumoResponse> equipes,
        List<EncontroDoPainelResponse> encontrosRecentes,
        List<PendenciaResponse> pendencias,
        ResumoFinanceiroResponse financeiro,
        List<CampanhaResponse> campanhas) {
}
