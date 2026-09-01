package com.silberlima.segueme.movimentacao.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre as transições de estado de {@link Transferencia} (RF-041, RN-MOVIMENTO-01). */
class TransferenciaTest {

    private final Paroquia origem = new Paroquia("Origem", new Setor("Setor", new Diocese("Diocese")));
    private final Paroquia destino = new Paroquia("Destino", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa pessoa = new Pessoa(
            "Candidato",
            PessoasDeTeste.cpf(),
            LocalDate.of(2000, 1, 1),
            "(11) 90000-0000",
            null,
            EstadoCivil.SOLTEIRO,
            origem,
            PessoasDeTeste.endereco(),
            null,
            null,
            null,
            null,
            null,
            null,
            null);

    @Test
    void shouldStartPendente() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, "Mudança de cidade");

        assertThat(transferencia.getStatus()).isEqualTo(StatusTransferencia.PENDENTE);
        assertThat(transferencia.getJustificativaRecusa()).isNull();
    }

    @Test
    void shouldApprovePendingTransfer() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);

        transferencia.aprovar();

        assertThat(transferencia.getStatus()).isEqualTo(StatusTransferencia.APROVADA);
    }

    @Test
    void shouldNotApproveTwice() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);
        transferencia.aprovar();

        assertThatThrownBy(transferencia::aprovar).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRefusePendingTransferWithJustificativa() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);

        transferencia.recusar("Pessoa ainda tem pendências na paróquia de origem.");

        assertThat(transferencia.getStatus()).isEqualTo(StatusTransferencia.RECUSADA);
        assertThat(transferencia.getJustificativaRecusa()).isEqualTo("Pessoa ainda tem pendências na paróquia de origem.");
    }

    @Test
    void shouldRequireJustificativaToRefuse() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);

        assertThatThrownBy(() -> transferencia.recusar("  ")).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> transferencia.recusar(null)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldNotDecideAlreadyDecidedTransfer() {
        Transferencia transferencia = new Transferencia(pessoa, origem, destino, null);
        transferencia.recusar("Motivo qualquer.");

        assertThatThrownBy(transferencia::aprovar).isInstanceOf(BusinessRuleViolationException.class);
    }
}
