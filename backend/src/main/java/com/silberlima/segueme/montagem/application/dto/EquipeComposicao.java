package com.silberlima.segueme.montagem.application.dto;

import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import java.util.List;

/**
 * Agregado de leitura de {@code ListarEquipesUseCase}/{@code ConsultarEquipeUseCase} (RF-028):
 * o tipo de equipe do catálogo nacional e seus integrantes num Encontro específico, com as
 * métricas de vaga derivadas (RN-06).
 */
public record EquipeComposicao(Equipe equipe, List<IntegranteEquipe> integrantes) {

    /** Integrantes que efetivamente ocupam vaga — recusados preservam histórico mas liberam a vaga (RN-MONTAGEM-06). */
    public long preenchidos() {
        return integrantes.stream()
                .filter(integrante -> integrante.getStatus() != StatusIntegrante.RECUSADO)
                .count();
    }

    /** Vagas em aberto somem ao atingir o máximo — não ficam negativas (pendência de UX #3, `montagem-de-equipes.md`). */
    public long vagasAbertas() {
        return Math.max(0, equipe.getMaximo() - preenchidos());
    }

    /**
     * "Completa" = atingiu ao menos o mínimo de integrantes exigido pelo tipo. Se "Confirmar
     * montagem" deve ou não bloquear com equipes incompletas é a pendência #7 de
     * {@code pendencias-gerais.md}, deliberadamente não resolvida nesta fase.
     */
    public boolean completa() {
        return preenchidos() >= equipe.getMinimo();
    }
}
