package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Preenche/confirma a Ficha de Inscrição de uma Pré-inscrição (RF-023/024, restrito à Equipe
 * Fichas via {@code @PreAuthorize} no controller — RN-INSCRICAO-01). Os dados das 4 seções da
 * Ficha já vivem na {@code Pessoa} (cadastro nacional, Fase 3); esta ação apenas confirma a
 * inscrição, cria o registro de auditoria da Ficha e promove a pessoa a Seguimista
 * (RN-INSCRICAO-02), registrando o evento no histórico (RN-PESSOA-02).
 */
@Service
public class PreencherFichaUseCase {

    private final PreInscricaoRepository preInscricaoRepository;
    private final FichaInscricaoRepository fichaInscricaoRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public PreencherFichaUseCase(
            PreInscricaoRepository preInscricaoRepository,
            FichaInscricaoRepository fichaInscricaoRepository,
            HistoricoEventoRepository historicoEventoRepository) {
        this.preInscricaoRepository = preInscricaoRepository;
        this.fichaInscricaoRepository = fichaInscricaoRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional
    public FichaInscricao execute(UUID preInscricaoId) {
        PreInscricao preInscricao = preInscricaoRepository
                .findByIdAndDeletedFalse(preInscricaoId)
                .orElseThrow(() -> new ResourceNotFoundException("Pré-inscrição não encontrada: " + preInscricaoId));

        if (fichaInscricaoRepository.existsByPreInscricao_Id(preInscricaoId)) {
            throw new BusinessRuleViolationException("Esta pré-inscrição já possui ficha confirmada.");
        }

        preInscricao.confirmar();
        preInscricao.getPessoa().confirmarComoSeguimista();

        FichaInscricao ficha = fichaInscricaoRepository.save(
                new FichaInscricao(preInscricao, preInscricao.getPessoa(), preInscricao.getEncontro()));

        historicoEventoRepository.save(new HistoricoEvento(
                preInscricao.getPessoa(),
                TipoEventoHistorico.TORNOU_SE_SEGUIMISTA,
                Instant.now(),
                "Ficha de inscrição confirmada pela Equipe Fichas para o encontro #"
                        + preInscricao.getEncontro().getNumero() + "."));

        return ficha;
    }
}
