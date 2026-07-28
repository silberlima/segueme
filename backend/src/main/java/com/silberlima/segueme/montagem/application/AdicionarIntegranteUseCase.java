package com.silberlima.segueme.montagem.application;

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
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Adiciona um candidato (sugerido ou não) a uma equipe de serviço do Encontro (RF-031), ação
 * restrita à Equipe Montagem via {@code @PreAuthorize} no controller (RN-07/RN-MONTAGEM-01).
 * Bloqueia diretamente candidatos de outra paróquia: RN-MOVIMENTO-04 exige que esse vínculo passe
 * pelo Setorial (Fase 8, ainda não implementada) — aqui só sinalizamos a restrição.
 */
@Service
public class AdicionarIntegranteUseCase {

    private final EncontroRepository encontroRepository;
    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final PessoaRepository pessoaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public AdicionarIntegranteUseCase(
            EncontroRepository encontroRepository,
            EquipeRepository equipeRepository,
            IntegranteEquipeRepository integranteEquipeRepository,
            PessoaRepository pessoaRepository,
            HistoricoEventoRepository historicoEventoRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.pessoaRepository = pessoaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
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

        integranteEquipeRepository
                .findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO)
                .ifPresent(existente -> {
                    throw new BusinessRuleViolationException("Esta pessoa já integra esta equipe.");
                });

        long preenchidos = integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                encontroId, equipeId, StatusIntegrante.RECUSADO);
        if (preenchidos >= equipe.getMaximo()) {
            throw new BusinessRuleViolationException(
                    "A equipe " + equipe.getNome() + " já atingiu o máximo de integrantes (RN-06).");
        }

        IntegranteEquipe integrante = integranteEquipeRepository.save(new IntegranteEquipe(encontro, equipe, pessoa, papel));

        historicoEventoRepository.save(new HistoricoEvento(
                pessoa,
                TipoEventoHistorico.PARTICIPACAO_EQUIPE,
                Instant.now(),
                "Adicionado(a) à equipe " + equipe.getNome() + " do encontro #" + encontro.getNumero()
                        + " pela Equipe Montagem."));
        if (papel == PapelIntegrante.COORDENADOR) {
            historicoEventoRepository.save(new HistoricoEvento(
                    pessoa,
                    TipoEventoHistorico.PAPEL_COORDENADOR,
                    Instant.now(),
                    "Designado(a) coordenador(a) da equipe " + equipe.getNome() + " do encontro #"
                            + encontro.getNumero() + "."));
        }

        return integrante;
    }
}
