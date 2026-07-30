package com.silberlima.segueme.dashboards.domain;

import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Totais de uma consulta agrupada por território, indexados por id. Existe para que os painéis
 * cruzem "lista de territórios × contagem" sem repetir a indexação em cada um deles, e para que
 * território sem nenhuma ocorrência valha 0 em vez de ausente.
 */
public record TotaisPorTerritorio(Map<UUID, Long> porTerritorio) {

    public static TotaisPorTerritorio de(List<ContagemPorTerritorio> contagens) {
        return new TotaisPorTerritorio(contagens.stream()
                .collect(Collectors.toMap(
                        ContagemPorTerritorio::getTerritorioId, ContagemPorTerritorio::getTotal, Long::sum)));
    }

    public long total(UUID territorioId) {
        return porTerritorio.getOrDefault(territorioId, 0L);
    }
}
