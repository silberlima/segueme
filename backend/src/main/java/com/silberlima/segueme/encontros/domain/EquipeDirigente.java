package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

/**
 * Equipe formada na criação do Encontro (RN-20), com 5 {@link PastaDirigente} fixas
 * ({@link MembroEquipeDirigente}). O diretor espiritual que a acompanha vive em {@link Encontro}.
 */
@Entity
@Table(name = "equipe_dirigente")
public class EquipeDirigente extends AbstractAuditableEntity {

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false, unique = true)
    private Encontro encontro;

    protected EquipeDirigente() {
    }

    public EquipeDirigente(Encontro encontro) {
        this.encontro = encontro;
    }

    public Encontro getEncontro() {
        return encontro;
    }
}
