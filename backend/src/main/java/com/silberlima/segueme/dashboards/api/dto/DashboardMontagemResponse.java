package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.montagem.api.dto.CandidatoSugeridoResponse;
import com.silberlima.segueme.montagem.api.dto.EquipeResumoResponse;
import java.util.List;

/**
 * Painel de Montagem (`dash_montagem`, RF-053). As sugestões são exclusivas deste perfil
 * (RN-MONTAGEM-02); {@code equipePrioritaria} nula = nenhuma equipe com vaga aberta.
 */
public record DashboardMontagemResponse(
        long equipesIncompletas,
        long vagasAbertas,
        long sugestoesDisponiveis,
        long recusas,
        EncontroDoPainelResponse encontroCorrente,
        List<EquipeResumoResponse> equipes,
        EquipeResumoResponse equipePrioritaria,
        List<CandidatoSugeridoResponse> sugestoes) {
}
