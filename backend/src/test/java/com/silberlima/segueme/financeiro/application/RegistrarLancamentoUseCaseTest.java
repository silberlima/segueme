package com.silberlima.segueme.financeiro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiroRepository;
import com.silberlima.segueme.financeiro.domain.TipoLancamento;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarLancamentoUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private LancamentoFinanceiroRepository lancamentoFinanceiroRepository;

    @Test
    void shouldRegisterDespesaAsPendente() {
        UUID encontroId = UUID.randomUUID();
        Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
        Pessoa diretor = new Pessoa(
                "Diretor",
                PessoasDeTeste.cpf(),
                LocalDate.of(1980, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Encontro encontro = new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(lancamentoFinanceiroRepository.save(any(LancamentoFinanceiro.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        LancamentoFinanceiro lancamento = new RegistrarLancamentoUseCase(encontroRepository, lancamentoFinanceiroRepository)
                .execute(encontroId, TipoLancamento.DESPESA, "Alimentação", new BigDecimal("50.00"), LocalDate.now());

        assertThat(lancamento.getTipo()).isEqualTo(TipoLancamento.DESPESA);
        assertThat(lancamento.getEncontro()).isEqualTo(encontro);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RegistrarLancamentoUseCase(encontroRepository, lancamentoFinanceiroRepository)
                        .execute(encontroId, TipoLancamento.RECEITA, "Doações", new BigDecimal("10.00"), LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
