package com.silberlima.segueme.relatorios.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/**
 * Cobre o ciclo de vida do relatório e o escopo congelado no pedido — sem ele a geração
 * assíncrona não teria como saber sobre que território apurar (RN-04).
 */
class RelatorioTest {

    @Test
    void shouldGuardarOEscopoDeParoquiaDoSolicitante() {
        UUID paroquiaId = UUID.randomUUID();

        Relatorio relatorio = new Relatorio(
                TipoRelatorio.POS_ENCONTRO, "user-a", new EscopoTerritorial.DeParoquia(paroquiaId));

        assertThat(relatorio.getEscopoNivel()).isEqualTo(NivelDoEscopo.PAROQUIA);
        assertThat(relatorio.escopo()).isEqualTo(new EscopoTerritorial.DeParoquia(paroquiaId));
    }

    @Test
    void shouldGuardarOEscopoNacionalSemTerritorio() {
        Relatorio relatorio =
                new Relatorio(TipoRelatorio.PESSOAS, "user-a", new EscopoTerritorial.Nacional());

        assertThat(relatorio.getEscopoId()).isNull();
        assertThat(relatorio.escopo()).isEqualTo(new EscopoTerritorial.Nacional());
    }

    @Test
    void shouldNascerPendente() {
        Relatorio relatorio =
                new Relatorio(TipoRelatorio.PESSOAS, "user-a", new EscopoTerritorial.Nacional());

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.PENDENTE);
        assertThat(relatorio.getConteudo()).isNull();
    }

    @Test
    void shouldConcluirGuardandoConteudoETotalDeLinhas() {
        Relatorio relatorio =
                new Relatorio(TipoRelatorio.PESSOAS, "user-a", new EscopoTerritorial.Nacional());

        relatorio.concluir(new ConteudoRelatorio(List.of("Nome"), List.of(List.of("Ana"), List.of("Bruno"))));

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.CONCLUIDO);
        assertThat(relatorio.getTotalLinhas()).isEqualTo(2);
        assertThat(relatorio.getConteudo()).isEqualTo("Nome\nAna\nBruno");
        assertThat(relatorio.getGeradoEm()).isNotNull();
    }

    @Test
    void shouldRegistrarOMotivoDaFalha() {
        Relatorio relatorio =
                new Relatorio(TipoRelatorio.FINANCEIRO, "user-a", new EscopoTerritorial.Nacional());

        relatorio.falhar("A paróquia ainda não tem nenhum Encontro para relatar.");

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.FALHOU);
        assertThat(relatorio.getErro()).isEqualTo("A paróquia ainda não tem nenhum Encontro para relatar.");
    }

    @Test
    void shouldReconhecerSomenteOProprioSolicitante() {
        Relatorio relatorio =
                new Relatorio(TipoRelatorio.PESSOAS, "user-a", new EscopoTerritorial.Nacional());

        assertThat(relatorio.pertenceA("user-a")).isTrue();
        assertThat(relatorio.pertenceA("user-b")).isFalse();
    }
}
