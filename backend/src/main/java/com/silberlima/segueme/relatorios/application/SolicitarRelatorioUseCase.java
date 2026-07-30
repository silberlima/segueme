package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.RelatorioSolicitadoEvent;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra o pedido de relatório e devolve na hora (UC-22). A geração acontece depois do commit,
 * fora desta transação — a request não espera pela varredura dos dados.
 */
@Service
public class SolicitarRelatorioUseCase {

    private final RelatorioRepository relatorioRepository;
    private final ApplicationEventPublisher eventPublisher;

    public SolicitarRelatorioUseCase(
            RelatorioRepository relatorioRepository, ApplicationEventPublisher eventPublisher) {
        this.relatorioRepository = relatorioRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Relatorio execute(
            TipoRelatorio tipo, String solicitanteId, List<Perfil> perfis, EscopoTerritorial escopo) {
        if (!tipo.autorizadoPara(perfis)) {
            throw new BusinessRuleViolationException(
                    "O perfil do usuário não pode gerar o relatório de " + tipo.getTitulo() + ".");
        }
        Relatorio relatorio = relatorioRepository.save(new Relatorio(tipo, solicitanteId, escopo));
        eventPublisher.publishEvent(new RelatorioSolicitadoEvent(relatorio.getId()));
        return relatorio;
    }
}
