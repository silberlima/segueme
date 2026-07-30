package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.ContagemRegiao;
import com.silberlima.segueme.dashboards.application.dto.DashboardNacionalResult;
import com.silberlima.segueme.dashboards.application.dto.DioceseDestaque;
import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import com.silberlima.segueme.encontros.domain.ContagemPorRegiao;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.DioceseRepository;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.RegiaoBrasil;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/** Cobre o Painel Nacional (RF-053): KPIs, gráfico por região e ranking de dioceses em destaque. */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardNacionalUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private DioceseRepository dioceseRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroRepository encontroRepository;

    @InjectMocks
    private ConsultarDashboardNacionalUseCase consultarDashboardNacionalUseCase;

    @Test
    void shouldConsolidarOsQuatroKpisNacionais() {
        when(pessoaRepository.countBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR)).thenReturn(1200L);
        when(dioceseRepository.countByDeletedFalse()).thenReturn(18L);
        when(paroquiaRepository.countByDeletedFalse()).thenReturn(240L);
        when(encontroRepository.countByDataInicioBetweenAndDeletedFalse(any(), any())).thenReturn(37L);
        semAgrupamentos();

        DashboardNacionalResult painel = consultarDashboardNacionalUseCase.execute();

        assertThat(painel.seguidores()).isEqualTo(1200);
        assertThat(painel.dioceses()).isEqualTo(18);
        assertThat(painel.paroquias()).isEqualTo(240);
        assertThat(painel.encontrosNoAno()).isEqualTo(37);
    }

    @Test
    void shouldExibirAsCincoRegioesMesmoZeradas() {
        kpisZerados();
        when(encontroRepository.contarPorRegiao(any(), any()))
                .thenReturn(List.of(new ContagemRegiaoProjecao(RegiaoBrasil.SUDESTE, 12L)));
        when(paroquiaRepository.contarPorDiocese()).thenReturn(List.of());
        when(pessoaRepository.contarPorDiocese(any())).thenReturn(List.of());
        when(encontroRepository.contarPorDiocese(any(), any())).thenReturn(List.of());
        when(dioceseRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of());

        DashboardNacionalResult painel = consultarDashboardNacionalUseCase.execute();

        assertThat(painel.encontrosPorRegiao())
                .extracting(ContagemRegiao::regiao)
                .containsExactly(
                        RegiaoBrasil.NORDESTE,
                        RegiaoBrasil.SUDESTE,
                        RegiaoBrasil.SUL,
                        RegiaoBrasil.CENTRO_OESTE,
                        RegiaoBrasil.NORTE);
        assertThat(painel.encontrosPorRegiao())
                .filteredOn(contagem -> contagem.regiao() == RegiaoBrasil.SUDESTE)
                .extracting(ContagemRegiao::total)
                .containsExactly(12L);
    }

    @Test
    void shouldAgruparEncontrosDeDioceseSemRegiaoNumaFaixaPropria() {
        kpisZerados();
        when(encontroRepository.contarPorRegiao(any(), any()))
                .thenReturn(List.of(new ContagemRegiaoProjecao(null, 4L)));
        when(paroquiaRepository.contarPorDiocese()).thenReturn(List.of());
        when(pessoaRepository.contarPorDiocese(any())).thenReturn(List.of());
        when(encontroRepository.contarPorDiocese(any(), any())).thenReturn(List.of());
        when(dioceseRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of());

        DashboardNacionalResult painel = consultarDashboardNacionalUseCase.execute();

        assertThat(painel.encontrosPorRegiao()).hasSize(6);
        assertThat(painel.encontrosPorRegiao().getLast()).isEqualTo(new ContagemRegiao(null, 4L));
    }

    @Test
    void shouldRanquearDiocesesPorSeguidores() {
        kpisZerados();
        when(encontroRepository.contarPorRegiao(any(), any())).thenReturn(List.of());

        Diocese maior = umaDiocese("Diocese Central");
        Diocese menor = umaDiocese("Diocese Norte");
        when(dioceseRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of(maior, menor));
        when(paroquiaRepository.contarPorDiocese())
                .thenReturn(List.of(
                        new ContagemProjecao(maior.getId(), 10L), new ContagemProjecao(menor.getId(), 3L)));
        when(pessoaRepository.contarPorDiocese(eq(SituacaoPessoa.SEGUIDOR)))
                .thenReturn(List.of(
                        new ContagemProjecao(maior.getId(), 300L), new ContagemProjecao(menor.getId(), 40L)));
        when(encontroRepository.contarPorDiocese(any(), any()))
                .thenReturn(List.of(new ContagemProjecao(maior.getId(), 5L)));

        DashboardNacionalResult painel = consultarDashboardNacionalUseCase.execute();

        assertThat(painel.diocesesDestaque())
                .extracting(DioceseDestaque::nome)
                .containsExactly("Diocese Central", "Diocese Norte");
        assertThat(painel.diocesesDestaque().getFirst())
                .isEqualTo(new DioceseDestaque(maior.getId(), "Diocese Central", 10, 300, 5));
        // Diocese sem Encontro no ano entra no ranking com 0, não fica de fora.
        assertThat(painel.diocesesDestaque().getLast().encontrosNoAno()).isZero();
    }

    private void kpisZerados() {
        when(pessoaRepository.countBySituacaoAndDeletedFalse(any())).thenReturn(0L);
        when(dioceseRepository.countByDeletedFalse()).thenReturn(0L);
        when(paroquiaRepository.countByDeletedFalse()).thenReturn(0L);
        when(encontroRepository.countByDataInicioBetweenAndDeletedFalse(any(), any())).thenReturn(0L);
    }

    private void semAgrupamentos() {
        when(encontroRepository.contarPorRegiao(any(), any())).thenReturn(List.of());
        when(paroquiaRepository.contarPorDiocese()).thenReturn(List.of());
        when(pessoaRepository.contarPorDiocese(any())).thenReturn(List.of());
        when(encontroRepository.contarPorDiocese(any(), any())).thenReturn(List.of());
        when(dioceseRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of());
    }

    private Diocese umaDiocese(String nome) {
        Diocese diocese = new Diocese(nome, RegiaoBrasil.SUDESTE);
        ReflectionTestUtils.setField(diocese, "id", UUID.randomUUID());
        return diocese;
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

    private record ContagemRegiaoProjecao(RegiaoBrasil regiao, long total) implements ContagemPorRegiao {

        @Override
        public RegiaoBrasil getRegiao() {
            return regiao;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }

    @Test
    void shouldNaoQuebrarSemNenhumDado() {
        kpisZerados();
        semAgrupamentos();

        DashboardNacionalResult painel = consultarDashboardNacionalUseCase.execute();

        assertThat(painel.diocesesDestaque()).isEmpty();
        assertThat(painel.encontrosPorRegiao()).hasSize(RegiaoBrasil.values().length);
    }
}
