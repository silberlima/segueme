package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.movimentacao.domain.StatusTransferencia;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.movimentacao.domain.TransferenciaSpecification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista Transferências do escopo territorial do usuário, com filtro opcional por status/pessoa (RF-041). */
@Service
public class ListarTransferenciasUseCase {

    private final TransferenciaRepository transferenciaRepository;

    public ListarTransferenciasUseCase(TransferenciaRepository transferenciaRepository) {
        this.transferenciaRepository = transferenciaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Transferencia> execute(
            EscopoTerritorial escopo, StatusTransferencia status, UUID pessoaId, Pageable pageable) {
        Specification<Transferencia> spec = Specification.allOf(
                TransferenciaSpecification.naoDeletada(),
                TransferenciaSpecification.comEscopo(escopo),
                TransferenciaSpecification.comStatus(status),
                TransferenciaSpecification.comPessoa(pessoaId));
        return transferenciaRepository.findAll(spec, pageable);
    }
}
