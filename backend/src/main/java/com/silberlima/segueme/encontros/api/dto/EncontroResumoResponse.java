package com.silberlima.segueme.encontros.api.dto;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Linha da Lista de Encontros (RF-014). {@code numSeguidores}/{@code numEquipes}/
 * {@code numEquipistas} são placeholders até as Fases 5/6 (Inscrições/Montagem) existirem —
 * mesmo padrão de {@code numEncontros} em {@code PessoaResumoResponse}. {@code faseAtual} também
 * é um rótulo simplificado por ora: o valor granular do protótipo ("Inscrições abertas",
 * "Montagem de equipes"...) depende dessas mesmas fases futuras.
 */
public record EncontroResumoResponse(
        UUID id,
        int numero,
        String padroeiro,
        String status,
        LocalDate dataInicio,
        LocalDate dataTermino,
        String local,
        UUID paroquiaId,
        String paroquiaNome,
        String coordenadorGeralNome,
        String faseAtual,
        int numSeguidores,
        int numEquipes,
        int numEquipistas) {
}
