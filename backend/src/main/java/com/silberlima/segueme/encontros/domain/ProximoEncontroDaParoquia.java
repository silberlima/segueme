package com.silberlima.segueme.encontros.domain;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Projeção Spring Data da data do próximo Encontro de uma paróquia. A lista "Paróquias do setor"
 * do Painel do Setor (`dashboards-por-perfil.md` 3.3) mostra o status do próximo Encontro de cada
 * paróquia: apurar isso numa consulta agrupada evita uma consulta por paróquia (N+1).
 */
public interface ProximoEncontroDaParoquia {

    UUID getTerritorioId();

    LocalDate getData();
}
