package com.silberlima.segueme.posencontro.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;

/**
 * Um encontro pós-encontro (semanal/periódico) de uma paróquia, identificado pela sua data
 * (RN-POS-01: a frequência é registrada por pessoa e por data de encontro pós). O plano da Fase 11
 * cita só {@code FrequenciaPosEncontro} com a data embutida; a data virou entidade própria porque o
 * módulo precisa agendar o próximo encontro e contar quantos houve no período — o denominador das
 * faixas de prioridade (RN-POS-03, ex.: 3/12) é o total de encontros realizados, não o número de
 * registros de cada pessoa.
 *
 * <p>Duas ocorrências na mesma paróquia e data são o mesmo encontro (constraint única).
 */
@Entity
@Table(
        name = "encontro_pos",
        uniqueConstraints = @UniqueConstraint(columnNames = {"paroquia_id", "data"}))
public class EncontroPos extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paroquia_id", nullable = false)
    private Paroquia paroquia;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Column(name = "tema", length = 200)
    private String tema;

    protected EncontroPos() {
    }

    public EncontroPos(Paroquia paroquia, LocalDate data, String tema) {
        if (paroquia == null) {
            throw new BusinessRuleViolationException("Todo encontro pós pertence a uma paróquia.");
        }
        if (data == null) {
            throw new BusinessRuleViolationException("Todo encontro pós exige a data em que acontece (RN-POS-01).");
        }
        this.paroquia = paroquia;
        this.data = data;
        this.tema = tema == null || tema.isBlank() ? null : tema.trim();
    }

    public Paroquia getParoquia() {
        return paroquia;
    }

    public LocalDate getData() {
        return data;
    }

    public String getTema() {
        return tema;
    }
}
