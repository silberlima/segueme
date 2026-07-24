package com.silberlima.segueme.encontros.api.dto;

import com.silberlima.segueme.encontros.domain.PastaDirigente;
import com.silberlima.segueme.encontros.domain.TipoMembroDirigente;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Payload único do wizard "Criar Encontro" (RF-015 a RF-020), enviado após a revisão do passo 5.
 * {@code equipeDirigente} pode ter menos de 15 entradas — pasta incompleta é permitida (RN-21).
 */
public record CriarEncontroRequest(
        @NotBlank String padroeiro,
        @NotNull UUID diretorEspiritualId,
        @NotBlank String local,
        @NotNull LocalDate dataInicio,
        @NotNull LocalDate dataTermino,
        @NotNull @Positive Integer vagas,
        @NotNull UUID paroquiaId,
        @NotNull @Valid List<MembroDirigenteInput> equipeDirigente,
        @NotNull @Valid EquipeComandoInput equipeComando) {

    public CriarEncontroRequest {
        equipeDirigente = equipeDirigente == null ? List.of() : List.copyOf(equipeDirigente);
    }

    public record MembroDirigenteInput(
            @NotNull PastaDirigente pasta, @NotNull TipoMembroDirigente tipoMembro, UUID pessoaId, UUID casalId) {
    }

    public record EquipeComandoInput(@NotNull UUID coordenadorGeralId, @NotNull UUID casalCoordenadorId) {
    }
}
