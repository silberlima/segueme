package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.PessoaSpecification;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista Pessoas do escopo territorial do usuário, com busca por nome/paróquia/competência (RF-007).
 *
 * <p>{@code @Transactional} porque, com {@code open-in-view} desligado, a resposta é mapeada no
 * Controller fora da sessão do Hibernate: a coleção {@code competencias} de cada pessoa é
 * inicializada aqui, em lote, antes da transação fechar.
 */
@Service
public class ListarPessoasUseCase {

    private final PessoaRepository pessoaRepository;

    public ListarPessoasUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Pessoa> execute(
            EscopoTerritorial escopo, String nome, UUID paroquiaId, UUID competenciaId, Pageable pageable) {
        Specification<Pessoa> spec = Specification.allOf(
                PessoaSpecification.naoDeletada(),
                PessoaSpecification.comEscopo(escopo),
                PessoaSpecification.comNome(nome),
                PessoaSpecification.comParoquia(paroquiaId),
                PessoaSpecification.comCompetencia(competenciaId),
                PessoaSpecification.comParoquiaCarregada());
        Page<Pessoa> pagina = pessoaRepository.findAll(spec, pageable);
        if (pagina.hasContent()) {
            pessoaRepository.inicializarCompetencias(pagina.getContent().stream().map(Pessoa::getId).toList());
        }
        return pagina;
    }
}
