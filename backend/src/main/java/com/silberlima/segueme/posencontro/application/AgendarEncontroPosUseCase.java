package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Agenda um encontro pós da paróquia numa data (RN-POS-01). A mesma paróquia não tem dois encontros
 * pós na mesma data — a data é o que identifica o encontro pós no registro de frequência.
 */
@Service
public class AgendarEncontroPosUseCase {

    private final ParoquiaRepository paroquiaRepository;
    private final EncontroPosRepository encontroPosRepository;

    public AgendarEncontroPosUseCase(
            ParoquiaRepository paroquiaRepository, EncontroPosRepository encontroPosRepository) {
        this.paroquiaRepository = paroquiaRepository;
        this.encontroPosRepository = encontroPosRepository;
    }

    @Transactional
    public EncontroPos execute(UUID paroquiaId, LocalDate data, String tema) {
        Paroquia paroquia = paroquiaRepository
                .findByIdAndDeletedFalse(paroquiaId)
                .orElseThrow(() -> new ResourceNotFoundException("Paróquia não encontrada: " + paroquiaId));

        if (encontroPosRepository.existsByParoquia_IdAndDataAndDeletedFalse(paroquiaId, data)) {
            throw new BusinessRuleViolationException(
                    "Já existe um encontro pós agendado nesta paróquia para " + data + ".");
        }

        return encontroPosRepository.save(new EncontroPos(paroquia, data, tema));
    }
}
