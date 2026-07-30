package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.StatusRelatorio;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Entrega o arquivo de um relatório concluído ao próprio solicitante. Relatório de outro usuário
 * responde como inexistente — não confirma nem nega que o id existe.
 */
@Service
public class BaixarRelatorioUseCase {

    private final RelatorioRepository relatorioRepository;

    public BaixarRelatorioUseCase(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public Relatorio execute(UUID relatorioId, String solicitanteId) {
        Relatorio relatorio = relatorioRepository
                .findByIdAndDeletedFalse(relatorioId)
                .filter(candidato -> candidato.pertenceA(solicitanteId))
                .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado: " + relatorioId));

        if (relatorio.getStatus() != StatusRelatorio.CONCLUIDO) {
            throw new BusinessRuleViolationException(
                    "O relatório ainda não está disponível para download (situação: " + relatorio.getStatus() + ").");
        }
        return relatorio;
    }
}
