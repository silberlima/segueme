package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecusarEmprestimoUseCaseTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
            null, null, null, null, null, null, null);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldRefuseSolicitadoEmprestimo() {
        UUID emprestimoId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));

        Emprestimo resultado = new RecusarEmprestimoUseCase(emprestimoRepository, eventPublisher).execute(emprestimoId, "Sem candidatos.");

        assertThat(resultado.getStatus()).isEqualTo(StatusEmprestimo.RECUSADO);
    }

    @Test
    void shouldThrowWhenEmprestimoNotFound() {
        UUID emprestimoId = UUID.randomUUID();
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RecusarEmprestimoUseCase(emprestimoRepository, eventPublisher).execute(emprestimoId, "Motivo"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
