package com.silberlima.segueme.dashboards.api;

import com.silberlima.segueme.convites.api.ConviteResponseMapper;
import com.silberlima.segueme.dashboards.api.dto.AceitesPorEquipeResponse;
import com.silberlima.segueme.dashboards.api.dto.ContagemMensalResponse;
import com.silberlima.segueme.dashboards.api.dto.ContagemRegiaoResponse;
import com.silberlima.segueme.dashboards.api.dto.ContagemTerritorioResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardComandoResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardDioceseResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardFichasResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardFinancasResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardMontagemResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardNacionalResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardParoquiaResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardPosEncontroResponse;
import com.silberlima.segueme.dashboards.api.dto.DashboardSetorialResponse;
import com.silberlima.segueme.dashboards.api.dto.DioceseDestaqueResponse;
import com.silberlima.segueme.dashboards.api.dto.EncontroDoPainelResponse;
import com.silberlima.segueme.dashboards.api.dto.ParoquiaDoSetorResponse;
import com.silberlima.segueme.dashboards.api.dto.PendenciaResponse;
import com.silberlima.segueme.dashboards.application.dto.DashboardComandoResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardDioceseResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardFichasResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardFinancasResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardMontagemResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardNacionalResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardParoquiaResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardPosEncontroResult;
import com.silberlima.segueme.dashboards.application.dto.DashboardSetorialResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.api.CampanhaResponseMapper;
import com.silberlima.segueme.financeiro.api.LancamentoFinanceiroResponseMapper;
import com.silberlima.segueme.financeiro.api.dto.CampanhaResponse;
import com.silberlima.segueme.financeiro.api.dto.ResumoFinanceiroResponse;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.inscricoes.api.CadastroReservaResponseMapper;
import com.silberlima.segueme.inscricoes.api.PreInscricaoResponseMapper;
import com.silberlima.segueme.inscricoes.api.dto.CadastroReservaResponse;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.montagem.api.MontagemResponseMapper;
import com.silberlima.segueme.montagem.api.dto.EquipeResumoResponse;
import com.silberlima.segueme.movimentacao.api.SolicitacaoResponseMapper;
import com.silberlima.segueme.posencontro.api.PosEncontroResponseMapper;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.IntStream;
import org.springframework.stereotype.Component;

/**
 * Traduz os resultados dos nove painéis para os contratos de API. Reaproveita os mappers de cada
 * domínio (equipes, convites, solicitações, inscrições, financeiro, pós-encontro) para que o painel
 * e a tela de detalhe do mesmo dado nunca divirjam de formato — o padrão visual comum dos painéis
 * (`dashboards-por-perfil.md` 2) depende disso.
 */
@Component
public class DashboardResponseMapper {

    private static final DateTimeFormatter MES = DateTimeFormatter.ofPattern("yyyy-MM");

    private final MontagemResponseMapper montagemResponseMapper;
    private final ConviteResponseMapper conviteResponseMapper;
    private final SolicitacaoResponseMapper solicitacaoResponseMapper;
    private final PreInscricaoResponseMapper preInscricaoResponseMapper;
    private final CadastroReservaResponseMapper cadastroReservaResponseMapper;
    private final CampanhaResponseMapper campanhaResponseMapper;
    private final LancamentoFinanceiroResponseMapper lancamentoFinanceiroResponseMapper;
    private final PosEncontroResponseMapper posEncontroResponseMapper;

    public DashboardResponseMapper(
            MontagemResponseMapper montagemResponseMapper,
            ConviteResponseMapper conviteResponseMapper,
            SolicitacaoResponseMapper solicitacaoResponseMapper,
            PreInscricaoResponseMapper preInscricaoResponseMapper,
            CadastroReservaResponseMapper cadastroReservaResponseMapper,
            CampanhaResponseMapper campanhaResponseMapper,
            LancamentoFinanceiroResponseMapper lancamentoFinanceiroResponseMapper,
            PosEncontroResponseMapper posEncontroResponseMapper) {
        this.montagemResponseMapper = montagemResponseMapper;
        this.conviteResponseMapper = conviteResponseMapper;
        this.solicitacaoResponseMapper = solicitacaoResponseMapper;
        this.preInscricaoResponseMapper = preInscricaoResponseMapper;
        this.cadastroReservaResponseMapper = cadastroReservaResponseMapper;
        this.campanhaResponseMapper = campanhaResponseMapper;
        this.lancamentoFinanceiroResponseMapper = lancamentoFinanceiroResponseMapper;
        this.posEncontroResponseMapper = posEncontroResponseMapper;
    }

    public DashboardNacionalResponse toResponse(DashboardNacionalResult result) {
        return new DashboardNacionalResponse(
                result.seguidores(),
                result.dioceses(),
                result.paroquias(),
                result.encontrosNoAno(),
                result.encontrosPorRegiao().stream()
                        .map(contagem -> new ContagemRegiaoResponse(
                                contagem.regiao() == null ? null : contagem.regiao().name(), contagem.total()))
                        .toList(),
                result.diocesesDestaque().stream()
                        .map(destaque -> new DioceseDestaqueResponse(
                                destaque.dioceseId(),
                                destaque.nome(),
                                destaque.paroquias(),
                                destaque.seguidores(),
                                destaque.encontrosNoAno()))
                        .toList());
    }

    public DashboardDioceseResponse toResponse(DashboardDioceseResult result) {
        return new DashboardDioceseResponse(
                result.seguidores(),
                result.setores(),
                result.paroquias(),
                result.encontrosNoAno(),
                result.seguidoresPorSetor().stream()
                        .map(contagem -> new ContagemTerritorioResponse(
                                contagem.territorioId(), contagem.nome(), contagem.total()))
                        .toList(),
                result.solicitacoesAoSetorial());
    }

    public DashboardSetorialResponse toResponse(DashboardSetorialResult result) {
        return new DashboardSetorialResponse(
                result.solicitacoesPendentes(),
                result.transferenciasNoMes(),
                result.emprestimosAtivos(),
                result.seguidores(),
                result.solicitacoes().stream().map(solicitacaoResponseMapper::toResponse).toList(),
                result.paroquias().stream()
                        .map(paroquia -> new ParoquiaDoSetorResponse(
                                paroquia.paroquiaId(),
                                paroquia.nome(),
                                paroquia.seguidores(),
                                paroquia.proximoEncontro()))
                        .toList());
    }

    public DashboardParoquiaResponse toResponse(DashboardParoquiaResult result) {
        return new DashboardParoquiaResponse(
                result.seguidoresAtivos(),
                result.encontrosRealizados(),
                result.diasParaProximoEncontro(),
                result.totalPendencias(),
                toEncontroResponse(result.encontroCorrente()),
                result.inscricoesConfirmadas(),
                toEquipesResponse(result),
                result.encontrosRecentes().stream()
                        .map(this::toEncontroResponse)
                        .toList(),
                result.pendencias().stream()
                        .map(pendencia ->
                                new PendenciaResponse(pendencia.tipo().name(), pendencia.quantidade()))
                        .toList(),
                toResumoResponse(result.financeiro()),
                toCampanhasResponse(result.campanhas()));
    }

    public DashboardFichasResponse toResponse(DashboardFichasResult result) {
        return new DashboardFichasResponse(
                result.aguardandoFicha(),
                result.emAnalise(),
                result.confirmados(),
                result.vagasRestantes(),
                result.cadastroReserva(),
                result.seguimistas(),
                result.seguidores(),
                toEncontroResponse(result.encontroCorrente()),
                result.preInscricoesAguardandoFicha().stream()
                        .map(preInscricaoResponseMapper::toResponse)
                        .toList(),
                toReservaResponse(result.reserva()));
    }

    public DashboardMontagemResponse toResponse(DashboardMontagemResult result) {
        return new DashboardMontagemResponse(
                result.equipesIncompletas(),
                result.vagasAbertas(),
                result.sugestoesDisponiveis(),
                result.recusas(),
                toEncontroResponse(result.encontroCorrente()),
                result.equipes().stream().map(montagemResponseMapper::toResumoResponse).toList(),
                result.equipePrioritaria() == null
                        ? null
                        : montagemResponseMapper.toResumoResponse(result.equipePrioritaria()),
                result.sugestoes().stream().map(montagemResponseMapper::toCandidatoResponse).toList());
    }

    public DashboardComandoResponse toResponse(DashboardComandoResult result) {
        return new DashboardComandoResponse(
                result.convitesEnviados(),
                result.aceites(),
                result.pendentes(),
                result.recusas(),
                toEncontroResponse(result.encontroCorrente()),
                result.convitesRecentes().stream().map(conviteResponseMapper::toResponse).toList(),
                result.aceitesPorEquipe().stream()
                        .map(aceites -> new AceitesPorEquipeResponse(
                                aceites.equipeId(), aceites.equipeNome(), aceites.aceitos(), aceites.convidados()))
                        .toList());
    }

    public DashboardFinancasResponse toResponse(DashboardFinancasResult result) {
        return new DashboardFinancasResponse(
                toResumoResponse(result.resumo()),
                toEncontroResponse(result.encontroCorrente()),
                toCampanhasResponse(result.campanhas()),
                result.lancamentosRecentes().stream()
                        .map(lancamentoFinanceiroResponseMapper::toResponse)
                        .toList());
    }

    public DashboardPosEncontroResponse toResponse(DashboardPosEncontroResult result) {
        return new DashboardPosEncontroResponse(
                result.jovensAcompanhados(),
                result.presencaMedia(),
                result.encontrosPosNoMes(),
                result.prioridadeAlta(),
                result.frequenciaMensal().stream()
                        .map(contagem -> new ContagemMensalResponse(contagem.mes().format(MES), contagem.total()))
                        .toList(),
                result.proximoEncontroPos() == null
                        ? null
                        : posEncontroResponseMapper.toResponse(result.proximoEncontroPos()),
                result.chamadaRegistrada(),
                result.prioridades().stream().map(posEncontroResponseMapper::toResponse).toList());
    }

    private List<EquipeResumoResponse> toEquipesResponse(DashboardParoquiaResult result) {
        return result.equipes().stream().map(montagemResponseMapper::toResumoResponse).toList();
    }

    private List<CampanhaResponse> toCampanhasResponse(List<Campanha> campanhas) {
        return campanhas.stream().map(campanhaResponseMapper::toResponse).toList();
    }

    /** A ordem na fila é a posição na lista já ordenada por chegada (RN-INSCRICAO-04). */
    private List<CadastroReservaResponse> toReservaResponse(List<CadastroReserva> reserva) {
        return IntStream.range(0, reserva.size())
                .mapToObj(indice -> cadastroReservaResponseMapper.toResponse(reserva.get(indice), indice + 1))
                .toList();
    }

    private ResumoFinanceiroResponse toResumoResponse(ResumoFinanceiro resumo) {
        return new ResumoFinanceiroResponse(
                resumo.saldo(), resumo.totalReceitas(), resumo.totalDespesas(), resumo.campanhasAtivas());
    }

    private EncontroDoPainelResponse toEncontroResponse(Encontro encontro) {
        if (encontro == null) {
            return null;
        }
        return new EncontroDoPainelResponse(
                encontro.getId(),
                encontro.getNumero(),
                encontro.getPadroeiro(),
                encontro.getStatus().name(),
                encontro.getDataInicio(),
                encontro.getDataTermino(),
                encontro.getLocal(),
                encontro.getVagas(),
                encontro.getParoquia().getId(),
                encontro.getParoquia().getNome(),
                encontro.getDiretorEspiritual().getNomeCompleto());
    }
}
