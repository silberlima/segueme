package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A Pessoa que o usuário autenticado é. Base do perfil Seguidor, que não enxerga o cadastro dos
 * outros — só o próprio.
 *
 * <p>Usuário sem Pessoa vinculada recebe 404, não uma resposta vazia: quem acessa o sistema sem
 * estar no cadastro é um caso de administração pendente (pendência #3), e devolver "nada" faria a
 * tela parecer um perfil em branco em vez de um vínculo faltando.
 */
@Service
public class ConsultarMeusDadosUseCase {

    private final PessoaRepository pessoaRepository;

    public ConsultarMeusDadosUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public Pessoa execute(String usuarioId) {
        return pessoaRepository
                .findByUsuarioIdAndDeletedFalse(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma pessoa do cadastro está vinculada a este usuário."));
    }
}
