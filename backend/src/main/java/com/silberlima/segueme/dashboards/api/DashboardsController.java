package com.silberlima.segueme.dashboards.api;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.dashboards.api.dto.DashboardComandoResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardDioceseResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardFichasResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardFinancasResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardMontagemResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardNacionalResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardParoquiaResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardPosEncontroResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardSetorialResponse;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardComandoUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardDioceseUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardFichasUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardFinancasUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardMontagemUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardNacionalUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardParoquiaUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardPosEncontroUseCase;
import com.silberlima.segueme.dashboards.application.ConsultarDashboardSetorialUseCase;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Os nove painéis iniciais, um por perfil operacional (RF-053). Cada rota é restrita ao perfil que a
 * documentação mapeia para ela (RN-DASH-01: um perfil não navega para o painel de outro) e o escopo
 * territorial vem sempre das claims do usuário autenticado (RN-04), nunca de parâmetro — assim não
 * há como pedir o painel de outra diocese, setor ou paróquia.
 *
 * <p>Os painéis das equipes não recebem {@code encontroId}: eles falam do Encontro corrente da
 * paróquia, resolvido por {@code EncontroCorrenteResolver}, como no protótipo (que não tem seletor
 * de Encontro nesses painéis).
 */
@RestController
@RequestMapping("/api/v1/dashboards")
@Tag(name = "Dashboards", description = "Painéis por perfil operacional (RF-053, RN-DASH-01/02)")
public class DashboardsController {

    private final ConsultarDashboardNacionalUseCase consultarDashboardNacionalUseCase;
    private final ConsultarDashboardDioceseUseCase consultarDashboardDioceseUseCase;
    private final ConsultarDashboardSetorialUseCase consultarDashboardSetorialUseCase;
    private final ConsultarDashboardParoquiaUseCase consultarDashboardParoquiaUseCase;
    private final ConsultarDashboardFichasUseCase consultarDashboardFichasUseCase;
    private final ConsultarDashboardMontagemUseCase consultarDashboardMontagemUseCase;
    private final ConsultarDashboardComandoUseCase consultarDashboardComandoUseCase;
    private final ConsultarDashboardFinancasUseCase consultarDashboardFinancasUseCase;
    private final ConsultarDashboardPosEncontroUseCase consultarDashboardPosEncontroUseCase;
    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final DashboardResponseMapper dashboardResponseMapper;

    public DashboardsController(
            ConsultarDashboardNacionalUseCase consultarDashboardNacionalUseCase,
            ConsultarDashboardDioceseUseCase consultarDashboardDioceseUseCase,
            ConsultarDashboardSetorialUseCase consultarDashboardSetorialUseCase,
            ConsultarDashboardParoquiaUseCase consultarDashboardParoquiaUseCase,
            ConsultarDashboardFichasUseCase consultarDashboardFichasUseCase,
            ConsultarDashboardMontagemUseCase consultarDashboardMontagemUseCase,
            ConsultarDashboardComandoUseCase consultarDashboardComandoUseCase,
            ConsultarDashboardFinancasUseCase consultarDashboardFinancasUseCase,
            ConsultarDashboardPosEncontroUseCase consultarDashboardPosEncontroUseCase,
            EscopoTerritorialResolver escopoTerritorialResolver,
            DashboardResponseMapper dashboardResponseMapper) {
        this.consultarDashboardNacionalUseCase = consultarDashboardNacionalUseCase;
        this.consultarDashboardDioceseUseCase = consultarDashboardDioceseUseCase;
        this.consultarDashboardSetorialUseCase = consultarDashboardSetorialUseCase;
        this.consultarDashboardParoquiaUseCase = consultarDashboardParoquiaUseCase;
        this.consultarDashboardFichasUseCase = consultarDashboardFichasUseCase;
        this.consultarDashboardMontagemUseCase = consultarDashboardMontagemUseCase;
        this.consultarDashboardComandoUseCase = consultarDashboardComandoUseCase;
        this.consultarDashboardFinancasUseCase = consultarDashboardFinancasUseCase;
        this.consultarDashboardPosEncontroUseCase = consultarDashboardPosEncontroUseCase;
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.dashboardResponseMapper = dashboardResponseMapper;
    }

    @GetMapping("/nacional")
    @PreAuthorize("hasRole('NACIONAL')")
    @Operation(summary = "Painel Nacional: seguidores, dioceses, paróquias, Encontros no ano, gráfico por região e dioceses em destaque")
    public DashboardNacionalResponse nacional() {
        return dashboardResponseMapper.toResponse(consultarDashboardNacionalUseCase.execute());
    }

    @GetMapping("/diocese")
    @PreAuthorize("hasRole('DIOCESE')")
    @Operation(summary = "Painel da Diocese: seguidores por setor e solicitações em aberto da diocese do usuário")
    public DashboardDioceseResponse diocese(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardDioceseUseCase.execute(dioceseId(jwt, authentication)));
    }

    @GetMapping("/setorial")
    @PreAuthorize("hasRole('SETORIAL')")
    @Operation(summary = "Painel do Setor: solicitações pendentes de aprovação e paróquias do setor do usuário")
    public DashboardSetorialResponse setorial(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardSetorialUseCase.execute(setorId(jwt, authentication)));
    }

    @GetMapping("/paroquia")
    @PreAuthorize("hasRole('PAROQUIA')")
    @Operation(summary = "Painel da Paróquia: Encontro corrente, progresso, pendências, Encontros recentes e financeiro")
    public DashboardParoquiaResponse paroquia(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardParoquiaUseCase.execute(paroquiaId(jwt, authentication)));
    }

    @GetMapping("/fichas")
    @PreAuthorize("hasRole('EQUIPE_FICHAS')")
    @Operation(summary = "Painel de Fichas: funil de inscrição, fichas a preencher e fila de reserva do Encontro corrente")
    public DashboardFichasResponse fichas(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardFichasUseCase.execute(paroquiaId(jwt, authentication)));
    }

    @GetMapping("/montagem")
    @PreAuthorize("hasRole('EQUIPE_MONTAGEM')")
    @Operation(summary = "Painel de Montagem: equipes do Encontro corrente e sugestões inteligentes (RN-MONTAGEM-02)")
    public DashboardMontagemResponse montagem(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardMontagemUseCase.execute(paroquiaId(jwt, authentication)));
    }

    @GetMapping("/comando")
    @PreAuthorize("hasRole('EQUIPE_COMANDO')")
    @Operation(summary = "Painel de Comando: acompanhamento de convites e aceites por equipe do Encontro corrente")
    public DashboardComandoResponse comando(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardComandoUseCase.execute(paroquiaId(jwt, authentication)));
    }

    @GetMapping("/financas")
    @PreAuthorize("hasRole('EQUIPE_FINANCAS')")
    @Operation(summary = "Painel Financeiro: saldo, receitas, despesas, campanhas e lançamentos do Encontro corrente")
    public DashboardFinancasResponse financas(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardFinancasUseCase.execute(paroquiaId(jwt, authentication)));
    }

    @GetMapping("/pos-encontro")
    @PreAuthorize("hasRole('POS_ENCONTRO')")
    @Operation(summary = "Painel Pós-Encontro: acompanhados, presença média, frequência mensal e ranking de prioridade")
    public DashboardPosEncontroResponse posEncontro(
            @AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        return dashboardResponseMapper.toResponse(
                consultarDashboardPosEncontroUseCase.execute(paroquiaId(jwt, authentication)));
    }

    private UUID dioceseId(Jwt jwt, Authentication authentication) {
        if (escopo(jwt, authentication) instanceof EscopoTerritorial.DeDiocese deDiocese) {
            return deDiocese.dioceseId();
        }
        throw new BusinessRuleViolationException("O Painel da Diocese exige um usuário com escopo de diocese.");
    }

    private UUID setorId(Jwt jwt, Authentication authentication) {
        if (escopo(jwt, authentication) instanceof EscopoTerritorial.DeSetor deSetor) {
            return deSetor.setorId();
        }
        throw new BusinessRuleViolationException("O Painel do Setor exige um usuário com escopo de setor.");
    }

    private UUID paroquiaId(Jwt jwt, Authentication authentication) {
        if (escopo(jwt, authentication) instanceof EscopoTerritorial.DeParoquia deParoquia) {
            return deParoquia.paroquiaId();
        }
        throw new BusinessRuleViolationException("Este painel exige um usuário com escopo de paróquia.");
    }

    private EscopoTerritorial escopo(Jwt jwt, Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        return escopoTerritorialResolver.resolve(
                Perfil.fromAuthorities(authorities),
                jwt.getClaimAsString("diocese_id"),
                jwt.getClaimAsString("setor_id"),
                jwt.getClaimAsString("paroquia_id"));
    }
}
