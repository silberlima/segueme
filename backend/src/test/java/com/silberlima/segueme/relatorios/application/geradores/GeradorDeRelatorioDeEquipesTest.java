package com.silberlima.segueme.relatorios.application.geradores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.application.ListarEquipesUseCase;
import com.silberlima.segueme.montagem.application.dto.EquipeComposicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobre o relatório de equipes: uma linha por integrante e, na equipe ainda vazia, a linha que
 * mostra as vagas em aberto — é justamente o que o relatório existe para revelar.
 */
@ExtendWith(MockitoExtension.class)
class GeradorDeRelatorioDeEquipesTest {

    @Mock
    private EncontroDoRelatorioResolver encontroDoRelatorioResolver;

    @Mock
    private ListarEquipesUseCase listarEquipesUseCase;

    @InjectMocks
    private GeradorDeRelatorioDeEquipes geradorDeRelatorioDeEquipes;

    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));
    private final EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(UUID.randomUUID());

    @Test
    void shouldGerarUmaLinhaPorIntegrante() {
        Encontro encontro = umEncontro();
        Equipe cozinha = new Equipe("Cozinha", 2, 4, null);
        when(encontroDoRelatorioResolver.encontroCorrente(escopo)).thenReturn(encontro);
        when(listarEquipesUseCase.execute(any())).thenReturn(List.of(new EquipeComposicao(
                cozinha,
                List.of(
                        new IntegranteEquipe(encontro, cozinha, umaPessoa("Ana"), PapelIntegrante.COORDENADOR),
                        new IntegranteEquipe(encontro, cozinha, umaPessoa("Bruno"), PapelIntegrante.INTEGRANTE)))));

        ConteudoRelatorio conteudo = geradorDeRelatorioDeEquipes.gerar(escopo);

        assertThat(conteudo.totalLinhas()).isEqualTo(2);
        assertThat(conteudo.linhas().getFirst())
                .startsWith("Cozinha", "2", "Ana", "COORDENADOR", "PENDENTE");
    }

    @Test
    void shouldMostrarAEquipeVaziaComSuasVagasEmAberto() {
        when(encontroDoRelatorioResolver.encontroCorrente(escopo)).thenReturn(umEncontro());
        when(listarEquipesUseCase.execute(any()))
                .thenReturn(List.of(new EquipeComposicao(new Equipe("Liturgia", 2, 5, null), List.of())));

        ConteudoRelatorio conteudo = geradorDeRelatorioDeEquipes.gerar(escopo);

        assertThat(conteudo.linhas()).hasSize(1);
        assertThat(conteudo.linhas().getFirst()).startsWith("Liturgia", "5", "(sem integrantes)");
    }

    private Encontro umEncontro() {
        return new Encontro(
                1,
                "São José",
                null,
                "Salão paroquial",
                LocalDate.of(2026, 8, 1),
                LocalDate.of(2026, 8, 3),
                40,
                paroquia);
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
}
