package com.silberlima.segueme.relatorios.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.acesso.domain.Perfil;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Cobre quem pode gerar cada tipo (RF-056). A lista dos seis tipos é a decisão documentada da
 * Fase 13 para a pendência #15-sistema — este teste é o que trava mudança silenciosa nela.
 */
class TipoRelatorioTest {

    @Test
    void shouldOferecerOsSeisTiposAoPerfilParoquia() {
        assertThat(TipoRelatorio.disponiveisPara(List.of(Perfil.PAROQUIA)))
                .containsExactly(TipoRelatorio.values());
    }

    @Test
    void shouldRestringirOsTiposDeEncontroAoPerfilDaEquipe() {
        assertThat(TipoRelatorio.disponiveisPara(List.of(Perfil.EQUIPE_FINANCAS)))
                .containsExactly(TipoRelatorio.FINANCEIRO);
    }

    @Test
    void shouldNaoOferecerRelatorioAoSeguidor() {
        assertThat(TipoRelatorio.disponiveisPara(List.of(Perfil.SEGUIDOR))).isEmpty();
    }

    @Test
    void shouldSomarOsTiposQuandoOUsuarioTemMaisDeUmPerfil() {
        assertThat(TipoRelatorio.disponiveisPara(List.of(Perfil.POS_ENCONTRO, Perfil.EQUIPE_FICHAS)))
                .containsExactlyInAnyOrder(
                        TipoRelatorio.PESSOAS, TipoRelatorio.INSCRICOES, TipoRelatorio.POS_ENCONTRO);
    }

    @Test
    void shouldRecusarOTipoQueOPerfilNaoPodeGerar() {
        assertThat(TipoRelatorio.FINANCEIRO.autorizadoPara(List.of(Perfil.POS_ENCONTRO))).isFalse();
    }
}
