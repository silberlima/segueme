package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.inscricoes.application.dto.FunilInscricao;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import com.silberlima.segueme.inscricoes.domain.StatusReserva;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consolida o funil de inscrição de um Encontro por etapa (RF-027). */
@Service
public class ConsultarFunilUseCase {

    private final EncontroRepository encontroRepository;
    private final PreInscricaoRepository preInscricaoRepository;
    private final FichaInscricaoRepository fichaInscricaoRepository;
    private final CadastroReservaRepository cadastroReservaRepository;

    public ConsultarFunilUseCase(
            EncontroRepository encontroRepository,
            PreInscricaoRepository preInscricaoRepository,
            FichaInscricaoRepository fichaInscricaoRepository,
            CadastroReservaRepository cadastroReservaRepository) {
        this.encontroRepository = encontroRepository;
        this.preInscricaoRepository = preInscricaoRepository;
        this.fichaInscricaoRepository = fichaInscricaoRepository;
        this.cadastroReservaRepository = cadastroReservaRepository;
    }

    @Transactional(readOnly = true)
    public FunilInscricao execute(UUID encontroId) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));

        long preInscritos =
                preInscricaoRepository.countByEncontro_IdAndStatus(encontroId, StatusPreInscricao.AGUARDANDO_FICHA);
        long emAnalise =
                preInscricaoRepository.countByEncontro_IdAndStatus(encontroId, StatusPreInscricao.EM_ANALISE);
        long confirmados = fichaInscricaoRepository.countByEncontro_Id(encontroId);
        long emReserva = cadastroReservaRepository.countByEncontro_Paroquia_IdAndStatus(
                encontro.getParoquia().getId(), StatusReserva.AGUARDANDO);

        // "seguidores": placeholder até a Fase 6/7 registrar a 1ª participação em equipe (RN-PESSOA-05).
        return new FunilInscricao(preInscritos, emAnalise, confirmados, emReserva, 0);
    }
}
