package com.silberlima.segueme.notificacao.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import java.time.Instant;

/** RN-SISTEMA-01/02: notificação dirigida a um usuário, com tipo e estado lida/não lida. */
@Entity
@Table(name = "notificacao")
public class Notificacao extends AbstractAuditableEntity {

    @Column(name = "destinatario_id", nullable = false)
    private String destinatarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotificacao tipo;

    @Column(name = "titulo", nullable = false)
    private String titulo;

    @Column(name = "mensagem", nullable = false)
    private String mensagem;

    @Column(name = "lida", nullable = false)
    private boolean lida = false;

    @Column(name = "lida_em")
    private Instant lidaEm;

    protected Notificacao() {
    }

    public Notificacao(String destinatarioId, TipoNotificacao tipo, String titulo, String mensagem) {
        this.destinatarioId = destinatarioId;
        this.tipo = tipo;
        this.titulo = titulo;
        this.mensagem = mensagem;
    }

    public void marcarComoLida() {
        if (!this.lida) {
            this.lida = true;
            this.lidaEm = Instant.now();
        }
    }

    public String getDestinatarioId() {
        return destinatarioId;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public boolean isLida() {
        return lida;
    }

    public Instant getLidaEm() {
        return lidaEm;
    }
}
