package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.relatorios.domain.RelatorioSolicitadoEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

/**
 * Dispara a geração fora da request: {@code AFTER_COMMIT} garante que o relatório já existe no
 * banco quando o thread assíncrono for buscá-lo, e {@code @Async} devolve a resposta ao usuário
 * sem esperar a varredura dos dados (skills/10).
 */
@Component
public class GerarRelatorioAoSolicitarListener {

    private final GerarRelatorioUseCase gerarRelatorioUseCase;

    public GerarRelatorioAoSolicitarListener(GerarRelatorioUseCase gerarRelatorioUseCase) {
        this.gerarRelatorioUseCase = gerarRelatorioUseCase;
    }

    @Async("relatoriosExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void aoSolicitarRelatorio(RelatorioSolicitadoEvent evento) {
        gerarRelatorioUseCase.execute(evento.relatorioId());
    }
}
