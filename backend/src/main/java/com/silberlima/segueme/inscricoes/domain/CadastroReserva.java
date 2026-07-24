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
 * Fila de prioridade de candidatos que não couberam no limite de vagas de um Encontro
 * (RN-INSCRICAO-03/04). A ordem é FIFO por {@code createdAt}; a prioridade para o próximo
 * Encontro da mesma paróquia é automática por construção da consulta (ver
 * {@code CadastroReservaRepository}), não por um campo de encontro "de destino".
 */
@Entity
@Table(name = "cadastro_reserva")
public class CadastroReserva extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusReserva status = StatusReserva.AGUARDANDO;

    protected CadastroReserva() {
    }

    public CadastroReserva(Pessoa pessoa, Encontro encontro) {
        this.pessoa = pessoa;
        this.encontro = encontro;
    }

    public void promover() {
        if (status == StatusReserva.PROMOVIDO) {
            throw new BusinessRuleViolationException("Candidato da reserva já foi promovido.");
        }
        this.status = StatusReserva.PROMOVIDO;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public StatusReserva getStatus() {
        return status;
    }
}
