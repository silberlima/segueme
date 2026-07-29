package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.posencontro.application.dto.PresencaPessoa;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontro;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista de chamada de um encontro pós: todo o pool de acompanhados da paróquia com a marcação já
 * registrada de cada um (tela Presenças, UC-20). Quem ainda não foi conferido vem com
 * {@code presente == null}, o que permite à tela distinguir "não conferido" de "ausente".
 */
@Service
public class ConsultarPresencasUseCase {

    private final EncontroPosRepository encontroPosRepository;
    private final FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;
    private final ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase;

    public ConsultarPresencasUseCase(
            EncontroPosRepository encontroPosRepository,
            FrequenciaPosEncontroRepository frequenciaPosEncontroRepository,
            ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase) {
        this.encontroPosRepository = encontroPosRepository;
        this.frequenciaPosEncontroRepository = frequenciaPosEncontroRepository;
        this.consultarAcompanhadosUseCase = consultarAcompanhadosUseCase;
    }

    @Transactional(readOnly = true)
    public List<PresencaPessoa> execute(UUID encontroPosId) {
        EncontroPos encontroPos = encontroPosRepository
                .findByIdAndDeletedFalse(encontroPosId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro pós não encontrado: " + encontroPosId));

        Map<UUID, Boolean> presencaPorPessoa = frequenciaPosEncontroRepository
                .findByEncontroPos_IdAndDeletedFalse(encontroPosId)
                .stream()
                .collect(Collectors.toMap(
                        frequencia -> frequencia.getPessoa().getId(), FrequenciaPosEncontro::isPresente));

        return consultarAcompanhadosUseCase.execute(encontroPos.getParoquia().getId()).stream()
                .map(pessoa -> new PresencaPessoa(pessoa, presencaPorPessoa.get(pessoa.getId())))
                .toList();
    }
}
