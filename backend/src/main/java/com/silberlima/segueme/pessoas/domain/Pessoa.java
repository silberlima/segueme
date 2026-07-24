package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

/**
 * Cadastro nacional único de uma pessoa (RN-PESSOA-01): nunca duplicado por paróquia, nasce
 * com {@link SituacaoPessoa#PRE_INSCRITO} (RN-PESSOA-04) e mantém seu histórico via
 * {@link HistoricoEvento}, que nunca é apagado (RN-PESSOA-02).
 */
@Entity
@Table(name = "pessoa")
public class Pessoa extends AbstractAuditableEntity {

    @Column(name = "nome_completo", nullable = false)
    private String nomeCompleto;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "telefone", nullable = false)
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 20)
    private EstadoCivil estadoCivil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "paroquia_id", nullable = false)
    private Paroquia paroquia;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", nullable = false, length = 20)
    private SituacaoPessoa situacao = SituacaoPessoa.PRE_INSCRITO;

    @Column(name = "alergias")
    private String alergias;

    @Column(name = "restricao_alimentar")
    private String restricaoAlimentar;

    @Column(name = "medicacao_uso_continuo")
    private String medicacaoUsoContinuo;

    @Column(name = "contato_emergencia_nome")
    private String contatoEmergenciaNome;

    @Column(name = "contato_emergencia_telefone")
    private String contatoEmergenciaTelefone;

    @Column(name = "contato_emergencia_parentesco")
    private String contatoEmergenciaParentesco;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "indicado_por_id")
    private Pessoa indicadoPor;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "pessoa_competencia",
            joinColumns = @JoinColumn(name = "pessoa_id"),
            inverseJoinColumns = @JoinColumn(name = "competencia_id"))
    private Set<Competencia> competencias = new HashSet<>();

    protected Pessoa() {
    }

    public Pessoa(
            String nomeCompleto,
            LocalDate dataNascimento,
            String telefone,
            EstadoCivil estadoCivil,
            Paroquia paroquia,
            String alergias,
            String restricaoAlimentar,
            String medicacaoUsoContinuo,
            String contatoEmergenciaNome,
            String contatoEmergenciaTelefone,
            String contatoEmergenciaParentesco,
            Pessoa indicadoPor) {
        this.nomeCompleto = nomeCompleto;
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.estadoCivil = estadoCivil;
        this.paroquia = paroquia;
        this.alergias = alergias;
        this.restricaoAlimentar = restricaoAlimentar;
        this.medicacaoUsoContinuo = medicacaoUsoContinuo;
        this.contatoEmergenciaNome = contatoEmergenciaNome;
        this.contatoEmergenciaTelefone = contatoEmergenciaTelefone;
        this.contatoEmergenciaParentesco = contatoEmergenciaParentesco;
        this.indicadoPor = indicadoPor;
    }

    public void adicionarCompetencias(Set<Competencia> novasCompetencias) {
        this.competencias.addAll(novasCompetencias);
    }

    /**
     * Confirma a Ficha de Inscrição da pessoa, promovendo-a de Pré-inscrito a Seguimista
     * (RN-INSCRICAO-02). {@code SituacaoPessoa.INSCRITO} não é usado nesta transição: o RN é
     * literal (Pré-inscrito → Seguimista), sem estado intermediário definido pelas regras/casos
     * de uso da Fase 5.
     */
    public void confirmarComoSeguimista() {
        if (situacao != SituacaoPessoa.PRE_INSCRITO) {
            throw new BusinessRuleViolationException(
                    "Só é possível confirmar como Seguimista uma pessoa Pré-inscrita (situação atual: "
                            + situacao + ").");
        }
        this.situacao = SituacaoPessoa.SEGUIMISTA;
    }

    public String getNomeCompleto() {
        return nomeCompleto;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public EstadoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public Paroquia getParoquia() {
        return paroquia;
    }

    public SituacaoPessoa getSituacao() {
        return situacao;
    }

    public String getAlergias() {
        return alergias;
    }

    public String getRestricaoAlimentar() {
        return restricaoAlimentar;
    }

    public String getMedicacaoUsoContinuo() {
        return medicacaoUsoContinuo;
    }

    public String getContatoEmergenciaNome() {
        return contatoEmergenciaNome;
    }

    public String getContatoEmergenciaTelefone() {
        return contatoEmergenciaTelefone;
    }

    public String getContatoEmergenciaParentesco() {
        return contatoEmergenciaParentesco;
    }

    public Pessoa getIndicadoPor() {
        return indicadoPor;
    }

    public Set<Competencia> getCompetencias() {
        return Collections.unmodifiableSet(competencias);
    }
}
