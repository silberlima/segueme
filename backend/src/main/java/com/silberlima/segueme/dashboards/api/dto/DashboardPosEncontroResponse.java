package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.posencontro.api.dto.EncontroPosResponse;
import com.silberlima.segueme.posencontro.api.dto.FrequenciaPessoaResponse;
import java.util.List;

/**
 * Painel Pós-Encontro (`dash_posencontro`, RF-053). {@code chamadaRegistrada} é quantas presenças já
 * foram conferidas no próximo encontro pós — não existe confirmação prévia de presença no modelo.
 */
public record DashboardPosEncontroResponse(
        long jovensAcompanhados,
        int presencaMedia,
        long encontrosPosNoMes,
        long prioridadeAlta,
        List<ContagemMensalResponse> frequenciaMensal,
        EncontroPosResponse proximoEncontroPos,
        long chamadaRegistrada,
        List<FrequenciaPessoaResponse> prioridades) {
}
