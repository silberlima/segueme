package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista os Participantes (fichas confirmadas) de um Encontro, com status de pagamento (RF-027). */
@Service
public class ListarParticipantesUseCase {

    private final FichaInscricaoRepository fichaInscricaoRepository;

    public ListarParticipantesUseCase(FichaInscricaoRepository fichaInscricaoRepository) {
        this.fichaInscricaoRepository = fichaInscricaoRepository;
    }

    @Transactional(readOnly = true)
    public Page<FichaInscricao> execute(UUID encontroId, Pageable pageable) {
        return fichaInscricaoRepository.findByEncontro_Id(encontroId, pageable);
    }
}
