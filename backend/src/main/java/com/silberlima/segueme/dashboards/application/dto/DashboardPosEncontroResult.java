package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import java.util.List;

/**
 * Painel Pós-Encontro (`dash_posencontro`, RF-053): quantos jovens são acompanhados, a presença
 * média apurada, o gráfico mensal de frequência e o ranking de prioridade que a Fase 6 consome.
 *
 * <p>{@code presencaMedia} é a média dos percentuais de frequência dos acompanhados na janela
 * apurada (RN-POS-02/03), arredondada para inteiro. {@code chamadaRegistrada} é quantas presenças já
 * foram conferidas no próximo encontro pós — o protótipo fala de um cartão "confirmados", mas não
 * existe confirmação prévia de presença no modelo: o que há é a chamada, que pode estar em branco.
 */
public record DashboardPosEncontroResult(
        long jovensAcompanhados,
        int presencaMedia,
        long encontrosPosNoMes,
        long prioridadeAlta,
        List<ContagemMensal> frequenciaMensal,
        EncontroPos proximoEncontroPos,
        long chamadaRegistrada,
        List<FrequenciaPessoa> prioridades) {
}
