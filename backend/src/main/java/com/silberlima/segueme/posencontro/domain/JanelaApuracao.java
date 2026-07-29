package com.silberlima.segueme.posencontro.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.time.LocalDate;

/**
 * Janela de datas em que a frequência é apurada. O período de apuração exato dos indicadores não
 * está definido no protótipo (pendência das telas de detalhe, `pendencias-gerais.md` #20): quando o
 * filtro não é informado, a apuração vai do início dos registros até hoje — encontros pós agendados
 * para o futuro nunca entram no cálculo, já que ainda não aconteceram.
 */
public record JanelaApuracao(LocalDate de, LocalDate ate) {

    private static final LocalDate SEM_LIMITE_INFERIOR = LocalDate.of(1970, 1, 1);

    public JanelaApuracao {
        if (de == null || ate == null) {
            throw new BusinessRuleViolationException("A janela de apuração exige data inicial e final.");
        }
        if (de.isAfter(ate)) {
            throw new BusinessRuleViolationException("A data inicial da apuração não pode ser posterior à final.");
        }
    }

    public static JanelaApuracao normalizar(LocalDate de, LocalDate ate) {
        return new JanelaApuracao(de == null ? SEM_LIMITE_INFERIOR : de, ate == null ? LocalDate.now() : ate);
    }
}
