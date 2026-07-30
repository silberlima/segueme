package com.silberlima.segueme.hierarquia.domain;

/**
 * Região do país a que a Diocese pertence. Existe para o gráfico "Encontros por região" do Painel
 * Nacional (`dashboards-por-perfil.md` 3.1), que agrupa nas cinco regiões oficiais do IBGE — a
 * ordem dos valores é a mesma em que o protótipo exibe as barras.
 *
 * <p>O atributo é opcional na Diocese: a hierarquia territorial (Fase 2) não tem tela de cadastro
 * (pendência #25 de {@code pendencias-gerais.md}), então dioceses já existentes ficam sem região
 * até que o dado seja informado — o painel as agrupa como "Não informada".
 */
public enum RegiaoBrasil {

    NORDESTE,
    SUDESTE,
    SUL,
    CENTRO_OESTE,
    NORTE
}
