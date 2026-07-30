package com.silberlima.segueme.relatorios.domain;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;

/**
 * Contrato de um tipo de relatório: dado o escopo territorial do solicitante (RN-04), produz a
 * tabela do arquivo. Uma implementação por {@link TipoRelatorio}, resolvida por
 * {@code GerarRelatorioUseCase} — acrescentar um tipo é acrescentar um gerador, sem tocar no
 * fluxo de solicitação nem no de download.
 */
public interface GeradorDeRelatorio {

    TipoRelatorio suporta();

    ConteudoRelatorio gerar(EscopoTerritorial escopo);
}
