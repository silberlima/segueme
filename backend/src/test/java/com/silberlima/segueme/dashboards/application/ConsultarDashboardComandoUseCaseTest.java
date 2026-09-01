package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.convites.application.ListarConvitesUseCase;
import com.silberlima.segueme.dashboards.application.dto.AceitesPorEquipe;
import com.silberlima.segueme.dashboards.application.dto.DashboardComandoResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
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
 * Cobre o Painel de Comando (RF-053): os quatro KPIs de convite e o gráfico de aceites por equipe,
 * que ignora recusados porque a vaga volta a ficar aberta (RN-MONTAGEM-06).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardComandoUseCaseTest {

    @Mock
    private EncontroCorrenteResolver encontroCorrenteResolver;

    @Mock
    private ListarConvitesUseCase listarConvitesUseCase;

    @InjectMocks
    private ConsultarDashboardComandoUseCase consultarDashboardComandoUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));
    private final Encontro encontro = umEncontro();

    @Test
    void shouldContarConvitesPorStatus() {
        Equipe cozinha = umaEquipe("Equipe Cozinha");
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(listarConvitesUseCase.execute(any(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(
                        umConvite(cozinha, "Ana", true),
                        umConvite(cozinha, "Bruno", false),
                        umConviteRecusado(cozinha, "Carla")));

        DashboardComandoResult painel = consultarDashboardComandoUseCase.execute(UUID.randomUUID());

        assertThat(painel.convitesEnviados()).isEqualTo(3);
        assertThat(painel.aceites()).isEqualTo(1);
        assertThat(painel.pendentes()).isEqualTo(1);
        assertThat(painel.recusas()).isEqualTo(1);
    }

    @Test
    void shouldAgruparAceitesPorEquipeIgnorandoRecusados() {
        Equipe cozinha = umaEquipe("Equipe Cozinha");
        Equipe vigilia = umaEquipe("Equipe Vigília");
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(listarConvitesUseCase.execute(any(), isNull(), isNull(), isNull()))
                .thenReturn(List.of(
                        umConvite(vigilia, "Ana", true),
                        umConvite(cozinha, "Bruno", true),
                        umConvite(cozinha, "Carla", false),
                        umConviteRecusado(cozinha, "Davi")));

        DashboardComandoResult painel = consultarDashboardComandoUseCase.execute(UUID.randomUUID());

        assertThat(painel.aceitesPorEquipe())
                .containsExactly(
                        new AceitesPorEquipe(cozinha.getId(), "Equipe Cozinha", 1, 2),
                        new AceitesPorEquipe(vigilia.getId(), "Equipe Vigília", 1, 1));
    }

    @Test
    void shouldReturnPainelZeradoQuandoNaoHaEncontro() {
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.empty());

        DashboardComandoResult painel = consultarDashboardComandoUseCase.execute(UUID.randomUUID());

        assertThat(painel.convitesEnviados()).isZero();
        assertThat(painel.encontroCorrente()).isNull();
        assertThat(painel.aceitesPorEquipe()).isEmpty();
    }

    private IntegranteEquipe umConvite(Equipe equipe, String nome, boolean aceito) {
        IntegranteEquipe convite =
                new IntegranteEquipe(encontro, equipe, umaPessoa(nome), PapelIntegrante.INTEGRANTE);
        if (aceito) {
            convite.aceitar();
        }
        return convite;
    }

    private IntegranteEquipe umConviteRecusado(Equipe equipe, String nome) {
        IntegranteEquipe convite =
                new IntegranteEquipe(encontro, equipe, umaPessoa(nome), PapelIntegrante.INTEGRANTE);
        convite.recusar("Indisponível");
        return convite;
    }

    private Equipe umaEquipe(String nome) {
        Equipe equipe = new Equipe(nome, 2, 6, null);
        ReflectionTestUtils.setField(equipe, "id", UUID.randomUUID());
        return equipe;
    }

    private Pessoa umaPessoa(String nome) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
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

    private Encontro umEncontro() {
        Encontro novo = new Encontro(
                12,
                "São José",
                umaPessoa("Padre Diretor"),
                "Casa de Retiros",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(23),
                60,
                paroquia);
        ReflectionTestUtils.setField(novo, "id", UUID.randomUUID());
        return novo;
    }
}
