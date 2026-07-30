package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.DashboardParoquiaResult;
import com.silberlima.segueme.dashboards.application.dto.Pendencia;
import com.silberlima.segueme.dashboards.domain.TipoPendencia;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.StatusEncontro;
import com.silberlima.segueme.financeiro.application.ListarCampanhasUseCase;
import com.silberlima.segueme.financeiro.application.ObterResumoFinanceiroUseCase;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import com.silberlima.segueme.inscricoes.domain.StatusReserva;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel da Paróquia (`dash_paroquia`, RF-053, RN-DASH-01): o cartão do Encontro corrente com
 * progresso e checklist de equipes, os Encontros recentes, as pendências (que também viram badge no
 * menu, RN-DASH-02) e o resumo financeiro.
 *
 * <p>Paróquia sem nenhum Encontro criado devolve o painel zerado, sem erro: o protótipo prevê a ação
 * "Novo encontro" justamente nesse estado.
 */
@Service
public class ConsultarDashboardParoquiaUseCase {

    private static final int MAXIMO_ENCONTROS_RECENTES = 5;
    private static final int MAXIMO_CAMPANHAS = 5;
    private static final ResumoFinanceiro FINANCEIRO_VAZIO =
            new ResumoFinanceiro(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0);

    private final PessoaRepository pessoaRepository;
    private final EncontroRepository encontroRepository;
    private final EncontroCorrenteResolver encontroCorrenteResolver;
    private final ListarEquipesUseCase listarEquipesUseCase;
    private final FichaInscricaoRepository fichaInscricaoRepository;
    private final PreInscricaoRepository preInscricaoRepository;
    private final CadastroReservaRepository cadastroReservaRepository;
    private final ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;
    private final ListarCampanhasUseCase listarCampanhasUseCase;

    public ConsultarDashboardParoquiaUseCase(
            PessoaRepository pessoaRepository,
            EncontroRepository encontroRepository,
            EncontroCorrenteResolver encontroCorrenteResolver,
            ListarEquipesUseCase listarEquipesUseCase,
            FichaInscricaoRepository fichaInscricaoRepository,
            PreInscricaoRepository preInscricaoRepository,
            CadastroReservaRepository cadastroReservaRepository,
            ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase,
            ListarCampanhasUseCase listarCampanhasUseCase) {
        this.pessoaRepository = pessoaRepository;
        this.encontroRepository = encontroRepository;
        this.encontroCorrenteResolver = encontroCorrenteResolver;
        this.listarEquipesUseCase = listarEquipesUseCase;
        this.fichaInscricaoRepository = fichaInscricaoRepository;
        this.preInscricaoRepository = preInscricaoRepository;
        this.cadastroReservaRepository = cadastroReservaRepository;
        this.obterResumoFinanceiroUseCase = obterResumoFinanceiroUseCase;
        this.listarCampanhasUseCase = listarCampanhasUseCase;
    }

    @Transactional(readOnly = true)
    public DashboardParoquiaResult execute(UUID paroquiaId) {
        LocalDate hoje = LocalDate.now();
        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, hoje);

        long seguidoresAtivos =
                pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIDOR);
        long encontrosRealizados =
                encontroRepository.countByParoquia_IdAndStatusAndDeletedFalse(paroquiaId, StatusEncontro.ENCERRADO);
        List<Encontro> recentes = encontroRepository.findByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(
                paroquiaId, PageRequest.of(0, MAXIMO_ENCONTROS_RECENTES));

        if (corrente.isEmpty()) {
            return new DashboardParoquiaResult(
                    seguidoresAtivos,
                    encontrosRealizados,
                    null,
                    0,
                    null,
                    0,
                    List.of(),
                    recentes,
                    List.of(),
                    FINANCEIRO_VAZIO,
                    List.of());
        }

        Encontro encontro = corrente.get();
        List<EquipeComposicao> equipes = listarEquipesUseCase.execute(encontro.getId());
        List<Pendencia> pendencias = pendencias(encontro, paroquiaId, equipes);

        return new DashboardParoquiaResult(
                seguidoresAtivos,
                encontrosRealizados,
                diasParaProximoEncontro(encontro, hoje),
                pendencias.stream().mapToLong(Pendencia::quantidade).sum(),
                encontro,
                fichaInscricaoRepository.countByEncontro_Id(encontro.getId()),
                equipes,
                recentes,
                pendencias,
                obterResumoFinanceiroUseCase.execute(encontro.getId()),
                campanhas(paroquiaId, encontro.getId()));
    }

    /** Nulo quando o Encontro corrente já começou — o cartão passa a mostrar "em andamento", não a contagem. */
    private Integer diasParaProximoEncontro(Encontro encontro, LocalDate hoje) {
        if (encontro.getDataInicio().isBefore(hoje)) {
            return null;
        }
        return (int) ChronoUnit.DAYS.between(hoje, encontro.getDataInicio());
    }

    private List<Pendencia> pendencias(Encontro encontro, UUID paroquiaId, List<EquipeComposicao> equipes) {
        long equipesIncompletas = equipes.stream().filter(equipe -> !equipe.completa()).count();
        long reserva =
                cadastroReservaRepository.countByEncontro_Paroquia_IdAndStatus(paroquiaId, StatusReserva.AGUARDANDO);
        long fichasAPreencher = preInscricaoRepository.countByEncontro_IdAndStatus(
                encontro.getId(), StatusPreInscricao.AGUARDANDO_FICHA);

        return List.of(
                new Pendencia(TipoPendencia.EQUIPES_INCOMPLETAS, equipesIncompletas),
                new Pendencia(TipoPendencia.CADASTRO_RESERVA, reserva),
                new Pendencia(TipoPendencia.FICHAS_A_PREENCHER, fichasAPreencher));
    }

    private List<Campanha> campanhas(UUID paroquiaId, UUID encontroId) {
        return listarCampanhasUseCase
                .execute(
                        new EscopoTerritorial.DeParoquia(paroquiaId),
                        encontroId,
                        null,
                        PageRequest.of(0, MAXIMO_CAMPANHAS, Sort.by(Sort.Direction.DESC, "createdAt")))
                .getContent();
    }
}
