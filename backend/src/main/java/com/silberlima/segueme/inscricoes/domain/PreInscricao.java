package com.silberlima.segueme.inscricoes.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
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
 * Candidato pré-inscrito a um Encontro, antes do preenchimento da Ficha (RF-022). Nasce
 * {@link StatusPreInscricao#AGUARDANDO_FICHA}; a Equipe Fichas pode marcá-la em análise
 * enquanto revisa, e ela é confirmada ao preencher a ficha (RN-INSCRICAO-01/02).
 */
@Entity
@Table(name = "pre_inscricao")
public class PreInscricao extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPreInscricao status = StatusPreInscricao.AGUARDANDO_FICHA;

    protected PreInscricao() {
    }

    public PreInscricao(Pessoa pessoa, Encontro encontro) {
        this.pessoa = pessoa;
        this.encontro = encontro;
    }

    public void marcarEmAnalise() {
        if (status == StatusPreInscricao.CONFIRMADA) {
            throw new BusinessRuleViolationException("Pré-inscrição já confirmada não pode voltar a em análise.");
        }
        this.status = StatusPreInscricao.EM_ANALISE;
    }

    public void confirmar() {
        if (status == StatusPreInscricao.CONFIRMADA) {
            throw new BusinessRuleViolationException("Pré-inscrição já confirmada.");
        }
        this.status = StatusPreInscricao.CONFIRMADA;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public StatusPreInscricao getStatus() {
        return status;
    }
}
