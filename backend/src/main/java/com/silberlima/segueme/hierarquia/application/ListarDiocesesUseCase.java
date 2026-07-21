package com.silberlima.segueme.hierarquia.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.application.dto.DioceseArvoreResult;
import com.silberlima.segueme.hierarquia.application.dto.ParoquiaResumoResult;
import com.silberlima.segueme.hierarquia.application.dto.SetorArvoreResult;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * Monta a tabela hierárquica Nacional → Diocese → Setor → Paróquia (RF-054), escopada por
 * {@link EscopoTerritorial}. As métricas de seguidores nascem em 0: nascem placeholder até a
 * Fase 3 (Pessoa) entregar dado real para contagem por território.
 */
@Service
public class ListarDiocesesUseCase {

    private final DioceseRepository dioceseRepository;
    private final SetorRepository setorRepository;
    private final ParoquiaRepository paroquiaRepository;

    public ListarDiocesesUseCase(
            DioceseRepository dioceseRepository,
            SetorRepository setorRepository,
            ParoquiaRepository paroquiaRepository) {
        this.dioceseRepository = dioceseRepository;
        this.setorRepository = setorRepository;
        this.paroquiaRepository = paroquiaRepository;
    }

    public Page<DioceseArvoreResult> execute(EscopoTerritorial escopo, Pageable pageable) {
        Page<Diocese> dioceses = buscarDioceses(escopo, pageable);
        if (dioceses.isEmpty()) {
            return new PageImpl<>(List.of(), pageable, dioceses.getTotalElements());
        }

        List<UUID> dioceseIds = dioceses.getContent().stream().map(Diocese::getId).toList();
        List<Setor> setores = setorRepository.findByDiocese_IdInAndDeletedFalse(dioceseIds);

        List<UUID> setorIds = setores.stream().map(Setor::getId).toList();
        List<Paroquia> paroquias = paroquiaRepository.findBySetor_IdInAndDeletedFalse(setorIds);

        Map<UUID, List<Paroquia>> paroquiasPorSetor =
                paroquias.stream().collect(Collectors.groupingBy(p -> p.getSetor().getId()));
        Map<UUID, List<Setor>> setoresPorDiocese =
                setores.stream().collect(Collectors.groupingBy(s -> s.getDiocese().getId()));

        List<DioceseArvoreResult> resultado = dioceses.getContent().stream()
                .map(diocese -> montarDiocese(diocese, setoresPorDiocese, paroquiasPorSetor))
                .toList();

        return new PageImpl<>(resultado, pageable, dioceses.getTotalElements());
    }

    private Page<Diocese> buscarDioceses(EscopoTerritorial escopo, Pageable pageable) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> dioceseRepository.findByDeletedFalse(pageable);
            case EscopoTerritorial.DeDiocese deDiocese -> dioceseRepository
                    .findByIdAndDeletedFalse(deDiocese.dioceseId())
                    .map(diocese -> (Page<Diocese>) new PageImpl<>(List.of(diocese), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
            case EscopoTerritorial.DeSetor ignored -> throw new BusinessRuleViolationException(
                    "Perfil Setorial não possui acesso à consulta de Dioceses.");
            case EscopoTerritorial.DeParoquia ignored -> throw new BusinessRuleViolationException(
                    "Perfil Paróquia não possui acesso à consulta de Dioceses.");
        };
    }

    private DioceseArvoreResult montarDiocese(
            Diocese diocese, Map<UUID, List<Setor>> setoresPorDiocese, Map<UUID, List<Paroquia>> paroquiasPorSetor) {
        List<SetorArvoreResult> setores = setoresPorDiocese
                .getOrDefault(diocese.getId(), List.of())
                .stream()
                .map(setor -> montarSetor(setor, paroquiasPorSetor))
                .toList();
        int numParoquias = setores.stream().mapToInt(SetorArvoreResult::numParoquias).sum();
        return new DioceseArvoreResult(diocese.getId(), diocese.getNome(), numParoquias, 0, setores);
    }

    private SetorArvoreResult montarSetor(Setor setor, Map<UUID, List<Paroquia>> paroquiasPorSetor) {
        List<ParoquiaResumoResult> paroquias = paroquiasPorSetor
                .getOrDefault(setor.getId(), List.of())
                .stream()
                .map(paroquia -> new ParoquiaResumoResult(paroquia.getId(), paroquia.getNome()))
                .toList();
        return new SetorArvoreResult(setor.getId(), setor.getNome(), paroquias.size(), 0, paroquias);
    }
}
