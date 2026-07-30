package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
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
class RecusarTransferenciaUseCaseTest {

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldRefuseAndKeepPessoaAtOrigem() {
        UUID transferenciaId = UUID.randomUUID();
        Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
        Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, origem,
                null, null, null, null, null, null, null);
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);
        when(transferenciaRepository.findByIdAndDeletedFalse(transferenciaId)).thenReturn(Optional.of(transferencia));

        Transferencia resultado = new RecusarTransferenciaUseCase(transferenciaRepository, eventPublisher)
                .execute(transferenciaId, "Pendências não resolvidas.");

        assertThat(resultado.getStatus()).isEqualTo(StatusTransferencia.RECUSADA);
        assertThat(pessoa.getParoquia()).isEqualTo(origem);
    }

    @Test
    void shouldThrowWhenJustificativaBlank() {
        UUID transferenciaId = UUID.randomUUID();
        Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
        Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, origem,
                null, null, null, null, null, null, null);
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);
        when(transferenciaRepository.findByIdAndDeletedFalse(transferenciaId)).thenReturn(Optional.of(transferencia));

        assertThatThrownBy(() -> new RecusarTransferenciaUseCase(transferenciaRepository, eventPublisher).execute(transferenciaId, " "))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenTransferenciaNotFound() {
        UUID transferenciaId = UUID.randomUUID();
        when(transferenciaRepository.findByIdAndDeletedFalse(transferenciaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RecusarTransferenciaUseCase(transferenciaRepository, eventPublisher).execute(transferenciaId, "Motivo"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
