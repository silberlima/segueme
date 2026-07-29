package com.silberlima.segueme.financeiro.domain;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import com.silberlima.segueme.encontros.domain.Encontro;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Receita ou despesa de um {@link Encontro} (RF-047 a RF-049, RN-FINANCEIRO-01/02). Despesas
 * percorrem o ciclo {@link SituacaoDespesa#PENDENTE} → {@link SituacaoDespesa#APROVADA} →
 * {@link SituacaoDespesa#PAGA} via {@link #aprovar(String)}/{@link #pagar()}; receitas não têm
 * situação ({@code situacao} permanece {@code null}).
 *
 * <p>Quem aprova uma despesa não é especificado no protótipo (pendência #12 de
 * {@code pendencias-gerais.md}) — {@code plano-implementacao.md} orienta aceitar qualquer usuário
 * com {@code ROLE_EQUIPE_FINANCAS} ou {@code ROLE_PAROQUIA} até validação do PO; a verificação de
 * perfil é feita no controller, {@link #aprovar(String)} só registra o identificador informado.
 *
 * <p>Categoria é texto livre: lista fechada de categorias e forma de pagamento não são
 * especificadas no protótipo (pendência #13 de {@code pendencias-gerais.md}).
 */
@Entity
@Table(name = "lancamento_financeiro")
public class LancamentoFinanceiro extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoLancamento tipo;

    @Column(name = "categoria", nullable = false, length = 100)
    private String categoria;

    @Column(name = "valor", nullable = false, precision = 12, scale = 2)
    private BigDecimal valor;

    @Column(name = "data", nullable = false)
    private LocalDate data;

    @Enumerated(EnumType.STRING)
    @Column(name = "situacao", length = 20)
    private SituacaoDespesa situacao;

    @Column(name = "aprovado_por")
    private String aprovadoPor;

    protected LancamentoFinanceiro() {
    }

    private LancamentoFinanceiro(
            Encontro encontro, TipoLancamento tipo, String categoria, BigDecimal valor, LocalDate data) {
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessRuleViolationException("O valor do lançamento deve ser positivo.");
        }
        if (categoria == null || categoria.isBlank()) {
            throw new BusinessRuleViolationException("Todo lançamento exige uma categoria.");
        }
        this.encontro = encontro;
        this.tipo = tipo;
        this.categoria = categoria;
        this.valor = valor;
        this.data = data;
    }

    public static LancamentoFinanceiro receita(Encontro encontro, String categoria, BigDecimal valor, LocalDate data) {
        return new LancamentoFinanceiro(encontro, TipoLancamento.RECEITA, categoria, valor, data);
    }

    public static LancamentoFinanceiro despesa(Encontro encontro, String categoria, BigDecimal valor, LocalDate data) {
        LancamentoFinanceiro despesa = new LancamentoFinanceiro(encontro, TipoLancamento.DESPESA, categoria, valor, data);
        despesa.situacao = SituacaoDespesa.PENDENTE;
        return despesa;
    }

    /** RN-FINANCEIRO-02: só uma despesa pendente pode ser aprovada. */
    public void aprovar(String aprovadoPor) {
        exigirDespesa();
        if (this.situacao != SituacaoDespesa.PENDENTE) {
            throw new BusinessRuleViolationException(
                    "Só é possível aprovar uma despesa pendente (situação atual: " + this.situacao + ").");
        }
        if (aprovadoPor == null || aprovadoPor.isBlank()) {
            throw new BusinessRuleViolationException("Toda aprovação de despesa exige o identificador de quem aprovou.");
        }
        this.situacao = SituacaoDespesa.APROVADA;
        this.aprovadoPor = aprovadoPor;
    }

    /** RN-FINANCEIRO-02: só uma despesa aprovada pode ser paga. */
    public void pagar() {
        exigirDespesa();
        if (this.situacao != SituacaoDespesa.APROVADA) {
            throw new BusinessRuleViolationException(
                    "Só é possível pagar uma despesa aprovada (situação atual: " + this.situacao + ").");
        }
        this.situacao = SituacaoDespesa.PAGA;
    }

    private void exigirDespesa() {
        if (this.tipo != TipoLancamento.DESPESA) {
            throw new BusinessRuleViolationException("Apenas despesas percorrem o ciclo de situação.");
        }
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public TipoLancamento getTipo() {
        return tipo;
    }

    public String getCategoria() {
        return categoria;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public LocalDate getData() {
        return data;
    }

    public SituacaoDespesa getSituacao() {
        return situacao;
    }

    public String getAprovadoPor() {
        return aprovadoPor;
    }
}
