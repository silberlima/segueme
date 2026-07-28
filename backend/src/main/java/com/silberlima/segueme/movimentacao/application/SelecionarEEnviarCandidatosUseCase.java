package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Setorial seleciona candidatos (de outras paróquias do setor) e os envia à Montagem (RF-043/044,
 * RN-MOVIMENTO-05). Correspondente ao {@code SelectAndSendCandidatesUseCase} citado em
 * {@code plano-implementacao.md}. Idempotente: reenviar um pessoaId já candidato não duplica.
 */
@Service
public class SelecionarEEnviarCandidatosUseCase {

    private final EmprestimoRepository emprestimoRepository;
    private final PessoaRepository pessoaRepository;
    private final EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    public SelecionarEEnviarCandidatosUseCase(
            EmprestimoRepository emprestimoRepository,
            PessoaRepository pessoaRepository,
            EmprestimoCandidatoRepository emprestimoCandidatoRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.pessoaRepository = pessoaRepository;
        this.emprestimoCandidatoRepository = emprestimoCandidatoRepository;
    }

    @Transactional
    public Emprestimo execute(UUID emprestimoId, List<UUID> pessoaIds) {
        Emprestimo emprestimo = emprestimoRepository
                .findByIdAndDeletedFalse(emprestimoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado: " + emprestimoId));

        for (UUID pessoaId : pessoaIds) {
            if (emprestimoCandidatoRepository.existsByEmprestimo_IdAndPessoa_Id(emprestimoId, pessoaId)) {
                continue;
            }
            Pessoa pessoa = pessoaRepository
                    .findByIdAndDeletedFalse(pessoaId)
                    .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + pessoaId));
            emprestimoCandidatoRepository.save(new EmprestimoCandidato(emprestimo, pessoa));
        }

        emprestimo.enviarSugestoes();
        return emprestimo;
    }
}
