package com.silberlima.segueme.notificacao.domain;

import com.silberlima.segueme.domain.model.AbstractAuditableEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;

/**
 * Preferência de recebimento de um tipo de notificação por usuário (RF-057, seção "Notificações"
 * de {@code sistema-relatorios-config.md}).
 *
 * <p>Só existe linha para o tipo que o usuário desligou ou religou explicitamente: a ausência de
 * registro significa "ativa", regra concentrada em {@link PreferenciasDeNotificacao}. Assim quem
 * nunca abriu Configurações continua recebendo tudo, sem precisar de seed por usuário.
 */
@Entity
@Table(name = "preferencia_notificacao")
public class PreferenciaNotificacao extends AbstractAuditableEntity {

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoNotificacao tipo;

    @Column(name = "ativo", nullable = false)
    private boolean ativo = true;

    protected PreferenciaNotificacao() {
    }

    public PreferenciaNotificacao(String usuarioId, TipoNotificacao tipo, boolean ativo) {
        this.usuarioId = usuarioId;
        this.tipo = tipo;
        this.ativo = ativo;
    }

    public void definir(boolean ativo) {
        this.ativo = ativo;
    }

    public String getUsuarioId() {
        return usuarioId;
    }

    public TipoNotificacao getTipo() {
        return tipo;
    }

    public boolean isAtivo() {
        return ativo;
    }
}
