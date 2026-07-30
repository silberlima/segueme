package com.silberlima.segueme.dashboards.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * O Encontro como os painéis o exibem: cartão do Encontro corrente e tabela de Encontros recentes
 * (`dashboards-por-perfil.md` 3.4). É um recorte próprio, e não o {@code EncontroResumoResponse} da
 * lista de Encontros, porque o cartão precisa de vagas e diretor espiritual e não de coordenador
 * geral nem de contadores de equipe.
 */
public record EncontroDoPainelResponse(
        UUID id,
        int numero,
        String padroeiro,
        String status,
        LocalDate dataInicio,
        LocalDate dataTermino,
        String local,
        int vagas,
        UUID paroquiaId,
        String paroquiaNome,
        String diretorEspiritualNome) {
}
