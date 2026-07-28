package com.silberlima.segueme.convites.application;

import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Lista os convites (RF-036/037) de um Encontro, com filtros opcionais de status/equipe/papel. */
@Service
public class ListarConvitesUseCase {

    private final IntegranteEquipeRepository integranteEquipeRepository;

    public ListarConvitesUseCase(IntegranteEquipeRepository integranteEquipeRepository) {
        this.integranteEquipeRepository = integranteEquipeRepository;
    }

    @Transactional(readOnly = true)
    public List<IntegranteEquipe> execute(
            UUID encontroId, StatusIntegrante status, UUID equipeId, PapelIntegrante papel) {
        List<IntegranteEquipe> convites = status != null
                ? integranteEquipeRepository.findByEncontro_IdAndStatus(encontroId, status)
                : integranteEquipeRepository.findByEncontro_Id(encontroId);

        return convites.stream()
                .filter(convite -> equipeId == null || convite.getEquipe().getId().equals(equipeId))
                .filter(convite -> papel == null || convite.getPapel() == papel)
                .toList();
    }
}
