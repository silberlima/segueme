package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Ranking de prioridade para ser convidado a servir nas próximas equipes (RF-059, RN-POS-02/03):
 * a mesma apuração da tela Frequência, sem recorte de período (acumulado de todos os encontros pós
 * já realizados) e ordenada da maior para a menor frequência.
 */
@Service
public class ListarPrioridadesUseCase {

    private static final Comparator<FrequenciaPessoa> MAIOR_FREQUENCIA_PRIMEIRO = Comparator.comparingInt(
                    FrequenciaPessoa::percentual)
            .reversed()
            .thenComparing(frequencia -> frequencia.pessoa().getNomeCompleto());

    private final ConsultarFrequenciaUseCase consultarFrequenciaUseCase;

    public ListarPrioridadesUseCase(ConsultarFrequenciaUseCase consultarFrequenciaUseCase) {
        this.consultarFrequenciaUseCase = consultarFrequenciaUseCase;
    }

    @Transactional(readOnly = true)
    public List<FrequenciaPessoa> execute(UUID paroquiaId) {
        return consultarFrequenciaUseCase.execute(paroquiaId, null, null).stream()
                .sorted(MAIOR_FREQUENCIA_PRIMEIRO)
                .toList();
    }
}
