package com.silberlima.segueme.encontros.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EquipeComando;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigente;
import java.util.List;

/** Agregado de leitura de {@code ConsultarEncontroUseCase} (RF-021): Encontro + suas duas equipes. */
public record EncontroDetalhado(Encontro encontro, List<MembroEquipeDirigente> membrosDirigente, EquipeComando equipeComando) {
}
