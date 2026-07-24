package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Promove um candidato do Cadastro de Reserva a uma vaga confirmada num Encontro (RF-026, UC-07).
 * Ação manual da Equipe Fichas — não há detecção automática de "vaga aberta" (pendência não
 * especificada em {@code pendencias-gerais.md}; UC-07 descreve a promoção como decisão manual).
 */
@Service
public class PromoverReservaUseCase {

    private final CadastroReservaRepository cadastroReservaRepository;
    private final PreInscricaoRepository preInscricaoRepository;
    private final EncontroRepository encontroRepository;

    public PromoverReservaUseCase(
            CadastroReservaRepository cadastroReservaRepository,
            PreInscricaoRepository preInscricaoRepository,
            EncontroRepository encontroRepository) {
        this.cadastroReservaRepository = cadastroReservaRepository;
        this.preInscricaoRepository = preInscricaoRepository;
        this.encontroRepository = encontroRepository;
    }

    @Transactional
    public PreInscricao execute(UUID cadastroReservaId, UUID encontroDestinoId) {
        CadastroReserva reserva = cadastroReservaRepository
                .findByIdAndDeletedFalse(cadastroReservaId)
                .orElseThrow(
                        () -> new ResourceNotFoundException("Cadastro de reserva não encontrado: " + cadastroReservaId));
        Encontro encontroDestino = encontroRepository
                .findByIdAndDeletedFalse(encontroDestinoId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroDestinoId));

        if (preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(reserva.getPessoa().getId(), encontroDestinoId)) {
            throw new BusinessRuleViolationException(
                    "Esta pessoa já possui uma pré-inscrição para o encontro de destino.");
        }

        reserva.promover();
        return preInscricaoRepository.save(new PreInscricao(reserva.getPessoa(), encontroDestino));
    }
}
