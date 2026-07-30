package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.convites.application.ListarConvitesUseCase;
import com.silberlima.segueme.dashboards.application.dto.AceitesPorEquipe;
import com.silberlima.segueme.dashboards.application.dto.DashboardComandoResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel de Comando (`dash_comando`, RF-053, RN-DASH-01): acompanhamento dos convites do Encontro
 * corrente. Reaproveita {@code ListarConvitesUseCase} — a lista já vem com equipe e pessoa
 * carregadas, então os quatro KPIs e o gráfico por equipe saem de uma consulta só, sem N+1.
 */
@Service
public class ConsultarDashboardComandoUseCase {

    private static final int MAXIMO_CONVITES_EXIBIDOS = 5;

    private final EncontroCorrenteResolver encontroCorrenteResolver;
    private final ListarConvitesUseCase listarConvitesUseCase;

    public ConsultarDashboardComandoUseCase(
            EncontroCorrenteResolver encontroCorrenteResolver, ListarConvitesUseCase listarConvitesUseCase) {
        this.encontroCorrenteResolver = encontroCorrenteResolver;
        this.listarConvitesUseCase = listarConvitesUseCase;
    }

    @Transactional(readOnly = true)
    public DashboardComandoResult execute(UUID paroquiaId) {
        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, LocalDate.now());
        if (corrente.isEmpty()) {
            return new DashboardComandoResult(0, 0, 0, 0, null, List.of(), List.of());
        }

        Encontro encontro = corrente.get();
        List<IntegranteEquipe> convites = listarConvitesUseCase.execute(encontro.getId(), null, null, null);

        return new DashboardComandoResult(
                convites.size(),
                contarPorStatus(convites, StatusIntegrante.ACEITO),
                contarPorStatus(convites, StatusIntegrante.PENDENTE),
                contarPorStatus(convites, StatusIntegrante.RECUSADO),
                encontro,
                convitesRecentes(convites),
                aceitesPorEquipe(convites));
    }

    private long contarPorStatus(List<IntegranteEquipe> convites, StatusIntegrante status) {
        return convites.stream().filter(convite -> convite.getStatus() == status).count();
    }

    private List<IntegranteEquipe> convitesRecentes(List<IntegranteEquipe> convites) {
        return convites.stream()
                .sorted(Comparator.comparing(IntegranteEquipe::getDataEnvioConvite, Comparator.reverseOrder()))
                .limit(MAXIMO_CONVITES_EXIBIDOS)
                .toList();
    }

    private List<AceitesPorEquipe> aceitesPorEquipe(List<IntegranteEquipe> convites) {
        Map<UUID, AceitesPorEquipe> porEquipe = new LinkedHashMap<>();
        for (IntegranteEquipe convite : convites) {
            if (convite.getStatus() == StatusIntegrante.RECUSADO) {
                continue;
            }
            UUID equipeId = convite.getEquipe().getId();
            AceitesPorEquipe atual = porEquipe.get(equipeId);
            long aceitos = (atual == null ? 0 : atual.aceitos())
                    + (convite.getStatus() == StatusIntegrante.ACEITO ? 1 : 0);
            long convidados = (atual == null ? 0 : atual.convidados()) + 1;
            porEquipe.put(
                    equipeId, new AceitesPorEquipe(equipeId, convite.getEquipe().getNome(), aceitos, convidados));
        }
        return porEquipe.values().stream()
                .sorted(Comparator.comparing(AceitesPorEquipe::equipeNome))
                .toList();
    }
}
