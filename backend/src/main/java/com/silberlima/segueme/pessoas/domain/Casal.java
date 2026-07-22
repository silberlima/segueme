package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/** Vínculo entre duas Pessoas casadas, ambas com cadastro próprio (RN-PESSOA-03/RN-03). */
@Entity
@Table(name = "casal")
public class Casal extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa1_id", nullable = false)
    private Pessoa pessoa1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa2_id", nullable = false)
    private Pessoa pessoa2;

    @Column(name = "data_vinculo", nullable = false)
    private LocalDate dataVinculo;

    protected Casal() {
    }

    public Casal(Pessoa pessoa1, Pessoa pessoa2, LocalDate dataVinculo) {
        this.pessoa1 = pessoa1;
        this.pessoa2 = pessoa2;
        this.dataVinculo = dataVinculo;
    }

    public Pessoa getPessoa1() {
        return pessoa1;
    }

    public Pessoa getPessoa2() {
        return pessoa2;
    }

    public LocalDate getDataVinculo() {
        return dataVinculo;
    }
}
