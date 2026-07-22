package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.stream.Stream;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista os Casais do escopo territorial do usuário (RF-012), escopado pela paróquia de {@code pessoa1}.
 *
 * <p>{@code @Transactional} pelo mesmo motivo de {@link ListarPessoasUseCase}: a coleção
 * {@code competencias} de cada pessoa do casal é inicializada aqui antes da transação fechar,
 * já que {@code CasalRepository} não pode fazer fetch join de duas coleções (pessoa1 e pessoa2)
 * numa consulta paginada sem cair no limite do Hibernate de múltiplos "bags".
 */
@Service
public class ListarCasaisUseCase {

    private final CasalRepository casalRepository;
    private final PessoaRepository pessoaRepository;

    public ListarCasaisUseCase(CasalRepository casalRepository, PessoaRepository pessoaRepository) {
        this.casalRepository = casalRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public Page<Casal> execute(EscopoTerritorial escopo, Pageable pageable) {
        Page<Casal> pagina = switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> casalRepository.findByDeletedFalse(pageable);
            case EscopoTerritorial.DeDiocese deDiocese ->
                    casalRepository.findByPessoa1_Paroquia_Setor_Diocese_IdAndDeletedFalse(
                            deDiocese.dioceseId(), pageable);
            case EscopoTerritorial.DeSetor deSetor ->
                    casalRepository.findByPessoa1_Paroquia_Setor_IdAndDeletedFalse(deSetor.setorId(), pageable);
            case EscopoTerritorial.DeParoquia deParoquia ->
                    casalRepository.findByPessoa1_Paroquia_IdAndDeletedFalse(deParoquia.paroquiaId(), pageable);
        };
        if (pagina.hasContent()) {
            var pessoaIds = pagina.getContent().stream()
                    .flatMap(casal -> Stream.of(casal.getPessoa1(), casal.getPessoa2()))
                    .map(Pessoa::getId)
                    .toList();
            pessoaRepository.inicializarCompetencias(pessoaIds);
        }
        return pagina;
    }
}
