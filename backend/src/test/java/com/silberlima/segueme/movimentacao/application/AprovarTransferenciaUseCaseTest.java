package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AprovarTransferenciaUseCaseTest {

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldApproveAndChangePessoaParoquiaAndRecordHistory() {
        UUID transferenciaId = UUID.randomUUID();
        Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
        Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
        Pessoa pessoa = new Pessoa(
                "Candidato", LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, origem,
                null, null, null, null, null, null, null);
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);
        when(transferenciaRepository.findByIdAndDeletedFalse(transferenciaId)).thenReturn(Optional.of(transferencia));

        Transferencia resultado = new AprovarTransferenciaUseCase(transferenciaRepository, historicoEventoRepository, eventPublisher)
                .execute(transferenciaId);

        assertThat(resultado.getStatus()).isEqualTo(StatusTransferencia.APROVADA);
        assertThat(pessoa.getParoquia()).isEqualTo(destino);
        ArgumentCaptor<HistoricoEvento> captor = ArgumentCaptor.forClass(HistoricoEvento.class);
        verify(historicoEventoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoEventoHistorico.TRANSFERENCIA_PAROQUIA);
        assertThat(captor.getValue().getPessoa()).isEqualTo(pessoa);
    }

    @Test
    void shouldThrowWhenTransferenciaNotFound() {
        UUID transferenciaId = UUID.randomUUID();
        when(transferenciaRepository.findByIdAndDeletedFalse(transferenciaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new AprovarTransferenciaUseCase(transferenciaRepository, historicoEventoRepository, eventPublisher)
                        .execute(transferenciaId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
