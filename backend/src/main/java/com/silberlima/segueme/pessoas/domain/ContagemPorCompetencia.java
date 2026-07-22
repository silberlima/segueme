package com.silberlima.segueme.pessoas.domain;

import java.util.UUID;

/** Projeção Spring Data para a contagem de pessoas por competência (RF-013), evitando N+1. */
public interface ContagemPorCompetencia {

    UUID getCompetenciaId();

    Long getTotal();
}
