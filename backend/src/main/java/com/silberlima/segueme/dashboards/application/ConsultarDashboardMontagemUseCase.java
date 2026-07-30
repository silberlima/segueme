package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.DashboardMontagemResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.montagem.application.GerarSugestoesUseCase;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel de Montagem (`dash_montagem`, RF-053, RN-DASH-01). As sugestões inteligentes só existem
 * para este perfil (RN-08/RN-MONTAGEM-02) e vêm de {@code GerarSugestoesUseCase} — a decisão continua
 * humana (RN-10/RN-MONTAGEM-04), o painel apenas exibe o ranking.
 */
@Service
public class ConsultarDashboardMontagemUseCase {

    /** O protótipo mostra até 3 candidatos ranqueados no painel; a tela de sugestões mostra a lista completa. */
    private static final int MAXIMO_SUGESTOES_EXIBIDAS = 3;

    private final EncontroCorrenteResolver encontroCorrenteResolver;
    private final ListarEquipesUseCase listarEquipesUseCase;
    private final GerarSugestoesUseCase gerarSugestoesUseCase;

    public ConsultarDashboardMontagemUseCase(
            EncontroCorrenteResolver encontroCorrenteResolver,
            ListarEquipesUseCase listarEquipesUseCase,
            GerarSugestoesUseCase gerarSugestoesUseCase) {
        this.encontroCorrenteResolver = encontroCorrenteResolver;
        this.listarEquipesUseCase = listarEquipesUseCase;
        this.gerarSugestoesUseCase = gerarSugestoesUseCase;
    }

    @Transactional(readOnly = true)
    public DashboardMontagemResult execute(UUID paroquiaId) {
        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, LocalDate.now());
        if (corrente.isEmpty()) {
            return new DashboardMontagemResult(0, 0, 0, 0, null, List.of(), null, List.of());
        }

        Encontro encontro = corrente.get();
        List<EquipeComposicao> equipes = listarEquipesUseCase.execute(encontro.getId());
        EquipeComposicao prioritaria = equipePrioritaria(equipes);
        List<CandidatoSugerido> sugestoes = prioritaria == null
                ? List.of()
                : gerarSugestoesUseCase.execute(encontro.getId(), prioritaria.equipe().getId());

        return new DashboardMontagemResult(
                equipes.stream().filter(equipe -> !equipe.completa()).count(),
                equipes.stream().mapToLong(EquipeComposicao::vagasAbertas).sum(),
                sugestoes.size(),
                recusas(equipes),
                encontro,
                equipes,
                prioritaria,
                sugestoes.stream().limit(MAXIMO_SUGESTOES_EXIBIDAS).toList());
    }

    /**
     * "Equipe com maior prioridade" não está definida no protótipo: adotamos a de mais vagas abertas,
     * empatando pelo nome para o painel não trocar de equipe entre duas cargas iguais.
     */
    private EquipeComposicao equipePrioritaria(List<EquipeComposicao> equipes) {
        return equipes.stream()
                .filter(equipe -> equipe.vagasAbertas() > 0)
                .max(Comparator.comparingLong(EquipeComposicao::vagasAbertas)
                        .thenComparing(equipe -> equipe.equipe().getNome(), Comparator.reverseOrder()))
                .orElse(null);
    }

    private long recusas(List<EquipeComposicao> equipes) {
        return equipes.stream()
                .flatMap(equipe -> equipe.integrantes().stream())
                .map(IntegranteEquipe::getStatus)
                .filter(status -> status == StatusIntegrante.RECUSADO)
                .count();
    }
}
