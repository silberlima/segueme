package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import java.util.List;

/**
 * Painel de Fichas (`dash_fichas`, RF-053): o funil de inscrição do Encontro corrente, as
 * pré-inscrições que ainda aguardam ficha e a fila de reserva por prioridade.
 *
 * <p>As etapas "Seguimistas" e "Seguidores" do funil não vêm de {@code FunilInscricao} (onde
 * {@code seguidores} continua sendo o placeholder documentado da Fase 5): são contadas pela situação
 * das pessoas da paróquia, que é o dado que a Fase 3 já mantém.
 */
public record DashboardFichasResult(
        long aguardandoFicha,
        long emAnalise,
        long confirmados,
        long vagasRestantes,
        long cadastroReserva,
        long seguimistas,
        long seguidores,
        Encontro encontroCorrente,
        List<PreInscricao> preInscricoesAguardandoFicha,
        List<CadastroReserva> reserva) {
}
