package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.UUID;
import org.springframework.stereotype.Service;

/** Consulta o Perfil Completo de uma Pessoa (RF-010). */
@Service
public class ConsultarPessoaUseCase {

    private final PessoaRepository pessoaRepository;

    public ConsultarPessoaUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    public Pessoa execute(UUID id) {
        return pessoaRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + id));
    }
}
