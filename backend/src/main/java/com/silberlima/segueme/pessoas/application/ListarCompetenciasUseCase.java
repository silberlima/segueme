package com.silberlima.segueme.pessoas.application;

import com.silberlima.segueme.pessoas.application.dto.CompetenciaComContagem;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.ContagemPorCompetencia;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

/**
 * Lista o catálogo nacional de competências com a contagem de pessoas de cada uma (RF-013).
 * A contagem é obtida em uma única consulta agregada ({@code PessoaRepository#contarPorCompetencia})
 * para evitar N+1 (`skills/05-persistencia.md`), mesmo com o catálogo sendo pequeno.
 */
@Service
public class ListarCompetenciasUseCase {

    private final CompetenciaRepository competenciaRepository;
    private final PessoaRepository pessoaRepository;

    public ListarCompetenciasUseCase(CompetenciaRepository competenciaRepository, PessoaRepository pessoaRepository) {
        this.competenciaRepository = competenciaRepository;
        this.pessoaRepository = pessoaRepository;
    }

    public List<CompetenciaComContagem> execute() {
        Map<UUID, Long> contagens = pessoaRepository.contarPorCompetencia().stream()
                .collect(Collectors.toMap(ContagemPorCompetencia::getCompetenciaId, ContagemPorCompetencia::getTotal));

        Function<Competencia, CompetenciaComContagem> comContagem =
                competencia -> new CompetenciaComContagem(competencia, contagens.getOrDefault(competencia.getId(), 0L));

        return competenciaRepository.findByDeletedFalseOrderByNomeAsc().stream()
                .map(comContagem)
                .toList();
    }
}
