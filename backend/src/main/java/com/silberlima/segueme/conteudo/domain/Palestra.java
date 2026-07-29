package com.silberlima.segueme.conteudo.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * Tema oficial de palestra do catálogo nacional (RN-22/RN-CONTEUDO-01): existem exatamente 7,
 * numerados em ordem fixa e imutável, semeados via Liquibase — a paróquia nunca cria, renomeia
 * ou reordena um tema, apenas designa quem o ministra em cada Encontro (ver {@link DesignacaoPalestra}).
 *
 * <p>Lista definitiva e ementa dos 7 temas não foram detalhadas no protótipo (pendência #15 de
 * {@code pendencias-gerais.md}) — seed placeholder documentado no changelog até validação do PO,
 * mesmo racional do seed de Competências.
 */
@Entity
@Table(name = "palestra")
public class Palestra extends AbstractAuditableEntity {

    @Column(name = "numero", nullable = false, unique = true)
    private int numero;

    @Column(name = "tema", nullable = false, length = 200)
    private String tema;

    protected Palestra() {
    }

    public Palestra(int numero, String tema) {
        this.numero = numero;
        this.tema = tema;
    }

    public int getNumero() {
        return numero;
    }

    public String getTema() {
        return tema;
    }
}
