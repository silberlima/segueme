package com.silberlima.segueme.hierarquia.application;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/** Lista as Paróquias do escopo territorial do usuário (RF-055), escopada por {@link EscopoTerritorial}. */
@Service
public class ListarParoquiasUseCase {

    private final ParoquiaRepository paroquiaRepository;

    public ListarParoquiasUseCase(ParoquiaRepository paroquiaRepository) {
        this.paroquiaRepository = paroquiaRepository;
    }

    public Page<Paroquia> execute(EscopoTerritorial escopo, Pageable pageable) {
        return switch (escopo) {
            case EscopoTerritorial.Nacional ignored -> paroquiaRepository.findByDeletedFalse(pageable);
            case EscopoTerritorial.DeDiocese deDiocese ->
                    paroquiaRepository.findBySetor_Diocese_IdAndDeletedFalse(deDiocese.dioceseId(), pageable);
            case EscopoTerritorial.DeSetor deSetor ->
                    paroquiaRepository.findBySetor_IdAndDeletedFalse(deSetor.setorId(), pageable);
            case EscopoTerritorial.DeParoquia deParoquia -> paroquiaRepository
                    .findByIdAndDeletedFalse(deParoquia.paroquiaId())
                    .map(paroquia -> (Page<Paroquia>) new PageImpl<>(List.of(paroquia), pageable, 1))
                    .orElseGet(() -> new PageImpl<>(List.of(), pageable, 0));
        };
    }
}
