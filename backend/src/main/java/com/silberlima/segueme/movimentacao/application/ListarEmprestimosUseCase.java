package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoSpecification;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista Empréstimos do escopo territorial do usuário (setor do Setorial ou paróquia da Montagem),
 * com filtro opcional por encontro/equipe/status (RF-042 a RF-046).
 */
@Service
public class ListarEmprestimosUseCase {

    private final EmprestimoRepository emprestimoRepository;

    public ListarEmprestimosUseCase(EmprestimoRepository emprestimoRepository) {
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<Emprestimo> execute(EscopoTerritorial escopo, UUID encontroId, UUID equipeId, StatusEmprestimo status) {
        Specification<Emprestimo> spec = Specification.allOf(
                EmprestimoSpecification.naoDeletado(),
                EmprestimoSpecification.comEscopo(escopo),
                EmprestimoSpecification.comEncontro(encontroId),
                EmprestimoSpecification.comEquipe(equipeId),
                EmprestimoSpecification.comStatus(status),
                EmprestimoSpecification.comAssociacoesCarregadas());
        return emprestimoRepository.findAll(spec);
    }
}
