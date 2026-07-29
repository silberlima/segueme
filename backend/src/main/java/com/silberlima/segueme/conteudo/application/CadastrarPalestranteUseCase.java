package com.silberlima.segueme.conteudo.application;

import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Cadastra um palestrante nos 3 tipos previstos (RF-052, RN-CONTEUDO-02). Convidado externo e
 * clero são identificados só pelo nome informado — {@code nome} é ignorado para o tipo SEGUIDOR,
 * cujo nome vem do cadastro de {@link Pessoa}.
 */
@Service
public class CadastrarPalestranteUseCase {

    private final PessoaRepository pessoaRepository;
    private final PalestranteRepository palestranteRepository;

    public CadastrarPalestranteUseCase(PessoaRepository pessoaRepository, PalestranteRepository palestranteRepository) {
        this.pessoaRepository = pessoaRepository;
        this.palestranteRepository = palestranteRepository;
    }

    @Transactional
    public Palestrante execute(TipoPalestrante tipo, String nome, UUID pessoaId) {
        Palestrante palestrante = switch (tipo) {
            case SEGUIDOR -> Palestrante.seguidor(buscarPessoa(pessoaId));
            case EXTERNO -> Palestrante.externo(nome);
            case CLERO -> Palestrante.clero(nome);
        };
        return palestranteRepository.save(palestrante);
    }

    private Pessoa buscarPessoa(UUID pessoaId) {
        if (pessoaId == null) {
            throw new BusinessRuleViolationException(
                    "Um palestrante do tipo SEGUIDOR exige o id de uma Pessoa cadastrada.");
        }
        return pessoaRepository
                .findByIdAndDeletedFalse(pessoaId)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoaId));
    }
}
