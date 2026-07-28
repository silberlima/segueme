package com.silberlima.segueme.montagem.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.pessoas.domain.Competencia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Tipo de equipe de serviço do catálogo nacional (RN-06/RN-MONTAGEM-07): nome, mínimo/máximo de
 * integrantes e pré-requisito são definidos nacionalmente e semeados via Liquibase — a paróquia
 * (Equipe Montagem) só preenche vagas dentro dela, nunca cria ou renomeia um tipo.
 * {@code preRequisitoMinEncontros} é a modelagem adotada para o "pré-requisito" citado em
 * RN-MONTAGEM-03/RN-06 (fórmula exata é pendência #6 de {@code pendencias-gerais.md}): número
 * mínimo de participações anteriores em qualquer equipe, ou {@code null} quando o tipo não exige
 * pré-requisito.
 */
@Entity
@Table(name = "equipe")
public class Equipe extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false, unique = true)
    private String nome;

    @Column(name = "minimo", nullable = false)
    private int minimo;

    @Column(name = "maximo", nullable = false)
    private int maximo;

    @Column(name = "pre_requisito_min_encontros")
    private Integer preRequisitoMinEncontros;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "equipe_competencia",
            joinColumns = @JoinColumn(name = "equipe_id"),
            inverseJoinColumns = @JoinColumn(name = "competencia_id"))
    private Set<Competencia> competenciasRequeridas = new HashSet<>();

    protected Equipe() {
    }

    public Equipe(String nome, int minimo, int maximo, Integer preRequisitoMinEncontros) {
        this.nome = nome;
        this.minimo = minimo;
        this.maximo = maximo;
        this.preRequisitoMinEncontros = preRequisitoMinEncontros;
    }

    public String getNome() {
        return nome;
    }

    public int getMinimo() {
        return minimo;
    }

    public int getMaximo() {
        return maximo;
    }

    public Integer getPreRequisitoMinEncontros() {
        return preRequisitoMinEncontros;
    }

    public Set<Competencia> getCompetenciasRequeridas() {
        return Collections.unmodifiableSet(competenciasRequeridas);
    }
}
