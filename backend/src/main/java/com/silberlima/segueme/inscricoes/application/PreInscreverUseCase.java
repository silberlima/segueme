package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.inscricoes.application.dto.DadosNovaPreInscricao;
import com.silberlima.segueme.inscricoes.application.dto.ResultadoPreInscricao;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pré-inscreve uma Pessoa já cadastrada num Encontro (RF-022). Se o limite de vagas do Encontro
 * já foi atingido pelas pré-inscrições ativas, a pessoa entra automaticamente no Cadastro de
 * Reserva em vez de uma Pré-inscrição (RN-INSCRICAO-03).
 */
@Service
public class PreInscreverUseCase {

    private final PreInscricaoRepository preInscricaoRepository;
    private final CadastroReservaRepository cadastroReservaRepository;
    private final EncontroRepository encontroRepository;
    private final PessoaRepository pessoaRepository;

    public PreInscreverUseCase(
            PreInscricaoRepository preInscricaoRepository,
            CadastroReservaRepository cadastroReservaRepository,
            EncontroRepository encontroRepository,
            PessoaRepository pessoaRepository) {
        this.preInscricaoRepository = preInscricaoRepository;
        this.cadastroReservaRepository = cadastroReservaRepository;
        this.encontroRepository = encontroRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public ResultadoPreInscricao execute(DadosNovaPreInscricao dados) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(dados.encontroId())
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + dados.encontroId()));
        Pessoa pessoa = pessoaRepository
                .findByIdAndDeletedFalse(dados.pessoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + dados.pessoaId()));

        if (preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(dados.pessoaId(), dados.encontroId())) {
            throw new BusinessRuleViolationException(
                    "Esta pessoa já possui uma pré-inscrição para este encontro.");
        }

        long ativos = preInscricaoRepository.countByEncontro_Id(dados.encontroId());
        if (ativos >= encontro.getVagas()) {
            CadastroReserva reserva = cadastroReservaRepository.save(new CadastroReserva(pessoa, encontro));
            return new ResultadoPreInscricao(null, reserva);
        }

        PreInscricao preInscricao = preInscricaoRepository.save(new PreInscricao(pessoa, encontro));
        return new ResultadoPreInscricao(preInscricao, null);
    }
}
