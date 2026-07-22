package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Vincula duas Pessoas como Casal (RF-012, RN-PESSOA-03/RN-03) e registra o evento
 * {@code VINCULO_CASAL_REGISTRADO} no histórico de ambas.
 *
 * <p>Regra inferida (não detalhada no protótipo): uma pessoa não pode estar em mais de um
 * casal simultâneo — validado via {@code CasalRepository#existsByPessoa1_IdOrPessoa2_Id}.
 */
@Service
public class VincularCasalUseCase {

    private final PessoaRepository pessoaRepository;
    private final CasalRepository casalRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public VincularCasalUseCase(
            PessoaRepository pessoaRepository,
            CasalRepository casalRepository,
            HistoricoEventoRepository historicoEventoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.casalRepository = casalRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    @Transactional
    public Casal execute(UUID pessoa1Id, UUID pessoa2Id, LocalDate dataVinculo) {
        if (pessoa1Id.equals(pessoa2Id)) {
            throw new BusinessRuleViolationException("Uma pessoa não pode formar casal consigo mesma.");
        }

        Pessoa pessoa1 = pessoaRepository
                .findByIdAndDeletedFalse(pessoa1Id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoa1Id));
        Pessoa pessoa2 = pessoaRepository
                .findByIdAndDeletedFalse(pessoa2Id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoa2Id));

        if (casalRepository.existsByPessoa1_IdOrPessoa2_Id(pessoa1Id, pessoa1Id)
                || casalRepository.existsByPessoa1_IdOrPessoa2_Id(pessoa2Id, pessoa2Id)) {
            throw new BusinessRuleViolationException("Uma das pessoas já possui um casal vinculado.");
        }

        Casal casal = casalRepository.save(new Casal(pessoa1, pessoa2, dataVinculo));

        Instant agora = Instant.now();
        historicoEventoRepository.save(new HistoricoEvento(
                pessoa1,
                TipoEventoHistorico.VINCULO_CASAL_REGISTRADO,
                agora,
                "Vínculo de casal registrado com " + pessoa2.getNomeCompleto() + "."));
        historicoEventoRepository.save(new HistoricoEvento(
                pessoa2,
                TipoEventoHistorico.VINCULO_CASAL_REGISTRADO,
                agora,
                "Vínculo de casal registrado com " + pessoa1.getNomeCompleto() + "."));

        return casal;
    }
}
