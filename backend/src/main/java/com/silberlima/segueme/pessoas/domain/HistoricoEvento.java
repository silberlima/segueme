package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Evento da linha do tempo de uma Pessoa (`cadastro-de-pessoas.md` 2.4). **Append-only
 * (RN-PESSOA-02):** não expõe setters nem método de alteração — só nasce via construtor e é
 * lido, nunca atualizado ou removido.
 */
@Entity
@Table(name = "historico_evento")
public class HistoricoEvento extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_id", nullable = false)
    private Pessoa pessoa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoEventoHistorico tipo;

    @Column(name = "data_evento", nullable = false)
    private Instant dataEvento;

    @Column(name = "descricao")
    private String descricao;

    protected HistoricoEvento() {
    }

    public HistoricoEvento(Pessoa pessoa, TipoEventoHistorico tipo, Instant dataEvento, String descricao) {
        this.pessoa = pessoa;
        this.tipo = tipo;
        this.dataEvento = dataEvento;
        this.descricao = descricao;
    }

    public Pessoa getPessoa() {
        return pessoa;
    }

    public TipoEventoHistorico getTipo() {
        return tipo;
    }

    public Instant getDataEvento() {
        return dataEvento;
    }

    public String getDescricao() {
        return descricao;
    }
}
