package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.application.ListarPessoasUseCase;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.relatorios.domain.CelulaDeRelatorio;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import com.silberlima.segueme.relatorios.domain.VarreduraPaginada;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/** Cadastro de pessoas do escopo territorial do solicitante (RF-007 lido como relatório). */
@Component
public class GeradorDeRelatorioDePessoas implements GeradorDeRelatorio {

    private static final List<String> COLUNAS = List.of(
            "Nome", "Paróquia", "Situação", "Nascimento", "Telefone", "Estado civil", "Competências");

    private final ListarPessoasUseCase listarPessoasUseCase;

    public GeradorDeRelatorioDePessoas(ListarPessoasUseCase listarPessoasUseCase) {
        this.listarPessoasUseCase = listarPessoasUseCase;
    }

    @Override
    public TipoRelatorio suporta() {
        return TipoRelatorio.PESSOAS;
    }

    @Override
    public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
        List<List<String>> linhas = VarreduraPaginada
                .coletar(pageable -> listarPessoasUseCase.execute(escopo, null, null, null, pageable))
                .stream()
                .map(GeradorDeRelatorioDePessoas::linha)
                .toList();
        return new ConteudoRelatorio(COLUNAS, linhas);
    }

    private static List<String> linha(Pessoa pessoa) {
        return List.of(
                CelulaDeRelatorio.de(pessoa.getNomeCompleto()),
                CelulaDeRelatorio.de(pessoa.getParoquia().getNome()),
                CelulaDeRelatorio.de(pessoa.getSituacao()),
                CelulaDeRelatorio.de(pessoa.getDataNascimento()),
                CelulaDeRelatorio.de(pessoa.getTelefone()),
                CelulaDeRelatorio.de(pessoa.getEstadoCivil()),
                competencias(pessoa));
    }

    private static String competencias(Pessoa pessoa) {
        return pessoa.getCompetencias().stream()
                .map(Competencia::getNome)
                .sorted()
                .collect(Collectors.joining(", "));
    }
}
