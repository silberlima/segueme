package com.silberlima.segueme.dashboards.api.dto;

import com.silberlima.segueme.convites.api.dto.ConviteResponse;
import java.util.List;

/** Painel de Comando (`dash_comando`, RF-053): acompanhamento de convites e aceites por equipe. */
public record DashboardComandoResponse(
        long convitesEnviados,
        long aceites,
        long pendentes,
        long recusas,
        EncontroDoPainelResponse encontroCorrente,
        List<ConviteResponse> convitesRecentes,
        List<AceitesPorEquipeResponse> aceitesPorEquipe) {
}
