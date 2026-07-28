package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.montagem.application.dto.DadosSubstituicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.montagem.domain.Substituicao;
import com.silberlima.segueme.montagem.domain.SubstituicaoRepository;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra a substituição de um integrante por outro numa equipe de serviço (RF-032), restrita à
 * Equipe Montagem (RN-MONTAGEM-01). A pessoa que sai é marcada {@link StatusIntegrante#RECUSADO}
 * — nunca removida — e a que entra nasce {@link StatusIntegrante#PENDENTE}, preservando o
 * histórico de ambas (RN-MONTAGEM-06/RN-02/13). Sendo uma troca 1-para-1, não há necessidade de
 * revalidar o máximo de vagas da equipe.
 */
@Service
public class RegistrarSubstituicaoUseCase {

    private final EncontroRepository encontroRepository;
    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final SubstituicaoRepository substituicaoRepository;
    private final PessoaRepository pessoaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public RegistrarSubstituicaoUseCase(
            EncontroRepository encontroRepository,
            EquipeRepository equipeRepository,
            IntegranteEquipeRepository integranteEquipeRepository,
            SubstituicaoRepository substituicaoRepository,
            PessoaRepository pessoaRepository,
            HistoricoEventoRepository historicoEventoRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.substituicaoRepository = substituicaoRepository;
        this.pessoaRepository = pessoaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional
    public Substituicao execute(DadosSubstituicao dados) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(dados.encontroId())
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + dados.encontroId()));
        Equipe equipe = equipeRepository
                .findByIdAndDeletedFalse(dados.equipeId())
                .orElseThrow(() -> new ResourceNotFoundException("Equipe não encontrada: " + dados.equipeId()));

        IntegranteEquipe integranteSai = integranteEquipeRepository
                .findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        dados.encontroId(), dados.equipeId(), dados.pessoaSaiId(), StatusIntegrante.RECUSADO)
                .orElseThrow(() -> new ResourceNotFoundException("Esta pessoa não integra esta equipe neste encontro."));

        Pessoa pessoaEntra = pessoaRepository
                .findByIdAndDeletedFalse(dados.pessoaEntraId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + dados.pessoaEntraId()));
        if (!Objects.equals(pessoaEntra.getParoquia().getId(), encontro.getParoquia().getId())) {
            throw new BusinessRuleViolationException(
                    "Candidato de outra paróquia requer empréstimo aprovado pelo Setorial (RN-MOVIMENTO-04, Fase 8).");
        }
        if (integranteEquipeRepository
                .findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        dados.encontroId(), dados.equipeId(), dados.pessoaEntraId(), StatusIntegrante.RECUSADO)
                .isPresent()) {
            throw new BusinessRuleViolationException("A pessoa que está entrando já integra esta equipe.");
        }

        integranteSai.marcarRecusado();

        integranteEquipeRepository.save(new IntegranteEquipe(encontro, equipe, pessoaEntra, integranteSai.getPapel()));

        Instant agora = Instant.now();
        Substituicao substituicao = substituicaoRepository.save(new Substituicao(
                encontro,
                equipe,
                integranteSai.getPessoa(),
                dados.motivoSaida(),
                pessoaEntra,
                dados.justificativa(),
                agora));

        historicoEventoRepository.save(new HistoricoEvento(
                integranteSai.getPessoa(),
                TipoEventoHistorico.PARTICIPACAO_EQUIPE,
                agora,
                "Saiu da equipe " + equipe.getNome() + " do encontro #" + encontro.getNumero() + " ("
                        + dados.motivoSaida() + ")."));
        historicoEventoRepository.save(new HistoricoEvento(
                pessoaEntra,
                TipoEventoHistorico.PARTICIPACAO_EQUIPE,
                agora,
                "Entrou na equipe " + equipe.getNome() + " do encontro #" + encontro.getNumero()
                        + " via substituição."));

        return substituicao;
    }
}
