package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.inscricoes.api.dto.CadastroReservaResponse;
import com.silberlima.segueme.inscricoes.api.dto.PreInscricaoResponse;
import java.util.List;

/** Painel de Fichas (`dash_fichas`, RF-053): funil de inscrição, fichas a preencher e fila de reserva. */
public record DashboardFichasResponse(
        long aguardandoFicha,
        long emAnalise,
        long confirmados,
        long vagasRestantes,
        long cadastroReserva,
        long seguimistas,
        long seguidores,
        EncontroDoPainelResponse encontroCorrente,
        List<PreInscricaoResponse> preInscricoesAguardandoFicha,
        List<CadastroReservaResponse> reserva) {
}
