package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.DashboardFichasResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.inscricoes.application.ConsultarFunilUseCase;
import com.silberlima.segueme.inscricoes.application.ListarCadastroReservaUseCase;
import com.silberlima.segueme.inscricoes.application.ListarPreInscricoesUseCase;
import com.silberlima.segueme.inscricoes.application.dto.FunilInscricao;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel de Fichas (RF-053): funil reaproveitado, vagas restantes nunca negativas e as duas
 * etapas do funil que vêm da situação da pessoa (seguimistas/seguidores).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardFichasUseCaseTest {

    @Mock
    private EncontroCorrenteResolver encontroCorrenteResolver;

    @Mock
    private ConsultarFunilUseCase consultarFunilUseCase;

    @Mock
    private ListarPreInscricoesUseCase listarPreInscricoesUseCase;

    @Mock
    private ListarCadastroReservaUseCase listarCadastroReservaUseCase;

    @Mock
    private PreInscricaoRepository preInscricaoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @InjectMocks
    private ConsultarDashboardFichasUseCase consultarDashboardFichasUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));

    @Test
    void shouldExporOFunilDoEncontroCorrente() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(60);
        situacoesDaParoquia(paroquiaId, 9, 40);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(consultarFunilUseCase.execute(encontro.getId())).thenReturn(new FunilInscricao(7, 3, 25, 5, 0));
        when(preInscricaoRepository.countByEncontro_Id(encontro.getId())).thenReturn(35L);
        when(listarPreInscricoesUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(listarCadastroReservaUseCase.execute(encontro.getId())).thenReturn(List.of());

        DashboardFichasResult painel = consultarDashboardFichasUseCase.execute(paroquiaId);

        assertThat(painel.aguardandoFicha()).isEqualTo(7);
        assertThat(painel.emAnalise()).isEqualTo(3);
        assertThat(painel.confirmados()).isEqualTo(25);
        assertThat(painel.cadastroReserva()).isEqualTo(5);
        assertThat(painel.vagasRestantes()).isEqualTo(25);
        assertThat(painel.seguimistas()).isEqualTo(9);
        assertThat(painel.seguidores()).isEqualTo(40);
    }

    @Test
    void shouldNaoDeixarVagasRestantesNegativas() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(20);
        situacoesDaParoquia(paroquiaId, 0, 0);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(consultarFunilUseCase.execute(encontro.getId())).thenReturn(new FunilInscricao(0, 0, 20, 6, 0));
        when(preInscricaoRepository.countByEncontro_Id(encontro.getId())).thenReturn(26L);
        when(listarPreInscricoesUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(listarCadastroReservaUseCase.execute(encontro.getId())).thenReturn(List.of());

        DashboardFichasResult painel = consultarDashboardFichasUseCase.execute(paroquiaId);

        assertThat(painel.vagasRestantes()).isZero();
    }

    @Test
    void shouldExibirApenasOsCincoPrimeirosDaFilaDeReserva() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro(60);
        situacoesDaParoquia(paroquiaId, 0, 0);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(consultarFunilUseCase.execute(encontro.getId())).thenReturn(new FunilInscricao(0, 0, 0, 8, 0));
        when(preInscricaoRepository.countByEncontro_Id(encontro.getId())).thenReturn(0L);
        when(listarPreInscricoesUseCase.execute(any(), any(), any(), any(), any()))
                .thenReturn(new PageImpl<>(List.of()));
        when(listarCadastroReservaUseCase.execute(encontro.getId()))
                .thenReturn(IntStream.range(0, 8)
                        .mapToObj(indice -> new CadastroReserva(umaPessoa("Candidato " + indice), encontro))
                        .toList());

        DashboardFichasResult painel = consultarDashboardFichasUseCase.execute(paroquiaId);

        assertThat(painel.reserva()).hasSize(5);
    }

    @Test
    void shouldReturnPainelZeradoQuandoNaoHaEncontro() {
        UUID paroquiaId = UUID.randomUUID();
        situacoesDaParoquia(paroquiaId, 2, 3);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.empty());

        DashboardFichasResult painel = consultarDashboardFichasUseCase.execute(paroquiaId);

        assertThat(painel.encontroCorrente()).isNull();
        assertThat(painel.aguardandoFicha()).isZero();
        assertThat(painel.seguimistas()).isEqualTo(2);
        assertThat(painel.seguidores()).isEqualTo(3);
        verify(consultarFunilUseCase, never()).execute(any());
    }

    private void situacoesDaParoquia(UUID paroquiaId, long seguimistas, long seguidores) {
        when(pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIMISTA))
                .thenReturn(seguimistas);
        when(pessoaRepository.countByParoquia_IdAndSituacaoAndDeletedFalse(paroquiaId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(seguidores);
    }

    private Pessoa umaPessoa(String nome) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2005, 1, 1),
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
    }

    private Encontro umEncontro(int vagas) {
        Encontro encontro = new Encontro(
                12,
                "São José",
                umaPessoa("Padre Diretor"),
                "Casa de Retiros",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(23),
                vagas,
                paroquia);
        ReflectionTestUtils.setField(encontro, "id", UUID.randomUUID());
        return encontro;
    }
}
