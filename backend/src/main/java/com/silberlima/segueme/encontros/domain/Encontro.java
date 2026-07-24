package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;

/**
 * O objeto central de operação de uma paróquia (RF-014 a RF-021). Numeração sequencial por
 * paróquia (RN-16), nasce {@link StatusEncontro#ATIVO} com a Equipe Dirigente já montada e
 * entregue à Equipe Comando (RN-21).
 */
@Entity
@Table(name = "encontro")
public class Encontro extends AbstractAuditableEntity {

    @Column(name = "numero", nullable = false)
    private int numero;

    @Column(name = "padroeiro", nullable = false)
    private String padroeiro;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diretor_espiritual_id", nullable = false)
    private Pessoa diretorEspiritual;

    @Column(name = "local", nullable = false)
    private String local;

    @Column(name = "data_inicio", nullable = false)
    private LocalDate dataInicio;

    @Column(name = "data_termino", nullable = false)
    private LocalDate dataTermino;

    @Column(name = "vagas", nullable = false)
    private int vagas;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paroquia_id", nullable = false)
    private Paroquia paroquia;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusEncontro status = StatusEncontro.ATIVO;

    /**
     * Lado inverso, somente leitura: permite fetch join do coordenador geral na Lista de
     * Encontros (RF-014) sem uma segunda consulta por item — mesmo padrão de
     * {@code PessoaSpecification.comParoquiaCarregada()}.
     */
    @OneToOne(mappedBy = "encontro", fetch = FetchType.LAZY)
    private EquipeComando equipeComando;

    protected Encontro() {
    }

    public Encontro(
            int numero,
            String padroeiro,
            Pessoa diretorEspiritual,
            String local,
            LocalDate dataInicio,
            LocalDate dataTermino,
            int vagas,
            Paroquia paroquia) {
        this.numero = numero;
        this.padroeiro = padroeiro;
        this.diretorEspiritual = diretorEspiritual;
        this.local = local;
        this.dataInicio = dataInicio;
        this.dataTermino = dataTermino;
        this.vagas = vagas;
        this.paroquia = paroquia;
    }

    public int getNumero() {
        return numero;
    }

    public String getPadroeiro() {
        return padroeiro;
    }

    public Pessoa getDiretorEspiritual() {
        return diretorEspiritual;
    }

    public String getLocal() {
        return local;
    }

    public LocalDate getDataInicio() {
        return dataInicio;
    }

    public LocalDate getDataTermino() {
        return dataTermino;
    }

    public int getVagas() {
        return vagas;
    }

    public Paroquia getParoquia() {
        return paroquia;
    }

    public StatusEncontro getStatus() {
        return status;
    }

    public EquipeComando getEquipeComando() {
        return equipeComando;
    }
}
