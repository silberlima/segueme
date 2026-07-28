package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Solicita a Transferência definitiva de uma Pessoa para outra Paróquia (RF-040). Fica
 * {@code PENDENTE} até decisão do Setorial (RN-MOVIMENTO-01).
 *
 * <p>Quem dispara o gatilho (a própria pessoa, a paróquia de origem ou a de destino) não é
 * modelado no protótipo — pendência #9 de {@code pendencias-gerais.md}. Adotado aqui: qualquer
 * perfil com acesso ao cadastro de Pessoas/Paróquia pode solicitar, informando a paróquia de
 * destino.
 */
@Service
public class SolicitarTransferenciaUseCase {

    private final PessoaRepository pessoaRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final TransferenciaRepository transferenciaRepository;

    public SolicitarTransferenciaUseCase(
            PessoaRepository pessoaRepository,
            ParoquiaRepository paroquiaRepository,
            TransferenciaRepository transferenciaRepository) {
        this.pessoaRepository = pessoaRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional
    public Transferencia execute(UUID pessoaId, UUID paroquiaDestinoId, String motivo) {
        Pessoa pessoa = pessoaRepository
                .findByIdAndDeletedFalse(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoaId));
        Paroquia paroquiaDestino = paroquiaRepository
                .findByIdAndDeletedFalse(paroquiaDestinoId)
                .orElseThrow(() -> new ResourceNotFoundException("Paróquia não encontrada: " + paroquiaDestinoId));

        if (Objects.equals(pessoa.getParoquia().getId(), paroquiaDestino.getId())) {
            throw new BusinessRuleViolationException("A pessoa já pertence a esta paróquia.");
        }

        return transferenciaRepository.save(new Transferencia(pessoa, pessoa.getParoquia(), paroquiaDestino, motivo));
    }
}
