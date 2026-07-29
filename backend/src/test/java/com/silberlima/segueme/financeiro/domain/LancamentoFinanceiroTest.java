package com.silberlima.segueme.financeiro.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre as transições de estado de {@link LancamentoFinanceiro} (RF-047/049, RN-FINANCEIRO-02). */
class LancamentoFinanceiroTest {

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);

    @Test
    void shouldCreateReceitaWithoutSituacao() {
        LancamentoFinanceiro receita = LancamentoFinanceiro.receita(encontro, "Doações", new BigDecimal("100.00"), LocalDate.now());

        assertThat(receita.getTipo()).isEqualTo(TipoLancamento.RECEITA);
        assertThat(receita.getSituacao()).isNull();
    }

    @Test
    void shouldCreateDespesaAsPendente() {
        LancamentoFinanceiro despesa = LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());

        assertThat(despesa.getTipo()).isEqualTo(TipoLancamento.DESPESA);
        assertThat(despesa.getSituacao()).isEqualTo(SituacaoDespesa.PENDENTE);
    }

    @Test
    void shouldRejectNonPositiveValor() {
        assertThatThrownBy(() -> LancamentoFinanceiro.receita(encontro, "Doações", BigDecimal.ZERO, LocalDate.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRejectBlankCategoria() {
        assertThatThrownBy(() -> LancamentoFinanceiro.receita(encontro, " ", new BigDecimal("10.00"), LocalDate.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldApprovePendingDespesa() {
        LancamentoFinanceiro despesa = LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());

        despesa.aprovar("finance-user");

        assertThat(despesa.getSituacao()).isEqualTo(SituacaoDespesa.APROVADA);
        assertThat(despesa.getAprovadoPor()).isEqualTo("finance-user");
    }

    @Test
    void shouldNotApproveTwice() {
        LancamentoFinanceiro despesa = LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());
        despesa.aprovar("finance-user");

        assertThatThrownBy(() -> despesa.aprovar("finance-user")).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldNotApproveReceita() {
        LancamentoFinanceiro receita = LancamentoFinanceiro.receita(encontro, "Doações", new BigDecimal("100.00"), LocalDate.now());

        assertThatThrownBy(() -> receita.aprovar("finance-user")).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldPayApprovedDespesa() {
        LancamentoFinanceiro despesa = LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());
        despesa.aprovar("finance-user");

        despesa.pagar();

        assertThat(despesa.getSituacao()).isEqualTo(SituacaoDespesa.PAGA);
    }

    @Test
    void shouldNotPayPendingDespesa() {
        LancamentoFinanceiro despesa = LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());

        assertThatThrownBy(despesa::pagar).isInstanceOf(BusinessRuleViolationException.class);
    }
}
