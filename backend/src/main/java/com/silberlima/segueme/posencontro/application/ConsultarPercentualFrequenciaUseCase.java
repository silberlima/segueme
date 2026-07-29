package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.posencontro.domain.ContagemEncontrosPos;
import com.silberlima.segueme.posencontro.domain.ContagemPresencas;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import com.silberlima.segueme.posencontro.domain.JanelaApuracao;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Percentual de frequência no pós-encontro de todas as pessoas com registro, numa única apuração
 * (duas consultas agregadas, sem N+1). É a porta pela qual a Fase 6 consome este módulo: o
 * {@code SuggestionEngine} pondera este fator em 20% do score de match (RN-09/RN-MONTAGEM-03) e
 * nasceu com placeholder zerado justamente esperando esta fase (`plano-implementacao.md` seção 19).
 *
 * <p>Quem não aparece no mapa retornado não tem nenhuma presença registrada — o chamador trata a
 * ausência como "sem dado" em vez de 0%, para não punir no score quem nunca teve encontro pós na
 * paróquia. Quem tem presenças em mais de uma paróquia (transferido no meio do caminho, Fase 8)
 * entra com o maior percentual apurado, em vez de somar presenças sobre denominadores diferentes.
 */
@Service
public class ConsultarPercentualFrequenciaUseCase {

    private final EncontroPosRepository encontroPosRepository;
    private final FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    public ConsultarPercentualFrequenciaUseCase(
            EncontroPosRepository encontroPosRepository,
            FrequenciaPosEncontroRepository frequenciaPosEncontroRepository) {
        this.encontroPosRepository = encontroPosRepository;
        this.frequenciaPosEncontroRepository = frequenciaPosEncontroRepository;
    }

    @Transactional(readOnly = true)
    public Map<UUID, Integer> execute() {
        JanelaApuracao janela = JanelaApuracao.normalizar(null, null);

        Map<UUID, Long> encontrosPorParoquia = encontroPosRepository
                .contarPorParoquia(janela.de(), janela.ate())
                .stream()
                .collect(Collectors.toMap(ContagemEncontrosPos::getParoquiaId, ContagemEncontrosPos::getTotal));

        Map<UUID, Integer> percentualPorPessoa = new HashMap<>();
        for (ContagemPresencas contagem : frequenciaPosEncontroRepository.contarPresencas(
                janela.de(), janela.ate())) {
            long totalEncontros = encontrosPorParoquia.getOrDefault(contagem.getParoquiaId(), 0L);
            if (totalEncontros == 0) {
                continue;
            }
            int percentual = (int) Math.round((contagem.getPresencas() * 100.0) / totalEncontros);
            percentualPorPessoa.merge(contagem.getPessoaId(), percentual, Math::max);
        }
        return percentualPorPessoa;
    }
}
