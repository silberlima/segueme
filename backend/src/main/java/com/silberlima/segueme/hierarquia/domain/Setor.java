package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Nível "Setor" da hierarquia territorial (RN-04): pertence a exatamente uma Diocese. */
@Entity
@Table(name = "setor")
public class Setor extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diocese_id", nullable = false)
    private Diocese diocese;

    protected Setor() {
    }

    public Setor(String nome, Diocese diocese) {
        this.nome = nome;
        this.diocese = diocese;
    }

    public String getNome() {
        return nome;
    }

    public Diocese getDiocese() {
        return diocese;
    }
}
