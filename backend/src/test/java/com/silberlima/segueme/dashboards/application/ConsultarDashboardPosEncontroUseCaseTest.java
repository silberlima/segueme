package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.ContagemMensal;
import com.silberlima.segueme.dashboards.application.dto.DashboardPosEncontroResult;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.posencontro.application.ConsultarAcompanhadosUseCase;
import com.silberlima.segueme.posencontro.application.ListarPrioridadesUseCase;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.ContagemPresencasPorData;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FaixaPrioridade;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel Pós-Encontro (RF-053): presença média, contagem de prioridade Alta (RN-POS-03) e o
 * gráfico mensal com largura fixa de 6 meses.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardPosEncontroUseCaseTest {

    @Mock
    private ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase;

    @Mock
    private ListarPrioridadesUseCase listarPrioridadesUseCase;

    @Mock
    private EncontroPosRepository encontroPosRepository;

    @Mock
    private FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    @InjectMocks
    private ConsultarDashboardPosEncontroUseCase consultarDashboardPosEncontroUseCase;

    private final ReguaPrioridade regua = new ReguaPrioridade(75, 40);
    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));

    @Test
    void shouldApurarPresencaMediaEPrioridadeAlta() {
        UUID paroquiaId = UUID.randomUUID();
        when(consultarAcompanhadosUseCase.execute(paroquiaId))
                .thenReturn(List.of(umaPessoa("Ana"), umaPessoa("Bruno"), umaPessoa("Carla")));
        when(listarPrioridadesUseCase.execute(paroquiaId))
                .thenReturn(List.of(
                        FrequenciaPessoa.apurar(umaPessoa("Ana"), 10, 10, regua),
                        FrequenciaPessoa.apurar(umaPessoa("Bruno"), 10, 8, regua),
                        FrequenciaPessoa.apurar(umaPessoa("Carla"), 10, 3, regua)));
        when(encontroPosRepository.countByParoquia_IdAndDataBetweenAndDeletedFalse(any(), any(), any()))
                .thenReturn(2L);
        when(frequenciaPosEncontroRepository.contarPresencasPorData(any(), any(), any())).thenReturn(List.of());
        when(encontroPosRepository.findTopByParoquia_IdAndDataGreaterThanEqualAndDeletedFalseOrderByDataAsc(
                        any(), any()))
                .thenReturn(Optional.empty());

        DashboardPosEncontroResult painel = consultarDashboardPosEncontroUseCase.execute(paroquiaId);

        assertThat(painel.jovensAcompanhados()).isEqualTo(3);
        assertThat(painel.presencaMedia()).isEqualTo(70);
        assertThat(painel.prioridadeAlta()).isEqualTo(2);
        assertThat(painel.encontrosPosNoMes()).isEqualTo(2);
        assertThat(painel.prioridades())
                .extracting(FrequenciaPessoa::faixa)
                .containsExactly(FaixaPrioridade.ALTA, FaixaPrioridade.ALTA, FaixaPrioridade.BAIXA);
    }

    @Test
    void shouldMontarGraficoMensalComSeisMesesInclusiveOsVazios() {
        UUID paroquiaId = UUID.randomUUID();
        LocalDate hoje = LocalDate.now();
        semAcompanhados(paroquiaId);
        when(frequenciaPosEncontroRepository.contarPresencasPorData(any(), any(), any()))
                .thenReturn(List.of(new PresencasProjecao(hoje.withDayOfMonth(1), 9L)));
        when(encontroPosRepository.findTopByParoquia_IdAndDataGreaterThanEqualAndDeletedFalseOrderByDataAsc(
                        any(), any()))
                .thenReturn(Optional.empty());

        DashboardPosEncontroResult painel = consultarDashboardPosEncontroUseCase.execute(paroquiaId);

        assertThat(painel.frequenciaMensal()).hasSize(6);
        assertThat(painel.frequenciaMensal().getLast())
                .isEqualTo(new ContagemMensal(YearMonth.from(hoje), 9L));
        assertThat(painel.frequenciaMensal().getFirst().total()).isZero();
    }

    @Test
    void shouldExporAChamadaJaRegistradaDoProximoEncontroPos() {
        UUID paroquiaId = UUID.randomUUID();
        EncontroPos proximo = new EncontroPos(paroquia, LocalDate.now().plusDays(7), "Retiro");
        ReflectionTestUtils.setField(proximo, "id", UUID.randomUUID());
        semAcompanhados(paroquiaId);
        when(frequenciaPosEncontroRepository.contarPresencasPorData(any(), any(), any())).thenReturn(List.of());
        when(encontroPosRepository.findTopByParoquia_IdAndDataGreaterThanEqualAndDeletedFalseOrderByDataAsc(
                        any(), any()))
                .thenReturn(Optional.of(proximo));
        when(frequenciaPosEncontroRepository.countByEncontroPos_IdAndPresenteTrueAndDeletedFalse(proximo.getId()))
                .thenReturn(11L);

        DashboardPosEncontroResult painel = consultarDashboardPosEncontroUseCase.execute(paroquiaId);

        assertThat(painel.proximoEncontroPos()).isEqualTo(proximo);
        assertThat(painel.chamadaRegistrada()).isEqualTo(11);
    }

    @Test
    void shouldReturnPresencaMediaZeroSemAcompanhados() {
        UUID paroquiaId = UUID.randomUUID();
        semAcompanhados(paroquiaId);
        when(frequenciaPosEncontroRepository.contarPresencasPorData(any(), any(), any())).thenReturn(List.of());
        when(encontroPosRepository.findTopByParoquia_IdAndDataGreaterThanEqualAndDeletedFalseOrderByDataAsc(
                        any(), any()))
                .thenReturn(Optional.empty());

        DashboardPosEncontroResult painel = consultarDashboardPosEncontroUseCase.execute(paroquiaId);

        assertThat(painel.presencaMedia()).isZero();
        assertThat(painel.chamadaRegistrada()).isZero();
        assertThat(painel.prioridades()).isEmpty();
    }

    private void semAcompanhados(UUID paroquiaId) {
        when(consultarAcompanhadosUseCase.execute(paroquiaId)).thenReturn(List.of());
        when(listarPrioridadesUseCase.execute(paroquiaId)).thenReturn(List.of());
        when(encontroPosRepository.countByParoquia_IdAndDataBetweenAndDeletedFalse(any(), any(), any()))
                .thenReturn(0L);
    }

    private Pessoa umaPessoa(String nome) {
        return new Pessoa(
                nome,
                LocalDate.of(2005, 1, 1),
                "(11) 90000-0000",
                EstadoCivil.SOLTEIRO,
                paroquia,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private record PresencasProjecao(LocalDate data, long total) implements ContagemPresencasPorData {

        @Override
        public LocalDate getData() {
            return data;
        }

        @Override
        public Long getTotal() {
            return total;
        }
    }
}
