package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarHistoricoPessoaUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    @Test
    void shouldThrowWhenPessoaNotFound() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.existsByIdAndDeletedFalse(pessoaId)).thenReturn(false);

        assertThatThrownBy(() -> new ListarHistoricoPessoaUseCase(pessoaRepository, historicoEventoRepository)
                        .execute(pessoaId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturnEmptyListWhenPessoaHasNoEvents() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.existsByIdAndDeletedFalse(pessoaId)).thenReturn(true);
        when(historicoEventoRepository.findByPessoa_IdOrderByDataEventoAsc(pessoaId)).thenReturn(List.of());

        var resultado =
                new ListarHistoricoPessoaUseCase(pessoaRepository, historicoEventoRepository).execute(pessoaId);

        assertThat(resultado).isEmpty();
    }
}
