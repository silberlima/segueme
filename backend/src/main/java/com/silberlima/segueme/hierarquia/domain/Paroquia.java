package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/** Nível "Paróquia" da hierarquia territorial (RN-04): pertence a exatamente um Setor. */
@Entity
@Table(name = "paroquia")
public class Paroquia extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "setor_id", nullable = false)
    private Setor setor;

    protected Paroquia() {
    }

    public Paroquia(String nome, Setor setor) {
        this.nome = nome;
        this.setor = setor;
    }

    public String getNome() {
        return nome;
    }

    public Setor getSetor() {
        return setor;
    }
}
