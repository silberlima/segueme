package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoSpecification;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.movimentacao.domain.TransferenciaSpecification;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Inbox único do Setorial (RF-046, `transferencias-e-emprestimos.md` 2.3): Transferências
 * {@code PENDENTE} e Empréstimos {@code SOLICITADO} do setor, combinados e ordenados por data —
 * mesma técnica de fusão em memória de {@code GerarSugestoesUseCase} (Fase 6), já que Solicitação
 * não é uma tabela própria.
 */
@Service
public class ListarSolicitacoesUseCase {

    private final TransferenciaRepository transferenciaRepository;
    private final EmprestimoRepository emprestimoRepository;

    public ListarSolicitacoesUseCase(
            TransferenciaRepository transferenciaRepository, EmprestimoRepository emprestimoRepository) {
        this.transferenciaRepository = transferenciaRepository;
        this.emprestimoRepository = emprestimoRepository;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoItem> execute(EscopoTerritorial escopo) {
        Specification<Transferencia> specTransferencia = Specification.allOf(
                TransferenciaSpecification.naoDeletada(),
                TransferenciaSpecification.comEscopo(escopo),
                TransferenciaSpecification.comStatus(StatusTransferencia.PENDENTE),
                TransferenciaSpecification.comAssociacoesCarregadas());
        Specification<Emprestimo> specEmprestimo = Specification.allOf(
                EmprestimoSpecification.naoDeletado(),
                EmprestimoSpecification.comEscopo(escopo),
                EmprestimoSpecification.comStatus(StatusEmprestimo.SOLICITADO),
                EmprestimoSpecification.comAssociacoesCarregadas());

        Stream<SolicitacaoItem> transferencias =
                transferenciaRepository.findAll(specTransferencia).stream().map(SolicitacaoItem::de);
        Stream<SolicitacaoItem> emprestimos = emprestimoRepository.findAll(specEmprestimo).stream().map(SolicitacaoItem::de);

        return Stream.concat(transferencias, emprestimos)
                .sorted(Comparator.comparing(SolicitacaoItem::createdAt).reversed())
                .toList();
    }
}
