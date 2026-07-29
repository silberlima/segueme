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

/**
 * Campanha de arrecadação de um {@link Encontro} (ex.: festa junina, rifa solidária — RF-050,
 * RN-FINANCEIRO-03). Nasce {@link StatusCampanha#ATIVA} com {@code valorArrecadado} zerado;
 * {@link #encerrar()} preserva o resultado final ("encerrada · meta atingida", `financeiro.md`
 * 2.4).
 *
 * <p>O vínculo entre uma Receita de {@link LancamentoFinanceiro} e uma Campanha (lançamento
 * automático vs. manual) não é especificado no protótipo (`financeiro.md` §6) — não modelado aqui;
 * a arrecadação é registrada diretamente via {@link #registrarArrecadacao(BigDecimal)}.
 */
@Entity
@Table(name = "campanha")
public class Campanha extends AbstractAuditableEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "encontro_id", nullable = false)
    private Encontro encontro;

    @Column(name = "nome", nullable = false, length = 200)
    private String nome;

    @Column(name = "meta", nullable = false, precision = 12, scale = 2)
    private BigDecimal meta;

    @Column(name = "valor_arrecadado", nullable = false, precision = 12, scale = 2)
    private BigDecimal valorArrecadado = BigDecimal.ZERO;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusCampanha status;

    protected Campanha() {
    }

    public Campanha(Encontro encontro, String nome, BigDecimal meta) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleViolationException("Toda campanha exige um nome.");
        }
        if (meta == null || meta.signum() <= 0) {
            throw new BusinessRuleViolationException("A meta da campanha deve ser positiva.");
        }
        this.encontro = encontro;
        this.nome = nome;
        this.meta = meta;
        this.status = StatusCampanha.ATIVA;
    }

    public void registrarArrecadacao(BigDecimal valor) {
        if (this.status != StatusCampanha.ATIVA) {
            throw new BusinessRuleViolationException("Só é possível registrar arrecadação numa campanha ativa.");
        }
        if (valor == null || valor.signum() <= 0) {
            throw new BusinessRuleViolationException("O valor arrecadado deve ser positivo.");
        }
        this.valorArrecadado = this.valorArrecadado.add(valor);
    }

    public void encerrar() {
        if (this.status == StatusCampanha.ENCERRADA) {
            throw new BusinessRuleViolationException("A campanha já está encerrada.");
        }
        this.status = StatusCampanha.ENCERRADA;
    }

    public Encontro getEncontro() {
        return encontro;
    }

    public String getNome() {
        return nome;
    }

    public BigDecimal getMeta() {
        return meta;
    }

    public BigDecimal getValorArrecadado() {
        return valorArrecadado;
    }

    public StatusCampanha getStatus() {
        return status;
    }
}
