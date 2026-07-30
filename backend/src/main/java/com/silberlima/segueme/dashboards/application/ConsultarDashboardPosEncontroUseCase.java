package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.ContagemMensal;
import com.silberlima.segueme.dashboards.application.dto.DashboardPosEncontroResult;
import com.silberlima.segueme.dashboards.domain.PeriodoApuracao;
import com.silberlima.segueme.posencontro.application.ConsultarAcompanhadosUseCase;
import com.silberlima.segueme.posencontro.application.ListarPrioridadesUseCase;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FaixaPrioridade;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel Pós-Encontro (`dash_posencontro`, RF-053, RN-DASH-01): jovens acompanhados, presença média,
 * encontros pós no mês e o ranking de prioridade — o mesmo de {@code ListarPrioridadesUseCase}, que
 * o motor de sugestões da Fase 6 pondera em 20% do score (RN-POS-02/03).
 */
@Service
public class ConsultarDashboardPosEncontroUseCase {

    private static final int MESES_DO_GRAFICO = 6;
    private static final int MAXIMO_PRIORIDADES_EXIBIDAS = 5;

    private final ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase;
    private final ListarPrioridadesUseCase listarPrioridadesUseCase;
    private final EncontroPosRepository encontroPosRepository;
    private final FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    public ConsultarDashboardPosEncontroUseCase(
            ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase,
            ListarPrioridadesUseCase listarPrioridadesUseCase,
            EncontroPosRepository encontroPosRepository,
            FrequenciaPosEncontroRepository frequenciaPosEncontroRepository) {
        this.consultarAcompanhadosUseCase = consultarAcompanhadosUseCase;
        this.listarPrioridadesUseCase = listarPrioridadesUseCase;
        this.encontroPosRepository = encontroPosRepository;
        this.frequenciaPosEncontroRepository = frequenciaPosEncontroRepository;
    }

    @Transactional(readOnly = true)
    public DashboardPosEncontroResult execute(UUID paroquiaId) {
        LocalDate hoje = LocalDate.now();
        PeriodoApuracao mes = PeriodoApuracao.mesDe(hoje);
        List<FrequenciaPessoa> prioridades = listarPrioridadesUseCase.execute(paroquiaId);
        Optional<EncontroPos> proximo =
                encontroPosRepository.findTopByParoquia_IdAndDataGreaterThanEqualAndDeletedFalseOrderByDataAsc(
                        paroquiaId, hoje);

        return new DashboardPosEncontroResult(
                consultarAcompanhadosUseCase.execute(paroquiaId).size(),
                presencaMedia(prioridades),
                encontroPosRepository.countByParoquia_IdAndDataBetweenAndDeletedFalse(paroquiaId, mes.de(), mes.ate()),
                prioridades.stream()
                        .filter(frequencia -> frequencia.faixa() == FaixaPrioridade.ALTA)
                        .count(),
                frequenciaMensal(paroquiaId, hoje),
                proximo.orElse(null),
                proximo.map(encontroPos -> frequenciaPosEncontroRepository
                                .countByEncontroPos_IdAndPresenteTrueAndDeletedFalse(encontroPos.getId()))
                        .orElse(0L),
                prioridades.stream().limit(MAXIMO_PRIORIDADES_EXIBIDAS).toList());
    }

    /** Média dos percentuais de frequência dos acompanhados; 0 quando ninguém é acompanhado ainda. */
    private int presencaMedia(List<FrequenciaPessoa> prioridades) {
        return (int) Math.round(prioridades.stream()
                .mapToInt(FrequenciaPessoa::percentual)
                .average()
                .orElse(0));
    }

    private List<ContagemMensal> frequenciaMensal(UUID paroquiaId, LocalDate hoje) {
        PeriodoApuracao janela = PeriodoApuracao.ultimosMeses(hoje, MESES_DO_GRAFICO);
        Map<YearMonth, Long> totais = new HashMap<>();
        for (var contagem :
                frequenciaPosEncontroRepository.contarPresencasPorData(paroquiaId, janela.de(), janela.ate())) {
            totais.merge(YearMonth.from(contagem.getData()), contagem.getTotal(), Long::sum);
        }

        // Meses sem encontro pós entram zerados: o gráfico mensal tem sempre a mesma largura.
        List<ContagemMensal> barras = new ArrayList<>();
        YearMonth mesInicial = YearMonth.from(janela.de());
        for (int indice = 0; indice < MESES_DO_GRAFICO; indice++) {
            YearMonth mes = mesInicial.plusMonths(indice);
            barras.add(new ContagemMensal(mes, totais.getOrDefault(mes, 0L)));
        }
        return List.copyOf(barras);
    }
}
