package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.posencontro.application.ListarPrioridadesUseCase;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Frequência acumulada dos acompanhados e a faixa de prioridade de cada um (RF-059, RN-POS-02/03
 * lidos como relatório): a mesma apuração e a mesma ordem da tela Prioridades.
 */
@Component
public class GeradorDeRelatorioDePosEncontro implements GeradorDeRelatorio {

    private static final List<String> COLUNAS =
            List.of("Jovem", "Encontros pós", "Presenças", "Frequência (%)", "Prioridade");

    private final EncontroDoRelatorioResolver encontroDoRelatorioResolver;
    private final ListarPrioridadesUseCase listarPrioridadesUseCase;

    public GeradorDeRelatorioDePosEncontro(
            EncontroDoRelatorioResolver encontroDoRelatorioResolver,
            ListarPrioridadesUseCase listarPrioridadesUseCase) {
        this.encontroDoRelatorioResolver = encontroDoRelatorioResolver;
        this.listarPrioridadesUseCase = listarPrioridadesUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.POS_ENCONTRO;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        List<List<String>> linhas = listarPrioridadesUseCase
                .execute(encontroDoRelatorioResolver.paroquiaDoEscopo(escopo))
                .stream()
                .map(GeradorDeRelatorioDePosEncontro::linha)
                .toList();
        return new ConteudoRelatorio(COLUNAS, linhas);
    }

    private static List<String> linha(FrequenciaPessoa frequencia) {
        return List.of(
                CelulaDeRelatorio.de(frequencia.pessoa().getNomeCompleto()),
                CelulaDeRelatorio.de(frequencia.totalEncontros()),
                CelulaDeRelatorio.de(frequencia.presencas()),
                CelulaDeRelatorio.de(frequencia.percentual()),
                CelulaDeRelatorio.de(frequencia.faixa()));
    }
}
