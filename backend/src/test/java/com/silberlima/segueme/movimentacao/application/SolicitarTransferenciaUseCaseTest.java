package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class SolicitarTransferenciaUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Test
    void shouldCreatePendingTransferSnapshotingOrigem() {
        UUID pessoaId = UUID.randomUUID();
        UUID paroquiaDestinoId = UUID.randomUUID();
        Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
        ReflectionTestUtils.setField(origem, "id", UUID.randomUUID());
        Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
        ReflectionTestUtils.setField(destino, "id", paroquiaDestinoId);
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, origem,
                null, null, null, null, null, null, null);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaDestinoId)).thenReturn(Optional.of(destino));
        when(transferenciaRepository.save(any(Transferencia.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Transferencia transferencia = new SolicitarTransferenciaUseCase(pessoaRepository, paroquiaRepository, transferenciaRepository)
                .execute(pessoaId, paroquiaDestinoId, "Mudança de cidade");

        assertThat(transferencia.getStatus()).isEqualTo(StatusTransferencia.PENDENTE);
        assertThat(transferencia.getPessoa()).isEqualTo(pessoa);
        assertThat(transferencia.getParoquiaOrigem()).isEqualTo(origem);
        assertThat(transferencia.getParoquiaDestino()).isEqualTo(destino);
    }

    @Test
    void shouldThrowWhenDestinoIsSameAsOrigem() {
        UUID pessoaId = UUID.randomUUID();
        UUID paroquiaId = UUID.randomUUID();
        Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
        ReflectionTestUtils.setField(paroquia, "id", paroquiaId);
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
                null, null, null, null, null, null, null);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));

        assertThatThrownBy(() -> new SolicitarTransferenciaUseCase(pessoaRepository, paroquiaRepository, transferenciaRepository)
                        .execute(pessoaId, paroquiaId, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenPessoaNotFound() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new SolicitarTransferenciaUseCase(pessoaRepository, paroquiaRepository, transferenciaRepository)
                        .execute(pessoaId, UUID.randomUUID(), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
