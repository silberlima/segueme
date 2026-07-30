package com.silberlima.segueme.notificacao.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

/** Cobre o default "sem registro = recebe" das preferências de notificação (RF-057). */
class PreferenciasDeNotificacaoTest {

    @Test
    void shouldAtivarTodosOsTiposQuandoUsuarioNuncaEscolheu() {
        PreferenciasDeNotificacao preferencias = PreferenciasDeNotificacao.de(List.of());

        assertThat(preferencias.ativa(TipoNotificacao.INFORMATIVA)).isTrue();
        assertThat(preferencias.ativa(TipoNotificacao.POSITIVA)).isTrue();
        assertThat(preferencias.ativa(TipoNotificacao.NEGATIVA)).isTrue();
    }

    @Test
    void shouldRespeitarOTipoDesligadoExplicitamente() {
        PreferenciasDeNotificacao preferencias = PreferenciasDeNotificacao.de(
                List.of(new PreferenciaNotificacao("user-a", TipoNotificacao.NEGATIVA, false)));

        assertThat(preferencias.ativa(TipoNotificacao.NEGATIVA)).isFalse();
        assertThat(preferencias.ativa(TipoNotificacao.POSITIVA)).isTrue();
    }

    @Test
    void shouldExporOsTresTiposMesmoComUmSoRegistro() {
        PreferenciasDeNotificacao preferencias = PreferenciasDeNotificacao.de(
                List.of(new PreferenciaNotificacao("user-a", TipoNotificacao.INFORMATIVA, false)));

        assertThat(preferencias.porTipo()).containsOnlyKeys(TipoNotificacao.values());
    }
}
