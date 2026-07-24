package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Equipe que recebe o Encontro pronto da Equipe Dirigente e assume a execução: convite de
 * coordenadores das demais equipes e acompanhamento de aceites/recusas (RN-COMANDO-01, ainda não
 * implementado — ver Fase 7).
 */
@Entity
@Table(name = "equipe_comando")
public class EquipeComando extends AbstractAuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false, unique = true)
    private Encontro encontro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "coordenador_geral_id", nullable = false)
    private Pessoa coordenadorGeral;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casal_coordenador_id", nullable = false)
    private Casal casalCoordenador;

    protected EquipeComando() {
    }

    public EquipeComando(Encontro encontro, Pessoa coordenadorGeral, Casal casalCoordenador) {
        this.encontro = encontro;
        this.coordenadorGeral = coordenadorGeral;
        this.casalCoordenador = casalCoordenador;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public Pessoa getCoordenadorGeral() {
        return coordenadorGeral;
    }

    public Casal getCasalCoordenador() {
        return casalCoordenador;
    }
}
