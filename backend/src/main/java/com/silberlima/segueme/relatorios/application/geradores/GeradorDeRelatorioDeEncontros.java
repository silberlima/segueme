package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.encontros.application.ListarEncontrosUseCase;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import com.silberlima.segueme.relatorios.domain.VarreduraPaginada;
import java.util.List;
import org.springframework.stereotype.Component;

/** Encontros do escopo territorial do solicitante (RF-014 lido como relatório). */
@Component
public class GeradorDeRelatorioDeEncontros implements GeradorDeRelatorio {

    private static final List<String> COLUNAS =
            List.of("Número", "Paróquia", "Início", "Término", "Local", "Vagas", "Situação");

    private final ListarEncontrosUseCase listarEncontrosUseCase;

    public GeradorDeRelatorioDeEncontros(ListarEncontrosUseCase listarEncontrosUseCase) {
        this.listarEncontrosUseCase = listarEncontrosUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.ENCONTROS;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        List<List<String>> linhas = VarreduraPaginada
                .coletar(pageable -> listarEncontrosUseCase.execute(escopo, null, null, null, pageable))
                .stream()
                .map(GeradorDeRelatorioDeEncontros::linha)
                .toList();
        return new ConteudoRelatorio(COLUNAS, linhas);
    }

    private static List<String> linha(Encontro encontro) {
        return List.of(
                CelulaDeRelatorio.de(encontro.getNumero()),
                CelulaDeRelatorio.de(encontro.getParoquia().getNome()),
                CelulaDeRelatorio.de(encontro.getDataInicio()),
                CelulaDeRelatorio.de(encontro.getDataTermino()),
                CelulaDeRelatorio.de(encontro.getLocal()),
                CelulaDeRelatorio.de(encontro.getVagas()),
                CelulaDeRelatorio.de(encontro.getStatus()));
    }
}
