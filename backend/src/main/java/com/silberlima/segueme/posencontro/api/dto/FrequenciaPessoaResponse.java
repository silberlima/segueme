package com.silberlima.segueme.posencontro.api.dto;

import java.util.UUID;

/**
 * Frequência apurada de um acompanhado e sua faixa de prioridade (RF-059, RN-POS-03). Serve tanto a
 * tela Frequência quanto o ranking da tela Prioridades — a diferença entre elas é o recorte de
 * período e a ordenação, não o conteúdo da linha.
 */
public record FrequenciaPessoaResponse(
        UUID pessoaId,
        String pessoaNome,
        long totalEncontros,
        long presencas,
        int percentual,
        String faixaPrioridade) {
}
