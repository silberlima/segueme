package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.StatusPreInscricao;
import com.silberlima.segueme.inscricoes.domain.StatusReserva;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel da Paróquia (RF-053): estado sem Encontro criado, pendências somadas (RN-DASH-02) e
 * contagem de dias até o próximo Encontro.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardParoquiaUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EncontroCorrenteResolver encontroCorrenteResolver;

    @Mock
    private ListarEquipesUseCase listarEquipesUseCase;

    @Mock
    private FichaInscricaoRepository fichaInscricaoRepository;

    @Mock
    private PreInscricaoRepository preInscricaoRepository;

    @Mock
    private CadastroReservaRepository cadastroReservaRepository;

    @Mock
    private ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;

    @Mock
    private ListarCampanhasUseCase listarCampanhasUseCase;

    @InjectMocks
    private ConsultarDashboardParoquiaUseCase consultarDashboardParoquiaUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));

    @Test
    void shouldReturnPainelZeradoQuandoAParoquiaNuncaCriouEncontro() {
        UUID paroquiaId = UUID.randomUUID();
        when(pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(15L);
        when(encontroRepository.countByParoquia_IdAndStatusAndDeletedFalse(paroquiaId, StatusEncontro.ENCERRADO))
                .thenReturn(0L);
        when(encontroRepository.findByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(any(), any()))
                .thenReturn(List.of());
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.empty());

        DashboardParoquiaResult painel = consultarDashboardParoquiaUseCase.execute(paroquiaId);

        assertThat(painel.seguidoresAtivos()).isEqualTo(15);
        assertThat(painel.encontroCorrente()).isNull();
        assertThat(painel.diasParaProximoEncontro()).isNull();
        assertThat(painel.totalPendencias()).isZero();
        assertThat(painel.financeiro().saldo()).isEqualByComparingTo(BigDecimal.ZERO);
        verify(listarEquipesUseCase, never()).execute(any());
        verify(obterResumoFinanceiroUseCase, never()).execute(any());
    }

    @Test
    void shouldSomarAsTresPendenciasDoPainel() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(LocalDate.now().plusDays(30));
        cenarioComEncontro(paroquiaId, encontro);
        when(listarEquipesUseCase.execute(encontro.getId()))
                .thenReturn(List.of(umaEquipeIncompleta(), umaEquipeIncompleta()));
        when(cadastroReservaRepository.countByEncontro_Paroquia_IdAndStatus(paroquiaId, StatusReserva.AGUARDANDO))
                .thenReturn(4L);
        when(preInscricaoRepository.countByEncontro_IdAndStatus(
                        encontro.getId(), StatusPreInscricao.AGUARDANDO_FICHA))
                .thenReturn(3L);

        DashboardParoquiaResult painel = consultarDashboardParoquiaUseCase.execute(paroquiaId);

        assertThat(painel.pendencias())
                .containsExactly(
                        new Pendencia(TipoPendencia.EQUIPES_INCOMPLETAS, 2),
                        new Pendencia(TipoPendencia.CADASTRO_RESERVA, 4),
                        new Pendencia(TipoPendencia.FICHAS_A_PREENCHER, 3));
        assertThat(painel.totalPendencias()).isEqualTo(9);
    }

    @Test
    void shouldContarOsDiasAteOProximoEncontro() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(LocalDate.now().plusDays(12));
        cenarioComEncontro(paroquiaId, encontro);
        semPendencias(paroquiaId, encontro);

        DashboardParoquiaResult painel = consultarDashboardParoquiaUseCase.execute(paroquiaId);

        assertThat(painel.diasParaProximoEncontro()).isEqualTo(12);
    }

    @Test
    void shouldNaoContarDiasQuandoOEncontroJaComecou() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(LocalDate.now().minusDays(1));
        cenarioComEncontro(paroquiaId, encontro);
        semPendencias(paroquiaId, encontro);

        DashboardParoquiaResult painel = consultarDashboardParoquiaUseCase.execute(paroquiaId);

        assertThat(painel.diasParaProximoEncontro()).isNull();
        assertThat(painel.encontroCorrente()).isEqualTo(encontro);
    }

    private void cenarioComEncontro(UUID paroquiaId, Encontro encontro) {
        when(pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(0L);
        when(encontroRepository.countByParoquia_IdAndStatusAndDeletedFalse(paroquiaId, StatusEncontro.ENCERRADO))
                .thenReturn(0L);
        when(encontroRepository.findByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(any(), any()))
                .thenReturn(List.of(encontro));
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(fichaInscricaoRepository.countByEncontro_Id(encontro.getId())).thenReturn(0L);
        when(obterResumoFinanceiroUseCase.execute(encontro.getId()))
                .thenReturn(new ResumoFinanceiro(BigDecimal.TEN, BigDecimal.TEN, BigDecimal.ZERO, 1));
        when(listarCampanhasUseCase.execute(any(), any(), any(), any())).thenReturn(new PageImpl<>(List.of()));
    }

    private void semPendencias(UUID paroquiaId, Encontro encontro) {
        when(listarEquipesUseCase.execute(encontro.getId())).thenReturn(List.of());
        when(cadastroReservaRepository.countByEncontro_Paroquia_IdAndStatus(paroquiaId, StatusReserva.AGUARDANDO))
                .thenReturn(0L);
        when(preInscricaoRepository.countByEncontro_IdAndStatus(
                        encontro.getId(), StatusPreInscricao.AGUARDANDO_FICHA))
                .thenReturn(0L);
    }

    private EquipeComposicao umaEquipeIncompleta() {
        Equipe equipe = new Equipe("Equipe Vigília", 4, 6, null);
        ReflectionTestUtils.setField(equipe, "id", UUID.randomUUID());
        return new EquipeComposicao(equipe, List.of());
    }

    private Encontro umEncontro(LocalDate dataInicio) {
        Pessoa diretor = new Pessoa(
                "Padre Diretor",
                LocalDate.of(1970, 1, 1),
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
        Encontro encontro = new Encontro(
                12, "São José", diretor, "Casa de Retiros", dataInicio, dataInicio.plusDays(3), 60, paroquia);
        ReflectionTestUtils.setField(encontro, "id", UUID.randomUUID());
        return encontro;
    }
}
