package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.ContagemTerritorio;
import com.silberlima.segueme.dashboards.application.dto.DashboardDioceseResult;
import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.hierarquia.domain.SetorRepository;
import com.silberlima.segueme.movimentacao.application.ListarSolicitacoesUseCase;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.application.dto.TipoSolicitacao;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Cobre o Painel da Diocese (RF-053): KPIs da diocese, seguidores por setor e solicitações em aberto. */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardDioceseUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private SetorRepository setorRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private ListarSolicitacoesUseCase listarSolicitacoesUseCase;

    @InjectMocks
    private ConsultarDashboardDioceseUseCase consultarDashboardDioceseUseCase;

    @Test
    void shouldConsolidarOsKpisDaDiocese() {
        UUID dioceseId = UUID.randomUUID();
        when(pessoaRepository.countByParoquia_Setor_Diocese_IdAndSituacaoAndDeletedFalse(
                        dioceseId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(420L);
        when(setorRepository.countByDiocese_IdAndDeletedFalse(dioceseId)).thenReturn(4L);
        when(paroquiaRepository.countBySetor_Diocese_IdAndDeletedFalse(dioceseId)).thenReturn(19L);
        when(encontroRepository.countByParoquia_Setor_Diocese_IdAndDataInicioBetweenAndDeletedFalse(
                        any(), any(), any()))
                .thenReturn(6L);
        when(pessoaRepository.contarPorSetorDaDiocese(any(), any())).thenReturn(List.of());
        when(setorRepository.findByDiocese_IdAndDeletedFalseOrderByNomeAsc(dioceseId)).thenReturn(List.of());
        when(listarSolicitacoesUseCase.execute(any())).thenReturn(List.of());

        DashboardDioceseResult painel = consultarDashboardDioceseUseCase.execute(dioceseId);

        assertThat(painel.seguidores()).isEqualTo(420);
        assertThat(painel.setores()).isEqualTo(4);
        assertThat(painel.paroquias()).isEqualTo(19);
        assertThat(painel.encontrosNoAno()).isEqualTo(6);
    }

    @Test
    void shouldIncluirSetorSemSeguidorNoGrafico() {
        UUID dioceseId = UUID.randomUUID();
        Diocese diocese = new Diocese("Diocese Central");
        Setor comSeguidores = umSetor("Setor 1", diocese);
        Setor vazio = umSetor("Setor 2", diocese);
        kpisZerados(dioceseId);
        when(setorRepository.findByDiocese_IdAndDeletedFalseOrderByNomeAsc(dioceseId))
                .thenReturn(List.of(comSeguidores, vazio));
        when(pessoaRepository.contarPorSetorDaDiocese(dioceseId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(List.of(new ContagemProjecao(comSeguidores.getId(), 30L)));
        when(listarSolicitacoesUseCase.execute(any())).thenReturn(List.of());

        DashboardDioceseResult painel = consultarDashboardDioceseUseCase.execute(dioceseId);

        assertThat(painel.seguidoresPorSetor())
                .containsExactly(
                        new ContagemTerritorio(comSeguidores.getId(), "Setor 1", 30),
                        new ContagemTerritorio(vazio.getId(), "Setor 2", 0));
    }

    @Test
    void shouldContarSolicitacoesEmAbertoNoEscopoDaDiocese() {
        UUID dioceseId = UUID.randomUUID();
        kpisZerados(dioceseId);
        when(setorRepository.findByDiocese_IdAndDeletedFalseOrderByNomeAsc(dioceseId)).thenReturn(List.of());
        when(pessoaRepository.contarPorSetorDaDiocese(any(), any())).thenReturn(List.of());
        when(listarSolicitacoesUseCase.execute(any()))
                .thenReturn(List.of(umaSolicitacao(), umaSolicitacao(), umaSolicitacao()));

        DashboardDioceseResult painel = consultarDashboardDioceseUseCase.execute(dioceseId);

        ArgumentCaptor<EscopoTerritorial> escopo = ArgumentCaptor.forClass(EscopoTerritorial.class);
        verify(listarSolicitacoesUseCase).execute(escopo.capture());
        assertThat(escopo.getValue()).isEqualTo(new EscopoTerritorial.DeDiocese(dioceseId));
        assertThat(painel.solicitacoesAoSetorial()).isEqualTo(3);
    }

    private void kpisZerados(UUID dioceseId) {
        when(pessoaRepository.countByParoquia_Setor_Diocese_IdAndSituacaoAndDeletedFalse(any(), any()))
                .thenReturn(0L);
        when(setorRepository.countByDiocese_IdAndDeletedFalse(dioceseId)).thenReturn(0L);
        when(paroquiaRepository.countBySetor_Diocese_IdAndDeletedFalse(dioceseId)).thenReturn(0L);
        when(encontroRepository.countByParoquia_Setor_Diocese_IdAndDataInicioBetweenAndDeletedFalse(
                        any(), any(), any()))
                .thenReturn(0L);
    }

    private Setor umSetor(String nome, Diocese diocese) {
        Setor setor = new Setor(nome, diocese);
        ReflectionTestUtils.setField(setor, "id", UUID.randomUUID());
        return setor;
    }

    private SolicitacaoItem umaSolicitacao() {
        return new SolicitacaoItem(TipoSolicitacao.TRANSFERENCIA, null, null, Instant.now());
    }

    private record ContagemProjecao(UUID territorioId, long total) implements ContagemPorTerritorio {

        @Override
        public UUID getTerritorioId() {
            return territorioId;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }
}
