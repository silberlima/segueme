package com.silberlima.segueme.encontros.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.application.dto.EncontroDetalhado;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.EquipeComando;
import com.silberlima.segueme.encontros.domain.EquipeComandoRepository;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigente;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigenteRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Consulta os Detalhes de um Encontro, incluindo Equipe Dirigente e Equipe Comando (RF-021). */
@Service
public class ConsultarEncontroUseCase {

    private final EncontroRepository encontroRepository;
    private final MembroEquipeDirigenteRepository membroEquipeDirigenteRepository;
    private final EquipeComandoRepository equipeComandoRepository;

    public ConsultarEncontroUseCase(
            EncontroRepository encontroRepository,
            MembroEquipeDirigenteRepository membroEquipeDirigenteRepository,
            EquipeComandoRepository equipeComandoRepository) {
        this.encontroRepository = encontroRepository;
        this.membroEquipeDirigenteRepository = membroEquipeDirigenteRepository;
        this.equipeComandoRepository = equipeComandoRepository;
    }

    @Transactional(readOnly = true)
    public EncontroDetalhado execute(UUID id) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + id));
        List<MembroEquipeDirigente> membrosDirigente = membroEquipeDirigenteRepository.findByEquipeDirigente_Encontro_Id(id);
        EquipeComando equipeComando = equipeComandoRepository
                .findByEncontro_Id(id)
                .orElseThrow(() -> new ResourceNotFoundException("Equipe Comando não encontrada para o encontro: " + id));
        return new EncontroDetalhado(encontro, membrosDirigente, equipeComando);
    }
}
