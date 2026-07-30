package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.inscricoes.application.ConsultarFunilUseCase;
import com.silberlima.segueme.inscricoes.application.dto.FunilInscricao;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Funil de inscrição do Encontro corrente da paróquia (RF-027 lido como relatório): uma linha por
 * etapa, na mesma ordem da tela.
 */
@Component
public class GeradorDeRelatorioDeInscricoes implements GeradorDeRelatorio {

    private static final List<String> COLUNAS = List.of("Encontro", "Etapa", "Total");

    private final EncontroDoRelatorioResolver encontroDoRelatorioResolver;
    private final ConsultarFunilUseCase consultarFunilUseCase;

    public GeradorDeRelatorioDeInscricoes(
            EncontroDoRelatorioResolver encontroDoRelatorioResolver, ConsultarFunilUseCase consultarFunilUseCase) {
        this.encontroDoRelatorioResolver = encontroDoRelatorioResolver;
        this.consultarFunilUseCase = consultarFunilUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.INSCRICOES;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        Encontro encontro = encontroDoRelatorioResolver.encontroCorrente(escopo);
        FunilInscricao funil = consultarFunilUseCase.execute(encontro.getId());
        String rotuloDoEncontro = "Encontro " + encontro.getNumero() + " — " + encontro.getParoquia().getNome();

        return new ConteudoRelatorio(
                COLUNAS,
                List.of(
                        etapa(rotuloDoEncontro, "Pré-inscritos", funil.preInscritos()),
                        etapa(rotuloDoEncontro, "Em análise", funil.emAnalise()),
                        etapa(rotuloDoEncontro, "Confirmados", funil.confirmados()),
                        etapa(rotuloDoEncontro, "Em reserva", funil.emReserva()),
                        etapa(rotuloDoEncontro, "Seguidores", funil.seguidores())));
    }

    private static List<String> etapa(String encontro, String nome, long total) {
        return List.of(encontro, nome, CelulaDeRelatorio.de(total));
    }
}
