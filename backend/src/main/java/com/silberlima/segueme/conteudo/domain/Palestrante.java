package com.silberlima.segueme.conteudo.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Pessoa que ministra uma palestra do encontro (RF-052). {@link TipoPalestrante#SEGUIDOR} é o
 * único tipo com {@link #pessoa} obrigatória (RN-CONTEUDO-02); convidado externo e clero são
 * cadastrados só com nome, sem gerar registro de {@link Pessoa} (fluxo de cadastro do Convidado
 * externo não detalhado no protótipo — pendência #27 de {@code pendencias-gerais.md}).
 */
@Entity
@Table(name = "palestrante")
public class Palestrante extends AbstractAuditableEntity {

    @Column(name = "nome", nullable = false, length = 150)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoPalestrante tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    protected Palestrante() {
    }

    private Palestrante(String nome, TipoPalestrante tipo, Pessoa pessoa) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleViolationException("Todo palestrante exige um nome.");
        }
        if (tipo == TipoPalestrante.SEGUIDOR && pessoa == null) {
            throw new BusinessRuleViolationException(
                    "Um palestrante do tipo SEGUIDOR exige o cadastro de Pessoa vinculado (RN-CONTEUDO-02).");
        }
        if (tipo != TipoPalestrante.SEGUIDOR && pessoa != null) {
            throw new BusinessRuleViolationException(
                    "Somente um palestrante do tipo SEGUIDOR pode referenciar um cadastro de Pessoa (RN-CONTEUDO-02).");
        }
        this.nome = nome;
        this.tipo = tipo;
        this.pessoa = pessoa;
    }

    public static Palestrante seguidor(Pessoa pessoa) {
        return new Palestrante(pessoa.getNomeCompleto(), TipoPalestrante.SEGUIDOR, pessoa);
    }

    public static Palestrante externo(String nome) {
        return new Palestrante(nome, TipoPalestrante.EXTERNO, null);
    }

    public static Palestrante clero(String nome) {
        return new Palestrante(nome, TipoPalestrante.CLERO, null);
    }

    public String getNome() {
        return nome;
    }

    public TipoPalestrante getTipo() {
        return tipo;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }
}
