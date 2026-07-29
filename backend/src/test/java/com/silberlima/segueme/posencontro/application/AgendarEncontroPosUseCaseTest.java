package com.silberlima.segueme.posencontro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AgendarEncontroPosUseCaseTest {

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroPosRepository encontroPosRepository;

    @InjectMocks
    private AgendarEncontroPosUseCase agendarEncontroPosUseCase;

    private final UUID paroquiaId = UUID.randomUUID();
    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));
    private final LocalDate data = LocalDate.of(2026, 8, 5);

    @Test
    void shouldAgendarEncontroPos() {
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(encontroPosRepository.existsByParoquia_IdAndDataAndDeletedFalse(paroquiaId, data))
                .thenReturn(false);
        when(encontroPosRepository.save(any(EncontroPos.class))).thenAnswer(invocacao -> invocacao.getArgument(0));

        EncontroPos encontroPos = agendarEncontroPosUseCase.execute(paroquiaId, data, "Perseverança");

        assertThat(encontroPos.getData()).isEqualTo(data);
        assertThat(encontroPos.getTema()).isEqualTo("Perseverança");
    }

    @Test
    void shouldRejeitarSegundoEncontroPosNaMesmaData() {
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(encontroPosRepository.existsByParoquia_IdAndDataAndDeletedFalse(paroquiaId, data))
                .thenReturn(true);

        assertThatThrownBy(() -> agendarEncontroPosUseCase.execute(paroquiaId, data, null))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(encontroPosRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenParoquiaNotFound() {
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> agendarEncontroPosUseCase.execute(paroquiaId, data, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
