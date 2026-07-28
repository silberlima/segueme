package com.silberlima.segueme.movimentacao.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.pessoas.domain.Competencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Solicitação da Equipe Montagem ao Setorial por um Seguidor de outra paróquia para compor uma
 * vaga de {@link Equipe} num {@link Encontro} corrente, sem mudança definitiva de vínculo (RF-042,
 * RN-MOVIMENTO-02/04, RN-18). O fluxo é bidirecional: nasce {@link StatusEmprestimo#SOLICITADO}
 * (Montagem), o Setorial seleciona e envia candidatos ({@link #enviarSugestoes()}) e a Montagem
 * efetiva a adição ({@link #marcarEfetivado()}) — ver {@link EmprestimoCandidato}.
 *
 * <p>Prazo de vigência (encerramento automático ao fim do Encontro?) e limite de empréstimos
 * simultâneos por paróquia não são especificados no protótipo — pendências #10 e #11 de
 * {@code pendencias-gerais.md}; nenhum dos dois é aplicado aqui.
 */
@Entity
@Table(name = "emprestimo")
public class Emprestimo extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "competencia_requerida_id")
    private Competencia competenciaRequerida;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEmprestimo status;

    @Column(name = "justificativa_recusa", length = 1000)
    private String justificativaRecusa;

    protected Emprestimo() {
    }

    public Emprestimo(Encontro encontro, Equipe equipe, Competencia competenciaRequerida) {
        this.encontro = encontro;
        this.equipe = equipe;
        this.competenciaRequerida = competenciaRequerida;
        this.status = StatusEmprestimo.SOLICITADO;
    }

    /** RF-044: Setorial envia a seleção de candidatos à Montagem. */
    public void enviarSugestoes() {
        if (this.status != StatusEmprestimo.SOLICITADO) {
            throw new BusinessRuleViolationException(
                    "Só é possível enviar candidatos para um empréstimo solicitado (status atual: " + this.status + ").");
        }
        this.status = StatusEmprestimo.SUGESTOES_ENVIADAS;
    }

    /** RF-045: Montagem efetiva a adição de ao menos um candidato sugerido. */
    public void marcarEfetivado() {
        if (this.status != StatusEmprestimo.SUGESTOES_ENVIADAS) {
            throw new BusinessRuleViolationException(
                    "Só é possível efetivar um empréstimo com sugestões enviadas (status atual: " + this.status + ").");
        }
        this.status = StatusEmprestimo.EFETIVADO;
    }

    /**
     * Setorial recusa a solicitação antes de selecionar candidatos (`transferencias-e-emprestimos.md`
     * §2.3: cards da inbox de Solicitações têm Autorizar/Aprovar e Recusar para ambos os tipos).
     * Só é possível enquanto nenhum candidato foi enviado — a partir daí a decisão é da Montagem.
     */
    public void recusar(String justificativa) {
        if (this.status != StatusEmprestimo.SOLICITADO) {
            throw new BusinessRuleViolationException(
                    "Só é possível recusar um empréstimo ainda sem candidatos enviados (status atual: " + this.status + ").");
        }
        if (justificativa == null || justificativa.isBlank()) {
            throw new BusinessRuleViolationException("Toda recusa de empréstimo exige justificativa.");
        }
        this.status = StatusEmprestimo.RECUSADO;
        this.justificativaRecusa = justificativa;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public Competencia getCompetenciaRequerida() {
        return competenciaRequerida;
    }

    public StatusEmprestimo getStatus() {
        return status;
    }

    public String getJustificativaRecusa() {
        return justificativaRecusa;
    }
}
