package com.silberlima.segueme.encontros.domain;

import com.silberlima.segueme.hierarquia.domain.RegiaoBrasil;

/**
 * Projeção Spring Data do gráfico "Encontros por região" do Painel Nacional
 * (`dashboards-por-perfil.md` 3.1). {@code regiao} nula = a diocese do Encontro não tem região
 * informada (ver {@link RegiaoBrasil}).
 */
public interface ContagemPorRegiao {

    RegiaoBrasil getRegiao();

    Long getTotal();
}
