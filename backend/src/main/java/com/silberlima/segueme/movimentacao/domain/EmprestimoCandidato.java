package com.silberlima.segueme.movimentacao.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
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
 * Um candidato (de outra paróquia) selecionado pelo Setorial para um {@link Emprestimo} (RF-043,
 * RN-MOVIMENTO-05). Ao ser efetivado pela Montagem, guarda o {@link IntegranteEquipe} criado no
 * roster da equipe — rastreabilidade da origem do vínculo (RN-MOVIMENTO-03) sem alterar o schema
 * do domínio Montagem.
 */
@Entity
@Table(name = "emprestimo_candidato")
public class EmprestimoCandidato extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "emprestimo_id", nullable = false)
    private Emprestimo emprestimo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusCandidatoEmprestimo status;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "integrante_equipe_id")
    private IntegranteEquipe integranteEquipe;

    protected EmprestimoCandidato() {
    }

    public EmprestimoCandidato(Emprestimo emprestimo, Pessoa pessoa) {
        this.emprestimo = emprestimo;
        this.pessoa = pessoa;
        this.status = StatusCandidatoEmprestimo.SUGERIDO;
    }

    /** RF-045: efetivado quando a Montagem o adiciona ao roster da equipe. */
    public void efetivar(IntegranteEquipe integranteEquipe) {
        if (this.status != StatusCandidatoEmprestimo.SUGERIDO) {
            throw new BusinessRuleViolationException(
                    "Só é possível efetivar um candidato sugerido (status atual: " + this.status + ").");
        }
        this.status = StatusCandidatoEmprestimo.EFETIVADO;
        this.integranteEquipe = integranteEquipe;
    }

    public Emprestimo getEmprestimo() {
        return emprestimo;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public StatusCandidatoEmprestimo getStatus() {
        return status;
    }

    public IntegranteEquipe getIntegranteEquipe() {
        return integranteEquipe;
    }
}
