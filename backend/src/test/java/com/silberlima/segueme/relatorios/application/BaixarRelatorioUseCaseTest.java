package com.silberlima.segueme.relatorios.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Cobre o download: só o próprio solicitante, e só depois de concluído. */
@ExtendWith(MockitoExtension.class)
class BaixarRelatorioUseCaseTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @InjectMocks
    private BaixarRelatorioUseCase baixarRelatorioUseCase;

    private final UUID relatorioId = UUID.randomUUID();

    @Test
    void shouldDevolverOConteudoDoRelatorioConcluido() {
        Relatorio relatorio = umRelatorio();
        relatorio.concluir(new ConteudoRelatorio(List.of("Jovem"), List.of(List.of("Ana"))));
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        assertThat(baixarRelatorioUseCase.execute(relatorioId, "user-a").getConteudo()).isEqualTo("Jovem\nAna");
    }

    @Test
    void shouldThrowExceptionWhenRelatorioAindaNaoConcluiu() {
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(umRelatorio()));

        assertThatThrownBy(() -> baixarRelatorioUseCase.execute(relatorioId, "user-a"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ainda não está disponível");
    }

    @Test
    void shouldReturn404WhenRelatorioEDeOutroUsuario() {
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(umRelatorio()));

        assertThatThrownBy(() -> baixarRelatorioUseCase.execute(relatorioId, "user-b"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldReturn404WhenRelatorioNaoExiste() {
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> baixarRelatorioUseCase.execute(relatorioId, "user-a"))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Relatorio umRelatorio() {
        return new Relatorio(
                TipoRelatorio.POS_ENCONTRO, "user-a", new EscopoTerritorial.DeParoquia(UUID.randomUUID()));
    }
}
