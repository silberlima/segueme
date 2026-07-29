package com.silberlima.segueme.posencontro.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;

/**
 * Régua de corte que classifica um percentual de frequência em {@link FaixaPrioridade} (RN-POS-03).
 * Objeto de domínio puro (sem dependência de framework) e parametrizável: a régua exata é pendência
 * #14 de {@code pendencias-gerais.md}, então os valores vêm de configuração
 * ({@code app.pos-encontro.regua.*}) em vez de constantes hardcoded, e o default adotado
 * (75% / 40%) é coerente com os exemplos do próprio doc — 12/12 = Alta, 3/12 = Baixa.
 *
 * @param percentualMinimoAlta percentual (0–100) a partir do qual a frequência é {@link FaixaPrioridade#ALTA}
 * @param percentualMinimoMedia percentual (0–100) a partir do qual é {@link FaixaPrioridade#MEDIA}; abaixo dele, {@link FaixaPrioridade#BAIXA}
 */
public record ReguaPrioridade(int percentualMinimoAlta, int percentualMinimoMedia) {

    public ReguaPrioridade {
        if (percentualMinimoMedia < 0 || percentualMinimoAlta > 100) {
            throw new BusinessRuleViolationException("A régua de prioridade opera na escala de 0 a 100.");
        }
        if (percentualMinimoAlta <= percentualMinimoMedia) {
            throw new BusinessRuleViolationException(
                    "O corte da faixa Alta deve ser maior que o da faixa Média na régua de prioridade.");
        }
    }

    public FaixaPrioridade classificar(int percentualFrequencia) {
        if (percentualFrequencia >= percentualMinimoAlta) {
            return FaixaPrioridade.ALTA;
        }
        if (percentualFrequencia >= percentualMinimoMedia) {
            return FaixaPrioridade.MEDIA;
        }
        return FaixaPrioridade.BAIXA;
    }
}
