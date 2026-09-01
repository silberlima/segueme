package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.DashboardMontagemResult;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.application.GerarSugestoesUseCase;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.MatchScore;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
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
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel de Montagem (RF-053): KPIs de vaga, escolha da equipe prioritária e recorte de até
 * 3 sugestões exibidas (RN-MONTAGEM-02/04 — a decisão segue humana).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardMontagemUseCaseTest {

    @Mock
    private EncontroCorrenteResolver encontroCorrenteResolver;

    @Mock
    private ListarEquipesUseCase listarEquipesUseCase;

    @Mock
    private GerarSugestoesUseCase gerarSugestoesUseCase;

    @InjectMocks
    private ConsultarDashboardMontagemUseCase consultarDashboardMontagemUseCase;

    private final Paroquia paroquia = new Paroquia("Paróquia São José", new Setor("Setor 1", new Diocese("Diocese")));

    @Test
    void shouldEscolherAEquipeComMaisVagasAbertasComoPrioritaria() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        EquipeComposicao quaseCompleta = umaEquipe("Equipe Cozinha", 2, 4, 3);
        EquipeComposicao maisVazia = umaEquipe("Equipe Vigília", 2, 6, 1);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(listarEquipesUseCase.execute(encontro.getId())).thenReturn(List.of(quaseCompleta, maisVazia));
        when(gerarSugestoesUseCase.execute(encontro.getId(), maisVazia.equipe().getId()))
                .thenReturn(List.of());

        DashboardMontagemResult painel = consultarDashboardMontagemUseCase.execute(paroquiaId);

        assertThat(painel.equipePrioritaria()).isEqualTo(maisVazia);
        assertThat(painel.vagasAbertas()).isEqualTo(6);
        assertThat(painel.equipesIncompletas()).isEqualTo(1);
    }

    @Test
    void shouldExibirNoMaximoTresSugestoesMasContarTodasAsDisponiveis() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        EquipeComposicao equipe = umaEquipe("Equipe Vigília", 2, 6, 0);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(listarEquipesUseCase.execute(encontro.getId())).thenReturn(List.of(equipe));
        when(gerarSugestoesUseCase.execute(any(), any()))
                .thenReturn(IntStream.range(0, 7)
                        .mapToObj(indice -> umCandidato("Candidato " + indice))
                        .toList());

        DashboardMontagemResult painel = consultarDashboardMontagemUseCase.execute(paroquiaId);

        assertThat(painel.sugestoesDisponiveis()).isEqualTo(7);
        assertThat(painel.sugestoes()).hasSize(3);
    }

    @Test
    void shouldNaoGerarSugestoesQuandoNenhumaEquipeTemVagaAberta() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        EquipeComposicao completa = umaEquipe("Equipe Cozinha", 2, 3, 3);
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.of(encontro));
        when(listarEquipesUseCase.execute(encontro.getId())).thenReturn(List.of(completa));

        DashboardMontagemResult painel = consultarDashboardMontagemUseCase.execute(paroquiaId);

        assertThat(painel.equipePrioritaria()).isNull();
        assertThat(painel.sugestoes()).isEmpty();
        assertThat(painel.sugestoesDisponiveis()).isZero();
        verify(gerarSugestoesUseCase, never()).execute(any(), any());
    }

    @Test
    void shouldReturnPainelZeradoQuandoNaoHaEncontro() {
        when(encontroCorrenteResolver.resolver(any(), any())).thenReturn(Optional.empty());

        DashboardMontagemResult painel = consultarDashboardMontagemUseCase.execute(UUID.randomUUID());

        assertThat(painel.encontroCorrente()).isNull();
        assertThat(painel.equipes()).isEmpty();
        verify(listarEquipesUseCase, never()).execute(any());
    }

    private EquipeComposicao umaEquipe(String nome, int minimo, int maximo, int integrantes) {
        Equipe equipe = new Equipe(nome, minimo, maximo, null);
        ReflectionTestUtils.setField(equipe, "id", UUID.randomUUID());
        Encontro encontro = umEncontro();
        List<IntegranteEquipe> membros = IntStream.range(0, integrantes)
                .mapToObj(indice -> new IntegranteEquipe(
                        encontro, equipe, umaPessoa("Integrante " + indice), PapelIntegrante.INTEGRANTE))
                .toList();
        return new EquipeComposicao(equipe, membros);
    }

    private CandidatoSugerido umCandidato(String nome) {
        return new CandidatoSugerido(umaPessoa(nome), new MatchScore(80, 100, 100, 50, 100, "v1"), true, List.of());
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
        Encontro encontro = new Encontro(
                12,
                "São José",
                umaPessoa("Padre Diretor"),
                "Casa de Retiros",
                LocalDate.now().plusDays(20),
                LocalDate.now().plusDays(23),
                60,
                paroquia);
        ReflectionTestUtils.setField(encontro, "id", UUID.randomUUID());
        return encontro;
    }
}
