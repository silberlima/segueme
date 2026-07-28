package com.silberlima.segueme.movimentacao.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Solicitação de mudança definitiva do vínculo de uma {@link Pessoa} entre duas Paróquias
 * (RF-040, RN-MOVIMENTO-01/RN-17). Nasce {@link StatusTransferencia#PENDENTE} e só se efetiva
 * (RN-MOVIMENTO-03: histórico sempre preservado) mediante decisão do Setorial
 * ({@link #aprovar()}/{@link #recusar(String)}).
 *
 * <p>Quem dispara o gatilho de uma Transferência (a própria pessoa, a paróquia de origem ou a de
 * destino) não é modelado no protótipo — pendência #9 de {@code pendencias-gerais.md}. Adotado
 * aqui: qualquer perfil com acesso ao cadastro de Pessoas/Paróquia pode solicitar, informando a
 * paróquia de destino.
 */
@Entity
@Table(name = "transferencia")
public class Transferencia extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paroquia_origem_id", nullable = false)
    private Paroquia paroquiaOrigem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paroquia_destino_id", nullable = false)
    private Paroquia paroquiaDestino;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusTransferencia status;

    @Column(name = "motivo", length = 500)
    private String motivo;

    @Column(name = "justificativa_recusa", length = 1000)
    private String justificativaRecusa;

    protected Transferencia() {
    }

    public Transferencia(Pessoa pessoa, Paroquia paroquiaOrigem, Paroquia paroquiaDestino, String motivo) {
        this.pessoa = pessoa;
        this.paroquiaOrigem = paroquiaOrigem;
        this.paroquiaDestino = paroquiaDestino;
        this.motivo = motivo;
        this.status = StatusTransferencia.PENDENTE;
    }

    /** RF-041: aprovação do Setorial efetiva a mudança de paróquia (RN-MOVIMENTO-01). */
    public void aprovar() {
        exigirPendente();
        this.status = StatusTransferencia.APROVADA;
    }

    /** RF-041: recusa do Setorial mantém a pessoa na paróquia de origem. */
    public void recusar(String justificativa) {
        exigirPendente();
        if (justificativa == null || justificativa.isBlank()) {
            throw new BusinessRuleViolationException("Toda recusa de transferência exige justificativa.");
        }
        this.status = StatusTransferencia.RECUSADA;
        this.justificativaRecusa = justificativa;
    }

    private void exigirPendente() {
        if (this.status != StatusTransferencia.PENDENTE) {
            throw new BusinessRuleViolationException(
                    "Só é possível decidir uma transferência pendente (status atual: " + this.status + ").");
        }
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Paroquia getParoquiaOrigem() {
        return paroquiaOrigem;
    }

    public Paroquia getParoquiaDestino() {
        return paroquiaDestino;
    }

    public StatusTransferencia getStatus() {
        return status;
    }

    public String getMotivo() {
        return motivo;
    }

    public String getJustificativaRecusa() {
        return justificativaRecusa;
    }
}
