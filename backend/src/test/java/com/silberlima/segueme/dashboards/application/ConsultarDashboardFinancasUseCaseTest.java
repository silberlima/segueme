package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.DashboardFinancasResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.financeiro.application.ListarCampanhasUseCase;
import com.silberlima.segueme.financeiro.application.ListarLancamentosUseCase;
import com.silberlima.segueme.financeiro.application.ObterResumoFinanceiroUseCase;
import com.silberlima.segueme.financeiro.application.dto.ResumoFinanceiro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel Financeiro (RF-053): saldo vindo do mesmo use case da tela de financeiro
 * (RN-FINANCEIRO-04) e escopo de paróquia aplicado às listas.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardFinancasUseCaseTest {

    @Mock
    private EncontroCorrenteResolver encontroCorrenteResolver;

    @Mock
    private ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;

    @Mock
    private ListarCampanhasUseCase listarCampanhasUseCase;

    @Mock
    private ListarLancamentosUseCase listarLancamentosUseCase;

    @InjectMocks
    private ConsultarDashboardFinancasUseCase consultarDashboardFinancasUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));

    @Test
    void shouldExporOSaldoDoEncontroCorrente() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        Campanha campanha = new Campanha(encontro, "Rifa", new BigDecimal("2000"));
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(obterResumoFinanceiroUseCase.execute(encontro.getId()))
                .thenReturn(new ResumoFinanceiro(
                        new BigDecimal("1500"), new BigDecimal("4000"), new BigDecimal("2500"), 2));
        when(listarCampanhasUseCase.execute(any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of(campanha)));
        when(listarLancamentosUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        DashboardFinancasResult painel = consultarDashboardFinancasUseCase.execute(paroquiaId);

        assertThat(painel.resumo().saldo()).isEqualByComparingTo("1500");
        assertThat(painel.resumo().campanhasAtivas()).isEqualTo(2);
        assertThat(painel.campanhas()).containsExactly(campanha);
        assertThat(painel.encontroCorrente()).isEqualTo(encontro);
    }

    @Test
    void shouldEscoparAsListasNaParoquiaDoUsuario() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(obterResumoFinanceiroUseCase.execute(encontro.getId()))
                .thenReturn(new ResumoFinanceiro(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, 0));
        when(listarCampanhasUseCase.execute(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
        when(listarLancamentosUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));

        consultarDashboardFinancasUseCase.execute(paroquiaId);

        ArgumentCaptor<EscopoTerritorial> escopo = ArgumentCaptor.forClass(EscopoTerritorial.class);
        verify(listarLancamentosUseCase).execute(escopo.capture(), any(), any(), any(), any());
        assertThat(escopo.getValue()).isEqualTo(new EscopoTerritorial.DeParoquia(paroquiaId));
    }

    @Test
    void shouldReturnPainelZeradoQuandoNaoHaEncontro() {
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.empty());

        DashboardFinancasResult painel = consultarDashboardFinancasUseCase.execute(UUID.randomUUID());

        assertThat(painel.resumo().saldo()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(painel.encontroCorrente()).isNull();
        assertThat(painel.lancamentosRecentes()).isEmpty();
        verify(obterResumoFinanceiroUseCase, never()).execute(any());
    }

    private Encontro umEncontro() {
        Pessoa diretor = new Pessoa(
                "Padre Diretor",
                PessoasDeTeste.cpf(),
                LocalDate.of(1970, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
        Encontro encontro = new Encontro(
                12,
                "São José",
                diretor,
                "Casa de Retiros",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(23),
                60,
                paroquia);
        ReflectionTestUtils.setField(encontro, "id", UUID.randomUUID());
        return encontro;
    }
}
