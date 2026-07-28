package com.silberlima.segueme.montagem.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
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
import java.time.Instant;

/**
 * Vínculo de uma Pessoa a uma {@link Equipe} (tipo do catálogo nacional) num Encontro específico
 * (RF-028, RF-031). Nasce sempre {@link StatusIntegrante#PENDENTE}, com {@code dataEnvioConvite}
 * marcada no instante da criação — este registro É o convite (Fase 7, RN-CONVITE-01/02):
 * {@link #aceitar()} e {@link #recusar(String)} implementam a transição PENDENTE → ACEITO/RECUSADO
 * decidida pela própria pessoa convidada (mediada por staff — ver pendência em
 * {@code docs/pendencias-gerais.md}), enquanto {@link #marcarRecusado()} continua sendo o caminho
 * usado por {@code RegistrarSubstituicaoUseCase} quando é a Equipe Montagem, e não o convidado,
 * quem decide encerrar o vínculo (RN-MONTAGEM-06).
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

    @Column(name = "data_envio_convite")
    private Instant dataEnvioConvite;

    @Column(name = "data_resposta")
    private Instant dataResposta;

    @Column(name = "justificativa_recusa", length = 1000)
    private String justificativaRecusa;

    @Column(name = "reenvios", nullable = false)
    private int reenvios = 0;

    protected IntegranteEquipe() {
    }

    public IntegranteEquipe(Encontro encontro, Equipe equipe, Pessoa pessoa, PapelIntegrante papel) {
        this.encontro = encontro;
        this.equipe = equipe;
        this.pessoa = pessoa;
        this.papel = papel;
        this.status = StatusIntegrante.PENDENTE;
        this.dataEnvioConvite = Instant.now();
    }

    /** Usado exclusivamente por {@code RegistrarSubstituicaoUseCase} (RN-MONTAGEM-06). */
    public void marcarRecusado() {
        this.status = StatusIntegrante.RECUSADO;
    }

    /** RF-036: o convite é aceito pela pessoa convidada (RN-CONVITE-01). */
    public void aceitar() {
        exigirPendente();
        this.status = StatusIntegrante.ACEITO;
        this.dataResposta = Instant.now();
    }

    /** RF-037: recusa exige justificativa obrigatória (RN-CONVITE-02). */
    public void recusar(String justificativa) {
        exigirPendente();
        if (justificativa == null || justificativa.isBlank()) {
            throw new BusinessRuleViolationException("Toda recusa de convite exige justificativa (RN-CONVITE-02).");
        }
        this.status = StatusIntegrante.RECUSADO;
        this.justificativaRecusa = justificativa;
        this.dataResposta = Instant.now();
    }

    /** RF-039: reenvia um convite ainda pendente, reiniciando a contagem de tempo de espera. */
    public void reenviarConvite() {
        exigirPendente();
        this.reenvios++;
        this.dataEnvioConvite = Instant.now();
    }

    private void exigirPendente() {
        if (this.status != StatusIntegrante.PENDENTE) {
            throw new BusinessRuleViolationException(
                    "Só é possível responder ou reenviar um convite pendente (status atual: " + this.status + ").");
        }
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

    public Instant getDataEnvioConvite() {
        return dataEnvioConvite;
    }

    public Instant getDataResposta() {
        return dataResposta;
    }

    public String getJustificativaRecusa() {
        return justificativaRecusa;
    }

    public int getReenvios() {
        return reenvios;
    }
}
