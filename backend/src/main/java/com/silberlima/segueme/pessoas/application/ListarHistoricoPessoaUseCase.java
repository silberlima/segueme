package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Lista a linha do tempo de eventos de uma Pessoa, em ordem cronológica (RF-011, RN-PESSOA-02). */
@Service
public class ListarHistoricoPessoaUseCase {

    private final PessoaRepository pessoaRepository;
    private final HistoricoEventoRepository historicoEventoRepository;

    public ListarHistoricoPessoaUseCase(
            PessoaRepository pessoaRepository, HistoricoEventoRepository historicoEventoRepository) {
        this.pessoaRepository = pessoaRepository;
        this.historicoEventoRepository = historicoEventoRepository;
    }

    public List<HistoricoEvento> execute(UUID pessoaId) {
        if (!pessoaRepository.existsByIdAndDeletedFalse(pessoaId)) {
            throw new ResourceNotFoundException("Pessoa não encontrada: " + pessoaId);
        }
        return historicoEventoRepository.findByPessoa_IdOrderByDataEventoAsc(pessoaId);
    }
}
