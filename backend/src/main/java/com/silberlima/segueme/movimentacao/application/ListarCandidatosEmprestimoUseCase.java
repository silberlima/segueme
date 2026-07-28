package com.silberlima.segueme.movimentacao.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.PessoaSpecification;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pool de candidatos elegíveis a um {@link Emprestimo}, para o Setorial filtrar e selecionar
 * (RF-043, RN-MOVIMENTO-05): Seguidores do mesmo setor, de fora da paróquia do Encontro
 * solicitante, ainda não vinculados à equipe e ainda não candidatos deste mesmo empréstimo. Os
 * filtros de paróquia e habilidade são opcionais e dinâmicos (derivados dos candidatos existentes
 * no frontend, não de uma lista fixa).
 */
@Service
public class ListarCandidatosEmprestimoUseCase {

    private final EmprestimoRepository emprestimoRepository;
    private final PessoaRepository pessoaRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;
    private final EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    public ListarCandidatosEmprestimoUseCase(
            EmprestimoRepository emprestimoRepository,
            PessoaRepository pessoaRepository,
            IntegranteEquipeRepository integranteEquipeRepository,
            EmprestimoCandidatoRepository emprestimoCandidatoRepository) {
        this.emprestimoRepository = emprestimoRepository;
        this.pessoaRepository = pessoaRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
        this.emprestimoCandidatoRepository = emprestimoCandidatoRepository;
    }

    @Transactional(readOnly = true)
    public List<Pessoa> execute(UUID emprestimoId, EscopoTerritorial escopo, UUID paroquiaId, UUID competenciaId) {
        Emprestimo emprestimo = emprestimoRepository
                .findByIdAndDeletedFalse(emprestimoId)
                .orElseThrow(() -> new ResourceNotFoundException("Empréstimo não encontrado: " + emprestimoId));

        UUID paroquiaDoEncontroId = emprestimo.getEncontro().getParoquia().getId();
        Set<UUID> idsVinculados = integranteEquipeRepository.idsPessoaVinculadaNaEquipe(
                emprestimo.getEncontro().getId(), emprestimo.getEquipe().getId());
        Set<UUID> idsJaCandidatos = emprestimoCandidatoRepository.idsPessoaCandidataAoEmprestimo(emprestimoId);

        Specification<Pessoa> spec = Specification.allOf(
                PessoaSpecification.naoDeletada(),
                PessoaSpecification.comSituacao(SituacaoPessoa.SEGUIDOR),
                PessoaSpecification.comEscopo(escopo),
                PessoaSpecification.comParoquia(paroquiaId),
                PessoaSpecification.comCompetencia(competenciaId),
                PessoaSpecification.comParoquiaCarregada());

        List<Pessoa> candidatos = pessoaRepository.findAll(spec).stream()
                .filter(pessoa -> !pessoa.getParoquia().getId().equals(paroquiaDoEncontroId))
                .filter(pessoa -> !idsVinculados.contains(pessoa.getId()))
                .filter(pessoa -> !idsJaCandidatos.contains(pessoa.getId()))
                .toList();

        if (!candidatos.isEmpty()) {
            pessoaRepository.inicializarCompetencias(candidatos.stream().map(Pessoa::getId).toList());
        }
        return candidatos;
    }
}
