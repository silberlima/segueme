package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import java.util.List;

/**
 * Painel da Paróquia (`dash_paroquia`, RF-053): o cartão do Encontro corrente com progresso de
 * montagem e inscrições, os Encontros recentes, as pendências e o resumo financeiro.
 *
 * <p>{@code encontroCorrente} é nulo quando a paróquia nunca criou um Encontro — nesse caso
 * progresso, pendências e financeiro vêm zerados, e a tela mostra o estado vazio com a ação
 * "Novo encontro". {@code diasParaProximoEncontro} é nulo quando não há Encontro futuro em aberto.
 *
 * <p>O protótipo fala de um "checklist de equipes-chave completas" sem definir quais equipes são
 * chave: {@code equipes} traz a composição de todas as equipes do Encontro e a tela marca as
 * completas, sem eleger um subconjunto que a documentação não define.
 */
public record DashboardParoquiaResult(
        long seguidoresAtivos,
        long encontrosRealizados,
        Integer diasParaProximoEncontro,
        long totalPendencias,
        Encontro encontroCorrente,
        long inscricoesConfirmadas,
        List<EquipeComposicao> equipes,
        List<Encontro> encontrosRecentes,
        List<Pendencia> pendencias,
        ResumoFinanceiro financeiro,
        List<Campanha> campanhas) {
}
