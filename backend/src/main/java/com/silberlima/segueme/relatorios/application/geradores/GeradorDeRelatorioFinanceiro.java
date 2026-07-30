package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.application.ListarLancamentosUseCase;
import com.silberlima.segueme.financeiro.domain.LancamentoFinanceiro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import com.silberlima.segueme.relatorios.domain.VarreduraPaginada;
import java.util.List;
import org.springframework.stereotype.Component;

/** Lançamentos de receita e despesa do Encontro corrente (RF-048/049 lidos como relatório). */
@Component
public class GeradorDeRelatorioFinanceiro implements GeradorDeRelatorio {

    private static final List<String> COLUNAS = List.of("Data", "Tipo", "Categoria", "Valor", "Situação");

    private final EncontroDoRelatorioResolver encontroDoRelatorioResolver;
    private final ListarLancamentosUseCase listarLancamentosUseCase;

    public GeradorDeRelatorioFinanceiro(
            EncontroDoRelatorioResolver encontroDoRelatorioResolver,
            ListarLancamentosUseCase listarLancamentosUseCase) {
        this.encontroDoRelatorioResolver = encontroDoRelatorioResolver;
        this.listarLancamentosUseCase = listarLancamentosUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.FINANCEIRO;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        Encontro encontro = encontroDoRelatorioResolver.encontroCorrente(escopo);
        List<List<String>> linhas = VarreduraPaginada
                .coletar(pageable ->
                        listarLancamentosUseCase.execute(escopo, encontro.getId(), null, null, pageable))
                .stream()
                .map(GeradorDeRelatorioFinanceiro::linha)
                .toList();
        return new ConteudoRelatorio(COLUNAS, linhas);
    }

    private static List<String> linha(LancamentoFinanceiro lancamento) {
        return List.of(
                CelulaDeRelatorio.de(lancamento.getData()),
                CelulaDeRelatorio.de(lancamento.getTipo()),
                CelulaDeRelatorio.de(lancamento.getCategoria()),
                CelulaDeRelatorio.de(lancamento.getValor()),
                CelulaDeRelatorio.de(lancamento.getSituacao()));
    }
}
