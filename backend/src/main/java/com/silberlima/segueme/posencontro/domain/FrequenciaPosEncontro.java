package com.silberlima.segueme.posencontro.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Presença (ou ausência) de uma pessoa num {@link EncontroPos} (RF-058, RN-POS-01). Há no máximo um
 * registro por pessoa e encontro pós — remarcar a presença atualiza o registro existente via
 * {@link #registrar(boolean)} em vez de acumular linhas, para que a contagem de presenças que
 * alimenta a régua de prioridade (RN-POS-03) nunca dependa de deduplicação.
 */
@Entity
@Table(
        name = "frequencia_pos_encontro",
        uniqueConstraints = @UniqueConstraint(columnNames = {"encontro_pos_id", "pessoa_id"}))
public class FrequenciaPosEncontro extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_pos_id", nullable = false)
    private EncontroPos encontroPos;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Column(name = "presente", nullable = false)
    private boolean presente;

    protected FrequenciaPosEncontro() {
    }

    public FrequenciaPosEncontro(EncontroPos encontroPos, Pessoa pessoa, boolean presente) {
        if (encontroPos == null || pessoa == null) {
            throw new BusinessRuleViolationException(
                    "A frequência pós-encontro exige o encontro pós e a pessoa (RN-POS-01).");
        }
        this.encontroPos = encontroPos;
        this.pessoa = pessoa;
        this.presente = presente;
    }

    public void registrar(boolean presente) {
        this.presente = presente;
    }

    public EncontroPos getEncontroPos() {
        return encontroPos;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public boolean isPresente() {
        return presente;
    }
}
