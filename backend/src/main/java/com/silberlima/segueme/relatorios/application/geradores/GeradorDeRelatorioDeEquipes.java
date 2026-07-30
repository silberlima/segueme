package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * Composição das equipes do Encontro corrente com o estado do convite de cada integrante
 * (RF-028/036 lidos como relatório).
 *
 * <p>Equipe sem ninguém aparece mesmo assim, com a linha de vagas zerada: o relatório serve para
 * enxergar o que falta montar, então omitir a equipe vazia esconderia justamente o problema.
 */
@Component
public class GeradorDeRelatorioDeEquipes implements GeradorDeRelatorio {

    private static final List<String> COLUNAS = List.of(
            "Equipe", "Vagas abertas", "Integrante", "Papel", "Convite", "Enviado em", "Respondido em");
    private static final String SEM_INTEGRANTES = "(sem integrantes)";

    private final EncontroDoRelatorioResolver encontroDoRelatorioResolver;
    private final ListarEquipesUseCase listarEquipesUseCase;

    public GeradorDeRelatorioDeEquipes(
            EncontroDoRelatorioResolver encontroDoRelatorioResolver, ListarEquipesUseCase listarEquipesUseCase) {
        this.encontroDoRelatorioResolver = encontroDoRelatorioResolver;
        this.listarEquipesUseCase = listarEquipesUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.EQUIPES;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        Encontro encontro = encontroDoRelatorioResolver.encontroCorrente(escopo);
        List<List<String>> linhas = listarEquipesUseCase.execute(encontro.getId()).stream()
                .flatMap(composicao -> linhasDaEquipe(composicao).stream())
                .toList();
        return new ConteudoRelatorio(COLUNAS, linhas);
    }

    private static List<List<String>> linhasDaEquipe(EquipeComposicao composicao) {
        String equipe = composicao.equipe().getNome();
        String vagasAbertas = CelulaDeRelatorio.de(composicao.vagasAbertas());

        if (composicao.integrantes().isEmpty()) {
            return List.of(List.of(equipe, vagasAbertas, SEM_INTEGRANTES, "", "", "", ""));
        }
        return composicao.integrantes().stream()
                .map(integrante -> linhaDoIntegrante(equipe, vagasAbertas, integrante))
                .toList();
    }

    private static List<String> linhaDoIntegrante(
            String equipe, String vagasAbertas, IntegranteEquipe integrante) {
        return List.of(
                equipe,
                vagasAbertas,
                CelulaDeRelatorio.de(integrante.getPessoa().getNomeCompleto()),
                CelulaDeRelatorio.de(integrante.getPapel()),
                CelulaDeRelatorio.de(integrante.getStatus()),
                CelulaDeRelatorio.de(integrante.getDataEnvioConvite()),
                CelulaDeRelatorio.de(integrante.getDataResposta()));
    }
}
