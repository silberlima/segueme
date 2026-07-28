package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Emite um convite de coordenador (RF-034) ou de integrante (RF-035) para uma equipe de serviço do
 * Encontro, restrito à Equipe Comando (RN-CONVITE-04). O convite é o próprio
 * {@link IntegranteEquipe}, criado {@link StatusIntegrante#PENDENTE} com a data de envio marcada.
 * Convite de integrante exige que a equipe já tenha um coordenador com convite aceito
 * (RN-CONVITE-01 — ordem obrigatória do fluxo).
 */
@Service
public class EmitirConviteUseCase {

    private final EncontroRepository encontroRepository;
    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final PessoaRepository pessoaRepository;

    public EmitirConviteUseCase(
            EncontroRepository encontroRepository,
            EquipeRepository equipeRepository,
            IntegranteEquipeRepository integranteEquipeRepository,
            PessoaRepository pessoaRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public IntegranteEquipe execute(UUID encontroId, UUID equipeId, UUID pessoaId, PapelIntegrante papel) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        Equipe equipe = equipeRepository
                .findByIdAndDeletedFalse(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + equipeId));
        Pessoa pessoa = pessoaRepository
                .findByIdAndDeletedFalse(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoaId));

        if (!Objects.equals(pessoa.getParoquia().getId(), encontro.getParoquia().getId())) {
            throw new BusinessRuleViolationException(
                    "Candidato de outra paróquia requer empréstimo aprovado pelo Setorial (RN-MOVIMENTO-04, Fase 8).");
        }

        if (papel == PapelIntegrante.INTEGRANTE
                && !integranteEquipeRepository.existsByEncontro_IdAndEquipe_IdAndPapelAndStatus(
                        encontroId, equipeId, PapelIntegrante.COORDENADOR, StatusIntegrante.ACEITO)) {
            throw new BusinessRuleViolationException(
                    "Só é possível convidar integrantes depois que o coordenador da equipe "
                            + equipe.getNome() + " aceitar seu convite (RN-CONVITE-01).");
        }

        integranteEquipeRepository
                .findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO)
                .ifPresent(existente -> {
                    throw new BusinessRuleViolationException("Esta pessoa já tem um convite ativo para esta equipe.");
                });

        long preenchidos = integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                encontroId, equipeId, StatusIntegrante.RECUSADO);
        if (preenchidos >= equipe.getMaximo()) {
            throw new BusinessRuleViolationException(
                    "A equipe " + equipe.getNome() + " já atingiu o máximo de integrantes (RN-06).");
        }

        return integranteEquipeRepository.save(new IntegranteEquipe(encontro, equipe, pessoa, papel));
    }
}
