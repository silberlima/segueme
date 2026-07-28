package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.domain.CandidatoScoreInput;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.MatchScore;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.montagem.domain.SuggestionEngine;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Gera a lista ranqueada de candidatos sugeridos para uma vaga de equipe (RF-029/030,
 * RN-08/RN-MONTAGEM-02: resultado exclusivo da Equipe Montagem — a restrição de visibilidade é
 * aplicada pelo {@code @PreAuthorize} do controller, não aqui). Nunca substitui a decisão humana
 * da Montagem (RN-10/RN-MONTAGEM-04) — apenas ordena por {@link SuggestionEngine}.
 */
@Service
public class GerarSugestoesUseCase {

    private static final int MAXIMO_CANDIDATOS = 20;

    private final EncontroRepository encontroRepository;
    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final PessoaRepository pessoaRepository;
    private final SuggestionEngine suggestionEngine = new SuggestionEngine();

    public GerarSugestoesUseCase(
            EncontroRepository encontroRepository,
            EquipeRepository equipeRepository,
            IntegranteEquipeRepository integranteEquipeRepository,
            PessoaRepository pessoaRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public List<CandidatoSugerido> execute(UUID encontroId, UUID equipeId) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        Equipe equipe = equipeRepository
                .findByIdAndDeletedFalse(equipeId)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + equipeId));

        Set<UUID> jaVinculados = integranteEquipeRepository.idsPessoaVinculadaNaEquipe(encontroId, equipeId);

        List<CandidatoSugerido> candidatos = new ArrayList<>();
        for (Pessoa candidato : pessoaRepository.findBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR)) {
            if (jaVinculados.contains(candidato.getId())) {
                continue;
            }
            candidatos.add(avaliar(candidato, equipe, encontro));
        }

        return candidatos.stream()
                .sorted(Comparator.comparingInt((CandidatoSugerido c) -> c.score().total()).reversed())
                .limit(MAXIMO_CANDIDATOS)
                .toList();
    }

    private CandidatoSugerido avaliar(Pessoa candidato, Equipe equipe, Encontro encontro) {
        int participacoesSemelhantes = (int) integranteEquipeRepository
                .countByPessoa_IdAndEquipe_IdAndEncontro_IdNotAndStatusNot(
                        candidato.getId(), equipe.getId(), encontro.getId(), StatusIntegrante.RECUSADO);

        Set<Competencia> requeridas = equipe.getCompetenciasRequeridas();
        Set<Competencia> atendidas = new HashSet<>(candidato.getCompetencias());
        atendidas.retainAll(requeridas);

        boolean cumprePreRequisito = equipe.getPreRequisitoMinEncontros() == null
                || integranteEquipeRepository.countByPessoa_IdAndEncontro_IdNotAndStatusNot(
                                candidato.getId(), encontro.getId(), StatusIntegrante.RECUSADO)
                        >= equipe.getPreRequisitoMinEncontros();

        MatchScore score = suggestionEngine.calcular(new CandidatoScoreInput(
                participacoesSemelhantes, requeridas.size(), atendidas.size(), false, 0, cumprePreRequisito));

        boolean mesmaParoquia = Objects.equals(candidato.getParoquia().getId(), encontro.getParoquia().getId());

        List<String> justificativas = new ArrayList<>();
        if (equipe.getPreRequisitoMinEncontros() != null && cumprePreRequisito) {
            justificativas.add("Cumpre pré-requisito");
        }
        if (participacoesSemelhantes > 0) {
            justificativas.add(participacoesSemelhantes + " encontro(s) nesta equipe");
        }
        if (!mesmaParoquia) {
            justificativas.add("Requer empréstimo do setor");
        }

        return new CandidatoSugerido(candidato, score, mesmaParoquia, justificativas);
    }
}
