package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/** Dom/habilidade do catálogo nacional (RF-013), semeado via Liquibase. */
@Entity
@Table(name = "competencia")
public class Competencia extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    protected Competencia() {
    }

    public Competencia(String nome) {
        this.nome = nome;
    }

    public String getNome() {
        return nome;
    }
}
