package com.silberlima.segueme.encontros.application;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.EncontroSpecification;
import com.silberlima.segueme.encontros.domain.StatusEncontro;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista Encontros do escopo territorial do usuário, com busca por número/status/paróquia (RF-014). */
@Service
public class ListarEncontrosUseCase {

    private final EncontroRepository encontroRepository;

    public ListarEncontrosUseCase(EncontroRepository encontroRepository) {
        this.encontroRepository = encontroRepository;
    }

    @Transactional(readOnly = true)
    public Page<Encontro> execute(
            EscopoTerritorial escopo, Integer numero, StatusEncontro status, UUID paroquiaId, Pageable pageable) {
        Specification<Encontro> spec = Specification.allOf(
                EncontroSpecification.naoDeletado(),
                EncontroSpecification.comEscopo(escopo),
                EncontroSpecification.comNumero(numero),
                EncontroSpecification.comStatus(status),
                EncontroSpecification.comParoquia(paroquiaId),
                EncontroSpecification.comParoquiaCarregada(),
                EncontroSpecification.comEquipeComandoCarregada());
        return encontroRepository.findAll(spec, pageable);
    }
}
