package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.pessoas.domain.Casal;
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
 * Um dos até 3 membros (2 jovens + 1 casal, RN-20) de uma {@link PastaDirigente}. Usa
 * {@code pessoa} quando {@link TipoMembroDirigente#JOVEM} e {@code casal} quando
 * {@link TipoMembroDirigente#CASAL} — pasta incompleta é permitida (RN-21), não há constraint
 * de banco exigindo a composição completa.
 */
@Entity
@Table(name = "membro_equipe_dirigente")
public class MembroEquipeDirigente extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_dirigente_id", nullable = false)
    private EquipeDirigente equipeDirigente;

    @Enumerated(EnumType.STRING)
    @Column(name = "pasta", nullable = false, length = 20)
    private PastaDirigente pasta;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_membro", nullable = false, length = 10)
    private TipoMembroDirigente tipoMembro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id")
    private Pessoa pessoa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "casal_id")
    private Casal casal;

    protected MembroEquipeDirigente() {
    }

    public MembroEquipeDirigente(
            EquipeDirigente equipeDirigente,
            PastaDirigente pasta,
            TipoMembroDirigente tipoMembro,
            Pessoa pessoa,
            Casal casal) {
        this.equipeDirigente = equipeDirigente;
        this.pasta = pasta;
        this.tipoMembro = tipoMembro;
        this.pessoa = pessoa;
        this.casal = casal;
    }

    public EquipeDirigente getEquipeDirigente() {
        return equipeDirigente;
    }

    public PastaDirigente getPasta() {
        return pasta;
    }

    public TipoMembroDirigente getTipoMembro() {
        return tipoMembro;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public Casal getCasal() {
        return casal;
    }
}
