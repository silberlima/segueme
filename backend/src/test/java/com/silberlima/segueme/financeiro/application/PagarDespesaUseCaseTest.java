package com.silberlima.segueme.financeiro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import com.silberlima.segueme.financeiro.domain.SituacaoDespesa;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PagarDespesaUseCaseTest {

    @Mock
    private LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);

    @Test
    void shouldPayApprovedDespesa() {
        UUID lancamentoId = UUID.randomUUID();
        LancamentoFinanceiro despesa =
                LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());
        despesa.aprovar("finance-user");
        when(lancamentoFinanceiroRepository.findByIdAndDeletedFalse(lancamentoId)).thenReturn(Optional.of(despesa));

        LancamentoFinanceiro resultado = new PagarDespesaUseCase(lancamentoFinanceiroRepository).execute(lancamentoId);

        assertThat(resultado.getSituacao()).isEqualTo(SituacaoDespesa.PAGA);
    }

    @Test
    void shouldNotPayPendingDespesa() {
        UUID lancamentoId = UUID.randomUUID();
        LancamentoFinanceiro despesa =
                LancamentoFinanceiro.despesa(encontro, "Alimentação", new BigDecimal("50.00"), LocalDate.now());
        when(lancamentoFinanceiroRepository.findByIdAndDeletedFalse(lancamentoId)).thenReturn(Optional.of(despesa));

        assertThatThrownBy(() -> new PagarDespesaUseCase(lancamentoFinanceiroRepository).execute(lancamentoId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenLancamentoNotFound() {
        UUID lancamentoId = UUID.randomUUID();
        when(lancamentoFinanceiroRepository.findByIdAndDeletedFalse(lancamentoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new PagarDespesaUseCase(lancamentoFinanceiroRepository).execute(lancamentoId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
