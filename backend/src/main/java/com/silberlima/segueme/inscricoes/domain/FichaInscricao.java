package com.silberlima.segueme.inscricoes.domain;

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
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Registro de confirmação da inscrição de uma Pessoa num Encontro (RF-023/024,
 * RN-INSCRICAO-05: "os dados desta ficha integram o cadastro nacional e nunca são apagados").
 * Os dados pessoais/saúde/contato de emergência/competências das 4 seções da Ficha já vivem em
 * {@link Pessoa} desde a Fase 3 (cadastro nacional único) — esta entidade não os duplica, apenas
 * marca o evento de confirmação e o pagamento (placeholder até a Fase 9).
 */
@Entity
@Table(name = "ficha_inscricao")
public class FichaInscricao extends AbstractAuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pre_inscricao_id", nullable = false, unique = true)
    private PreInscricao preInscricao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_pagamento", nullable = false, length = 20)
    private StatusPagamento statusPagamento = StatusPagamento.PENDENTE;

    protected FichaInscricao() {
    }

    public FichaInscricao(PreInscricao preInscricao, Pessoa pessoa, Encontro encontro) {
        this.preInscricao = preInscricao;
        this.pessoa = pessoa;
        this.encontro = encontro;
    }

    public void marcarComoPago() {
        this.statusPagamento = StatusPagamento.PAGO;
    }

    public void marcarComoPendente() {
        this.statusPagamento = StatusPagamento.PENDENTE;
    }

    public PreInscricao getPreInscricao() {
        return preInscricao;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public StatusPagamento getStatusPagamento() {
        return statusPagamento;
    }
}
