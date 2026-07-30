package com.silberlima.segueme.dashboards.application.dto;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import java.util.List;

/**
 * Painel de Montagem (`dash_montagem`, RF-053). As sugestões inteligentes são exclusivas deste
 * perfil (RN-MONTAGEM-02) e vêm de {@code GerarSugestoesUseCase} para a equipe de maior prioridade —
 * a com mais vagas abertas, critério adotado porque o protótipo diz "equipe com maior prioridade"
 * sem definir a ordem. {@code equipePrioritaria} é nula quando nenhuma equipe tem vaga aberta.
 */
public record DashboardMontagemResult(
        long equipesIncompletas,
        long vagasAbertas,
        long sugestoesDisponiveis,
        long recusas,
        Encontro encontroCorrente,
        List<EquipeComposicao> equipes,
        EquipeComposicao equipePrioritaria,
        List<CandidatoSugerido> sugestoes) {
}
