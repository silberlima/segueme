package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Nível "Diocese/Arquidiocese" da hierarquia territorial (RN-04): Nacional → Diocese → Setor → Paróquia. */
@Entity
@Table(name = "diocese")
public class Diocese extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    protected Diocese() {
    }

    public Diocese(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
