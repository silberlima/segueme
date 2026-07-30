package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.DashboardFichasResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.inscricoes.application.ConsultarFunilUseCase;
import com.silberlima.segueme.inscricoes.application.ListarCadastroReservaUseCase;
import com.silberlima.segueme.inscricoes.application.ListarPreInscricoesUseCase;
import com.silberlima.segueme.inscricoes.application.dto.FunilInscricao;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel de Fichas (`dash_fichas`, RF-053, RN-DASH-01): o funil de inscrição do Encontro corrente da
 * paróquia, as pré-inscrições que aguardam ficha e a fila de reserva por prioridade
 * (RN-INSCRICAO-04). Reaproveita {@code ConsultarFunilUseCase} para não recalcular o funil aqui.
 */
@Service
public class ConsultarDashboardFichasUseCase {

    private static final int MAXIMO_LISTAS = 5;

    private final EncontroCorrenteResolver encontroCorrenteResolver;
    private final ConsultarFunilUseCase consultarFunilUseCase;
    private final ListarPreInscricoesUseCase listarPreInscricoesUseCase;
    private final ListarCadastroReservaUseCase listarCadastroReservaUseCase;
    private final PreInscricaoRepository preInscricaoRepository;
    private final PessoaRepository pessoaRepository;

    public ConsultarDashboardFichasUseCase(
            EncontroCorrenteResolver encontroCorrenteResolver,
            ConsultarFunilUseCase consultarFunilUseCase,
            ListarPreInscricoesUseCase listarPreInscricoesUseCase,
            ListarCadastroReservaUseCase listarCadastroReservaUseCase,
            PreInscricaoRepository preInscricaoRepository,
            PessoaRepository pessoaRepository) {
        this.encontroCorrenteResolver = encontroCorrenteResolver;
        this.consultarFunilUseCase = consultarFunilUseCase;
        this.listarPreInscricoesUseCase = listarPreInscricoesUseCase;
        this.listarCadastroReservaUseCase = listarCadastroReservaUseCase;
        this.preInscricaoRepository = preInscricaoRepository;
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public DashboardFichasResult execute(UUID paroquiaId) {
        long seguimistas =
                pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIMISTA);
        long seguidores =
                pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIDOR);

        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, LocalDate.now());
        if (corrente.isEmpty()) {
            return new DashboardFichasResult(
                    0, 0, 0, 0, 0, seguimistas, seguidores, null, List.of(), List.of());
        }

        Encontro encontro = corrente.get();
        FunilInscricao funil = consultarFunilUseCase.execute(encontro.getId());

        return new DashboardFichasResult(
                funil.preInscritos(),
                funil.emAnalise(),
                funil.confirmados(),
                vagasRestantes(encontro),
                funil.emReserva(),
                seguimistas,
                seguidores,
                encontro,
                aguardandoFicha(paroquiaId, encontro.getId()),
                reserva(encontro.getId()));
    }

    /** Toda pré-inscrição ocupa vaga, independente do status — não há estado de cancelamento (RN-INSCRICAO-01). */
    private long vagasRestantes(Encontro encontro) {
        long ocupadas = preInscricaoRepository.countByEncontro_Id(encontro.getId());
        return Math.max(0, encontro.getVagas() - ocupadas);
    }

    private List<PreInscricao> aguardandoFicha(UUID paroquiaId, UUID encontroId) {
        return listarPreInscricoesUseCase
                .execute(
                        new EscopoTerritorial.DeParoquia(paroquiaId),
                        encontroId,
                        StatusPreInscricao.AGUARDANDO_FICHA,
                        null,
                        PageRequest.of(0, MAXIMO_LISTAS, Sort.by(Sort.Direction.ASC, "createdAt")))
                .getContent();
    }

    private List<CadastroReserva> reserva(UUID encontroId) {
        return listarCadastroReservaUseCase.execute(encontroId).stream()
                .limit(MAXIMO_LISTAS)
                .toList();
    }
}
