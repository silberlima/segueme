package com.silberlima.segueme.pessoas.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
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

    /**
     * Chave natural do cadastro nacional (RN-PESSOA-01): é o CPF que impede que a mesma pessoa
     * apareça duas vezes, coisa que a combinação nome + data de nascimento nunca garantiu.
     * Guardado só com os dígitos — ver {@link #normalizarCpf(String)}.
     */
    @Column(name = "cpf", nullable = false, length = 11, unique = true)
    private String cpf;

    @Column(name = "data_nascimento", nullable = false)
    private LocalDate dataNascimento;

    @Column(name = "telefone", nullable = false)
    private String telefone;

    /** Perfil do Instagram, sem o {@code @} — opcional: nem todo mundo tem ou quer informar. */
    @Column(name = "instagram", length = 30)
    private String instagram;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_civil", nullable = false, length = 20)
    private EstadoCivil estadoCivil;

    @Embedded
    private Endereco endereco;

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

    /**
     * Usuário do Keycloak que é esta pessoa (o {@code sub} do token). Nulo na maioria dos
     * registros: quase ninguém do cadastro nacional acessa o sistema. É por aqui que o perfil
     * Seguidor encontra os próprios dados.
     */
    @Column(name = "usuario_id", length = 100)
    private String usuarioId;

    protected Pessoa() {
    }

    public Pessoa(
            String nomeCompleto,
            String cpf,
            LocalDate dataNascimento,
            String telefone,
            String instagram,
            EstadoCivil estadoCivil,
            Paroquia paroquia,
            Endereco endereco,
            String alergias,
            String restricaoAlimentar,
            String medicacaoUsoContinuo,
            String contatoEmergenciaNome,
            String contatoEmergenciaTelefone,
            String contatoEmergenciaParentesco,
            Pessoa indicadoPor) {
        this.nomeCompleto = nomeCompleto;
        this.cpf = normalizarCpf(cpf);
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.instagram = normalizarInstagram(instagram);
        this.estadoCivil = estadoCivil;
        this.paroquia = paroquia;
        this.endereco = endereco;
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
     * Forma canônica do CPF: só os 11 dígitos. Guardar o que veio digitado deixaria
     * {@code 529.982.247-25} e {@code 52998224725} conviverem como pessoas diferentes, e a
     * unicidade — que é a razão de o campo existir — não valeria nada.
     */
    public static String normalizarCpf(String cpf) {
        return cpf == null ? null : cpf.replaceAll("\\D", "");
    }

    /** O handle sem {@code @} e sem espaços; em branco vira nulo, porque o campo é opcional. */
    private static String normalizarInstagram(String instagram) {
        if (instagram == null || instagram.isBlank()) {
            return null;
        }
        return instagram.trim().replaceFirst("^@", "");
    }

    /**
     * Atualização que a própria pessoa faz dos seus dados, pelo perfil Seguidor: os dados que ela
     * é a fonte mais confiável sobre si mesma — CPF, data de nascimento, telefone, Instagram,
     * endereço, saúde e contato de emergência.
     *
     * <p>O que continua fora, e por quê:
     *
     * <ul>
     *   <li><b>nome</b> é como o movimento a identifica em fichas, equipes e histórico; corrigi-lo
     *       é trabalho de quem administra o cadastro, não autoatendimento;
     *   <li><b>paróquia</b> só muda por Transferência aprovada pelo Setorial (RN-MOVIMENTO-01) —
     *       deixar a pessoa trocar aqui contornaria toda a mediação;
     *   <li><b>situação</b> é consequência do fluxo de inscrição, nunca uma escolha;
     *   <li><b>competências</b> alimentam as Sugestões Inteligentes da Montagem (RN-08); permitir
     *       autoatribuição deixaria o ranqueamento à mercê de quem quer ser escalado.
     * </ul>
     *
     * <p>O CPF é editável mas continua único no cadastro: quem garante que a troca não colide com
     * outra pessoa é {@code AtualizarMeusDadosUseCase}, antes de chegar aqui.
     */
    public void atualizarDadosPessoais(
            String cpf,
            LocalDate dataNascimento,
            String telefone,
            String instagram,
            Endereco endereco,
            String alergias,
            String restricaoAlimentar,
            String medicacaoUsoContinuo,
            String contatoEmergenciaNome,
            String contatoEmergenciaTelefone,
            String contatoEmergenciaParentesco) {
        this.cpf = normalizarCpf(cpf);
        this.dataNascimento = dataNascimento;
        this.telefone = telefone;
        this.instagram = normalizarInstagram(instagram);
        this.endereco = endereco;
        this.alergias = alergias;
        this.restricaoAlimentar = restricaoAlimentar;
        this.medicacaoUsoContinuo = medicacaoUsoContinuo;
        this.contatoEmergenciaNome = contatoEmergenciaNome;
        this.contatoEmergenciaTelefone = contatoEmergenciaTelefone;
        this.contatoEmergenciaParentesco = contatoEmergenciaParentesco;
    }

    /**
     * Efetiva uma Transferência aprovada pelo Setorial (RF-041, RN-MOVIMENTO-01): muda o vínculo
     * definitivo da pessoa para a nova paróquia. O histórico da pessoa (Fichas, Encontros,
     * Equipes anteriores) é preservado por não referenciar a paróquia diretamente (RN-MOVIMENTO-03).
     */
    public void transferirParaParoquia(Paroquia novaParoquia) {
        this.paroquia = novaParoquia;
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

    public String getCpf() {
        return cpf;
    }

    public LocalDate getDataNascimento() {
        return dataNascimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public String getInstagram() {
        return instagram;
    }

    public EstadoCivil getEstadoCivil() {
        return estadoCivil;
    }

    public Endereco getEndereco() {
        return endereco;
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

    public String getUsuarioId() {
        return usuarioId;
    }
}
