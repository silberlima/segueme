package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Agenda de encontros pós de uma paróquia, do mais recente para o mais antigo (tela Presenças). */
@Service
public class ListarEncontrosPosUseCase {

    private final EncontroPosRepository encontroPosRepository;

    public ListarEncontrosPosUseCase(EncontroPosRepository encontroPosRepository) {
        this.encontroPosRepository = encontroPosRepository;
    }

    @Transactional(readOnly = true)
    public Page<EncontroPos> execute(UUID paroquiaId, Pageable pageable) {
        return encontroPosRepository.findByParoquia_IdAndDeletedFalseOrderByDataDesc(paroquiaId, pageable);
    }
}
