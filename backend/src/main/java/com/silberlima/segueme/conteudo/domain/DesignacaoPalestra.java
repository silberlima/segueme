package com.silberlima.segueme.conteudo.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Vínculo entre um {@link Encontro} específico, um tema do catálogo nacional ({@link Palestra}) e
 * o {@link Palestrante} designado para ministrá-lo (RF-051/052, UC — Designar palestrante). Nasce
 * sem palestrante e é preenchida/atualizada via {@link #designar(Palestrante)} — o mesmo tema
 * pode trocar de palestrante quantas vezes for preciso antes do encontro.
 */
@Entity
@Table(
        name = "designacao_palestra",
        uniqueConstraints = @UniqueConstraint(columnNames = {"encontro_id", "palestra_id"}))
public class DesignacaoPalestra extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palestra_id", nullable = false)
    private Palestra palestra;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "palestrante_id")
    private Palestrante palestrante;

    protected DesignacaoPalestra() {
    }

    public DesignacaoPalestra(Encontro encontro, Palestra palestra) {
        this.encontro = encontro;
        this.palestra = palestra;
    }

    public void designar(Palestrante palestrante) {
        this.palestrante = palestrante;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public Palestra getPalestra() {
        return palestra;
    }

    public Palestrante getPalestrante() {
        return palestrante;
    }
}
