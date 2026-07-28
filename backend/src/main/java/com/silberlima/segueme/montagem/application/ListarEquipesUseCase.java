package com.silberlima.segueme.montagem.application;

import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista as equipes de serviço de um Encontro (todo tipo do catálogo nacional), com a composição de cada uma (RF-028). */
@Service
public class ListarEquipesUseCase {

    private final EquipeRepository equipeRepository;
    private final IntegranteEquipeRepository integranteEquipeRepository;

    public ListarEquipesUseCase(EquipeRepository equipeRepository, IntegranteEquipeRepository integranteEquipeRepository) {
        this.equipeRepository = equipeRepository;
        this.integranteEquipeRepository = integranteEquipeRepository;
    }

    @Transactional(readOnly = true)
    public List<EquipeComposicao> execute(UUID encontroId) {
        List<Equipe> equipes = equipeRepository.findByDeletedFalseOrderByNomeAsc();
        Map<UUID, List<IntegranteEquipe>> integrantesPorEquipe = integranteEquipeRepository
                .findByEncontro_Id(encontroId)
                .stream()
                .collect(Collectors.groupingBy(integrante -> integrante.getEquipe().getId()));

        return equipes.stream()
                .map(equipe -> new EquipeComposicao(
                        equipe, integrantesPorEquipe.getOrDefault(equipe.getId(), List.of())))
                .toList();
    }
}
