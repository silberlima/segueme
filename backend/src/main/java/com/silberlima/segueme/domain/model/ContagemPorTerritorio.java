package com.silberlima.segueme.domain.model;

import java.util.UUID;

/**
 * Projeção Spring Data para uma contagem agrupada por um nível territorial — diocese, setor ou
 * paróquia, conforme a consulta que a produz. Vive no núcleo compartilhado porque os gráficos e
 * rankings dos dashboards por perfil (RF-053) agregam por território sobre mais de um domínio
 * (pessoas, paróquias, encontros), sempre numa consulta agrupada só em vez de uma contagem por
 * território (N+1, `skills/05-persistencia.md`).
 */
public interface ContagemPorTerritorio {

    UUID getTerritorioId();

    Long getTotal();
}
