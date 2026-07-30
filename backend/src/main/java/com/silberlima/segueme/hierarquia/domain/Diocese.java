package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/** Nível "Diocese/Arquidiocese" da hierarquia territorial (RN-04): Nacional → Diocese → Setor → Paróquia. */
@Entity
@Table(name = "diocese")
public class Diocese extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false)
    private String nome;

    /** Opcional: agrupa a diocese no gráfico por região do Painel Nacional (ver {@link RegiaoBrasil}). */
    @Enumerated(EnumType.STRING)
    @Column(name = "regiao", length = 20)
    private RegiaoBrasil regiao;

    protected Diocese() {
    }

    public Diocese(String nome) {
        this.nome = nome;
    }

    public Diocese(String nome, RegiaoBrasil regiao) {
        this.nome = nome;
        this.regiao = regiao;
    }

    public String getNome() {
        return nome;
    }

    public RegiaoBrasil getRegiao() {
        return regiao;
    }
}
