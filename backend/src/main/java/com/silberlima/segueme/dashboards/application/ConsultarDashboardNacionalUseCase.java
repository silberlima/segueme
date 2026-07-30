package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.ContagemRegiao;
import com.silberlima.segueme.dashboards.application.dto.DashboardNacionalResult;
import com.silberlima.segueme.dashboards.application.dto.DioceseDestaque;
import com.silberlima.segueme.dashboards.domain.PeriodoApuracao;
import com.silberlima.segueme.dashboards.domain.TotaisPorTerritorio;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.RegiaoBrasil;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel Nacional (`dash_nacional`, RF-053, RN-DASH-01). Toda contagem por território sai de
 * consultas agrupadas, nunca de uma consulta por diocese (`skills/05-persistencia.md`, N+1).
 */
@Service
public class ConsultarDashboardNacionalUseCase {

    /** O protótipo mostra um recorte "top N + ver todos"; 5 é o tamanho do ranking em todos os painéis. */
    private static final int MAXIMO_DESTAQUES = 5;

    private final PessoaRepository pessoaRepository;
    private final DioceseRepository dioceseRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final EncontroRepository encontroRepository;

    public ConsultarDashboardNacionalUseCase(
            PessoaRepository pessoaRepository,
            DioceseRepository dioceseRepository,
            ParoquiaRepository paroquiaRepository,
            EncontroRepository encontroRepository) {
        this.pessoaRepository = pessoaRepository;
        this.dioceseRepository = dioceseRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.encontroRepository = encontroRepository;
    }

    @Transactional(readOnly = true)
    public DashboardNacionalResult execute() {
        PeriodoApuracao ano = PeriodoApuracao.anoDe(LocalDate.now());

        return new DashboardNacionalResult(
                pessoaRepository.countBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR),
                dioceseRepository.countByDeletedFalse(),
                paroquiaRepository.countByDeletedFalse(),
                encontroRepository.countByDataInicioBetweenAndDeletedFalse(ano.de(), ano.ate()),
                encontrosPorRegiao(ano),
                diocesesDestaque(ano));
    }

    private List<ContagemRegiao> encontrosPorRegiao(PeriodoApuracao ano) {
        Map<RegiaoBrasil, Long> totais = new EnumMap<>(RegiaoBrasil.class);
        long semRegiao = 0;
        for (var contagem : encontroRepository.contarPorRegiao(ano.de(), ano.ate())) {
            if (contagem.getRegiao() == null) {
                semRegiao += contagem.getTotal();
            } else {
                totais.put(contagem.getRegiao(), contagem.getTotal());
            }
        }

        // As cinco regiões aparecem sempre, mesmo zeradas, para o gráfico não mudar de forma a cada ano.
        List<ContagemRegiao> barras = new ArrayList<>();
        for (RegiaoBrasil regiao : RegiaoBrasil.values()) {
            barras.add(new ContagemRegiao(regiao, totais.getOrDefault(regiao, 0L)));
        }
        if (semRegiao > 0) {
            barras.add(new ContagemRegiao(null, semRegiao));
        }
        return List.copyOf(barras);
    }

    private List<DioceseDestaque> diocesesDestaque(PeriodoApuracao ano) {
        TotaisPorTerritorio paroquias = TotaisPorTerritorio.de(paroquiaRepository.contarPorDiocese());
        TotaisPorTerritorio seguidores =
                TotaisPorTerritorio.de(pessoaRepository.contarPorDiocese(SituacaoPessoa.SEGUIDOR));
        TotaisPorTerritorio encontros =
                TotaisPorTerritorio.de(encontroRepository.contarPorDiocese(ano.de(), ano.ate()));

        return dioceseRepository.findByDeletedFalseOrderByNomeAsc().stream()
                .map(diocese -> new DioceseDestaque(
                        diocese.getId(),
                        diocese.getNome(),
                        paroquias.total(diocese.getId()),
                        seguidores.total(diocese.getId()),
                        encontros.total(diocese.getId())))
                .sorted(Comparator.comparingLong(DioceseDestaque::seguidores)
                        .thenComparingLong(DioceseDestaque::paroquias)
                        .reversed()
                        .thenComparing(DioceseDestaque::nome))
                .limit(MAXIMO_DESTAQUES)
                .toList();
    }
}
