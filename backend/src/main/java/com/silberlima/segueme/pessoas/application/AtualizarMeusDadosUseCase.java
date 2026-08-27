package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.application.dto.DadosPessoaisEditaveis;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * A própria pessoa corrige os seus dados de contato e saúde (perfil Seguidor).
 *
 * <p>O alvo nunca vem da requisição: é resolvido pelo {@code sub} do token. Aceitar um id do
 * cliente transformaria este endpoint em "editar qualquer pessoa" para quem trocasse o parâmetro.
 */
@Service
public class AtualizarMeusDadosUseCase {

    private final PessoaRepository pessoaRepository;

    public AtualizarMeusDadosUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional
    public Pessoa execute(String usuarioId, DadosPessoaisEditaveis dados) {
        Pessoa pessoa = pessoaRepository
                .findByUsuarioIdAndDeletedFalse(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Nenhuma pessoa do cadastro está vinculada a este usuário."));

        pessoa.atualizarDadosPessoais(
                dados.telefone(),
                dados.alergias(),
                dados.restricaoAlimentar(),
                dados.medicacaoUsoContinuo(),
                dados.contatoEmergenciaNome(),
                dados.contatoEmergenciaTelefone(),
                dados.contatoEmergenciaParentesco());

        return pessoa;
    }
}
