package com.silberlima.segueme.dashboards.application;

import com.silberlima.segueme.dashboards.application.dto.ContagemTerritorio;
import com.silberlima.segueme.dashboards.application.dto.DashboardDioceseResult;
import com.silberlima.segueme.dashboards.domain.PeriodoApuracao;
import com.silberlima.segueme.dashboards.domain.TotaisPorTerritorio;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.movimentacao.application.ListarSolicitacoesUseCase;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Painel da Diocese (`dash_diocese`, RF-053, RN-DASH-01), escopado à diocese do usuário (RN-04). */
@Service
public class ConsultarDashboardDioceseUseCase {

    private final PessoaRepository pessoaRepository;
    private final SetorRepository setorRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final EncontroRepository encontroRepository;
    private final ListarSolicitacoesUseCase listarSolicitacoesUseCase;

    public ConsultarDashboardDioceseUseCase(
            PessoaRepository pessoaRepository,
            SetorRepository setorRepository,
            ParoquiaRepository paroquiaRepository,
            EncontroRepository encontroRepository,
            ListarSolicitacoesUseCase listarSolicitacoesUseCase) {
        this.pessoaRepository = pessoaRepository;
        this.setorRepository = setorRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.encontroRepository = encontroRepository;
        this.listarSolicitacoesUseCase = listarSolicitacoesUseCase;
    }

    @Transactional(readOnly = true)
    public DashboardDioceseResult execute(UUID dioceseId) {
        PeriodoApuracao ano = PeriodoApuracao.anoDe(LocalDate.now());
        EscopoTerritorial escopo = new EscopoTerritorial.DeDiocese(dioceseId);

        return new DashboardDioceseResult(
                pessoaRepository.countByParoquia_Setor_Diocese_IdAndSituacaoAndDeletedFalse(
                        dioceseId, SituacaoPessoa.SEGUIDOR),
                setorRepository.countByDiocese_IdAndDeletedFalse(dioceseId),
                paroquiaRepository.countBySetor_Diocese_IdAndDeletedFalse(dioceseId),
                encontroRepository.countByParoquia_Setor_Diocese_IdAndDataInicioBetweenAndDeletedFalse(
                        dioceseId, ano.de(), ano.ate()),
                seguidoresPorSetor(dioceseId),
                listarSolicitacoesUseCase.execute(escopo).size());
    }

    private List<ContagemTerritorio> seguidoresPorSetor(UUID dioceseId) {
        TotaisPorTerritorio seguidores = TotaisPorTerritorio.de(
                pessoaRepository.contarPorSetorDaDiocese(dioceseId, SituacaoPessoa.SEGUIDOR));

        return setorRepository.findByDiocese_IdAndDeletedFalseOrderByNomeAsc(dioceseId).stream()
                .map(setor -> new ContagemTerritorio(setor.getId(), setor.getNome(), seguidores.total(setor.getId())))
                .toList();
    }
}
