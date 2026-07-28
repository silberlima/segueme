package com.silberlima.segueme.montagem.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
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
 * Vínculo de uma Pessoa a uma {@link Equipe} (tipo do catálogo nacional) num Encontro específico
 * (RF-028, RF-031). Nasce sempre {@link StatusIntegrante#PENDENTE} — a máquina de estados de
 * convite/aceite pertence à Fase 7 (RN-CONVITE-01); aqui o único caminho para
 * {@link StatusIntegrante#RECUSADO} é via {@link Substituicao}, que marca o registro em vez de
 * apagá-lo (RN-MONTAGEM-06).
 */
@Entity
@Table(name = "integrante_equipe")
public class IntegranteEquipe extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "papel", nullable = false, length = 20)
    private PapelIntegrante papel;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusIntegrante status;

    protected IntegranteEquipe() {
    }

    public IntegranteEquipe(Encontro encontro, Equipe equipe, Pessoa pessoa, PapelIntegrante papel) {
        this.encontro = encontro;
        this.equipe = equipe;
        this.pessoa = pessoa;
        this.papel = papel;
        this.status = StatusIntegrante.PENDENTE;
    }

    /** Usado exclusivamente por {@code RegistrarSubstituicaoUseCase} (RN-MONTAGEM-06). */
    public void marcarRecusado() {
        this.status = StatusIntegrante.RECUSADO;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public PapelIntegrante getPapel() {
        return papel;
    }

    public StatusIntegrante getStatus() {
        return status;
    }
}
