package com.silberlima.segueme.montagem.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.Instant;

/**
 * Registro de substituição de um integrante de equipe (RF-032). **Append-only (RN-MONTAGEM-05/06):**
 * não expõe setters nem método de alteração — nasce via construtor com justificativa obrigatória
 * e preserva o histórico de ambas as pessoas envolvidas (RN-02).
 */
@Entity
@Table(name = "substituicao")
public class Substituicao extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "equipe_id", nullable = false)
    private Equipe equipe;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_sai_id", nullable = false)
    private Pessoa pessoaSai;

    @Column(name = "motivo_saida", nullable = false)
    private String motivoSaida;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pessoa_entra_id", nullable = false)
    private Pessoa pessoaEntra;

    @Column(name = "justificativa", nullable = false, length = 1000)
    private String justificativa;

    @Column(name = "data_substituicao", nullable = false)
    private Instant dataSubstituicao;

    protected Substituicao() {
    }

    public Substituicao(
            Encontro encontro,
            Equipe equipe,
            Pessoa pessoaSai,
            String motivoSaida,
            Pessoa pessoaEntra,
            String justificativa,
            Instant dataSubstituicao) {
        this.encontro = encontro;
        this.equipe = equipe;
        this.pessoaSai = pessoaSai;
        this.motivoSaida = motivoSaida;
        this.pessoaEntra = pessoaEntra;
        this.justificativa = justificativa;
        this.dataSubstituicao = dataSubstituicao;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public Equipe getEquipe() {
        return equipe;
    }

    public Pessoa getPessoaSai() {
        return pessoaSai;
    }

    public String getMotivoSaida() {
        return motivoSaida;
    }

    public Pessoa getPessoaEntra() {
        return pessoaEntra;
    }

    public String getJustificativa() {
        return justificativa;
    }

    public Instant getDataSubstituicao() {
        return dataSubstituicao;
    }
}
