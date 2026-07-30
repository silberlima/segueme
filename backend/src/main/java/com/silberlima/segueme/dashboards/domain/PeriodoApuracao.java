package com.silberlima.segueme.dashboards.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.time.LocalDate;

/**
 * Recorte de datas de um KPI de dashboard. As regras exatas de apuração (período, corte de data)
 * são ilustrativas no protótipo e precisam ser validadas com o PO (`dashboards-por-perfil.md`
 * seção 5): esta é a implementação de melhor esforço adotada até lá, concentrada num único lugar
 * para que a revisão com o PO troque a regra aqui e não em nove painéis.
 *
 * <p>Convenções: "no ano" = ano civil corrente (1º de janeiro a 31 de dezembro), "no mês" = mês
 * civil corrente. Tudo pela data de início do fato (data de início do Encontro, data do encontro
 * pós, data de solicitação), nunca pela data de conclusão.
 */
public record PeriodoApuracao(LocalDate de, LocalDate ate) {

    public PeriodoApuracao {
        if (de == null || ate == null) {
            throw new BusinessRuleViolationException("O período de apuração exige data inicial e final.");
        }
        if (de.isAfter(ate)) {
            throw new BusinessRuleViolationException(
                    "A data inicial do período de apuração não pode ser posterior à final.");
        }
    }

    /** Ano civil corrente. */
    public static PeriodoApuracao anoDe(LocalDate hoje) {
        return new PeriodoApuracao(hoje.withDayOfYear(1), hoje.withDayOfYear(hoje.lengthOfYear()));
    }

    /** Mês civil corrente. */
    public static PeriodoApuracao mesDe(LocalDate hoje) {
        return new PeriodoApuracao(hoje.withDayOfMonth(1), hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }

    /** Janela retroativa de meses fechados usada pelos gráficos mensais (inclui o mês corrente). */
    public static PeriodoApuracao ultimosMeses(LocalDate hoje, int meses) {
        if (meses < 1) {
            throw new BusinessRuleViolationException("A janela do gráfico mensal exige ao menos 1 mês.");
        }
        LocalDate primeiroMes = hoje.withDayOfMonth(1).minusMonths(meses - 1L);
        return new PeriodoApuracao(primeiroMes, hoje.withDayOfMonth(hoje.lengthOfMonth()));
    }
}
