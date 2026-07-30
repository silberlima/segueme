package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.DashboardSetorialResult;
import com.silberlima.segueme.dashboards.application.dto.ParoquiaDoSetor;
import com.silberlima.segueme.dashboards.domain.PeriodoApuracao;
import com.silberlima.segueme.dashboards.domain.TotaisPorTerritorio;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.ProximoEncontroDaParoquia;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.movimentacao.application.ListarSolicitacoesUseCase;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoSpecification;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.movimentacao.domain.TransferenciaSpecification;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Painel do Setor (`dash_setorial`, RF-053, RN-DASH-01). As solicitações exibidas são as mesmas do
 * inbox {@code screen_solicitacoes}: o painel reaproveita {@code ListarSolicitacoesUseCase} para que
 * a decisão de aprovar/recusar continue sendo uma única ação central (`dashboards-por-perfil.md` 3.3).
 */
@Service
public class ConsultarDashboardSetorialUseCase {

    private static final int MAXIMO_SOLICITACOES_EXIBIDAS = 5;

    private final ListarSolicitacoesUseCase listarSolicitacoesUseCase;
    private final TransferenciaRepository transferenciaRepository;
    private final EmprestimoRepository emprestimoRepository;
    private final PessoaRepository pessoaRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final EncontroRepository encontroRepository;

    public ConsultarDashboardSetorialUseCase(
            ListarSolicitacoesUseCase listarSolicitacoesUseCase,
            TransferenciaRepository transferenciaRepository,
            EmprestimoRepository emprestimoRepository,
            PessoaRepository pessoaRepository,
            ParoquiaRepository paroquiaRepository,
            EncontroRepository encontroRepository) {
        this.listarSolicitacoesUseCase = listarSolicitacoesUseCase;
        this.transferenciaRepository = transferenciaRepository;
        this.emprestimoRepository = emprestimoRepository;
        this.pessoaRepository = pessoaRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.encontroRepository = encontroRepository;
    }

    @Transactional(readOnly = true)
    public DashboardSetorialResult execute(UUID setorId) {
        LocalDate hoje = LocalDate.now();
        EscopoTerritorial escopo = new EscopoTerritorial.DeSetor(setorId);
        List<SolicitacaoItem> solicitacoes = listarSolicitacoesUseCase.execute(escopo);

        return new DashboardSetorialResult(
                solicitacoes.size(),
                transferenciasNoMes(escopo, hoje),
                emprestimosAtivos(escopo),
                pessoaRepository.countByParoquia_Setor_IdAndSituacaoAndDeletedFalse(setorId, SituacaoPessoa.SEGUIDOR),
                solicitacoes.stream().limit(MAXIMO_SOLICITACOES_EXIBIDAS).toList(),
                paroquiasDoSetor(setorId, hoje));
    }

    private long transferenciasNoMes(EscopoTerritorial escopo, LocalDate hoje) {
        PeriodoApuracao mes = PeriodoApuracao.mesDe(hoje);
        Specification<Transferencia> spec = Specification.allOf(
                TransferenciaSpecification.naoDeletada(),
                TransferenciaSpecification.comEscopo(escopo),
                TransferenciaSpecification.solicitadaDesde(
                        mes.de().atStartOfDay(ZoneId.systemDefault()).toInstant()));
        return transferenciaRepository.count(spec);
    }

    private long emprestimosAtivos(EscopoTerritorial escopo) {
        Specification<Emprestimo> spec = Specification.allOf(
                EmprestimoSpecification.naoDeletado(),
                EmprestimoSpecification.comEscopo(escopo),
                EmprestimoSpecification.comStatus(StatusEmprestimo.EFETIVADO));
        return emprestimoRepository.count(spec);
    }

    private List<ParoquiaDoSetor> paroquiasDoSetor(UUID setorId, LocalDate hoje) {
        TotaisPorTerritorio seguidores =
                TotaisPorTerritorio.de(pessoaRepository.contarPorParoquiaDoSetor(setorId, SituacaoPessoa.SEGUIDOR));
        Map<UUID, LocalDate> proximoEncontro =
                encontroRepository.proximoEncontroPorParoquiaDoSetor(setorId, hoje).stream()
                        .collect(Collectors.toMap(
                                ProximoEncontroDaParoquia::getTerritorioId,
                                ProximoEncontroDaParoquia::getData,
                                (primeira, segunda) -> primeira));

        return paroquiaRepository.findBySetor_IdAndDeletedFalseOrderByNomeAsc(setorId).stream()
                .map(paroquia -> new ParoquiaDoSetor(
                        paroquia.getId(),
                        paroquia.getNome(),
                        seguidores.total(paroquia.getId()),
                        proximoEncontro.get(paroquia.getId())))
                .toList();
    }
}
