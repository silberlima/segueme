package com.silberlima.segueme.relatorios.application.geradores;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.posencontro.application.ListarPrioridadesUseCase;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
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
 * Cobre o relatório de pós-encontro: sai da mesma apuração da tela Prioridades, na mesma ordem
 * (RF-059, RN-POS-02/03) — relatório e tela não podem divergir.
 */
@ExtendWith(MockitoExtension.class)
class GeradorDeRelatorioDePosEncontroTest {

    @Mock
    private EncontroDoRelatorioResolver encontroDoRelatorioResolver;

    @Mock
    private ListarPrioridadesUseCase listarPrioridadesUseCase;

    @InjectMocks
    private GeradorDeRelatorioDePosEncontro geradorDeRelatorioDePosEncontro;

    private final ReguaPrioridade regua = new ReguaPrioridade(75, 40);
    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));

    @Test
    void shouldGerarUmaLinhaPorJovemAcompanhado() {
        UUID paroquiaId = UUID.randomUUID();
        EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(paroquiaId);
        when(encontroDoRelatorioResolver.paroquiaDoEscopo(escopo)).thenReturn(paroquiaId);
        when(listarPrioridadesUseCase.execute(paroquiaId))
                .thenReturn(List.of(
                        FrequenciaPessoa.apurar(umaPessoa("Ana Assídua"), 4, 4, regua),
                        FrequenciaPessoa.apurar(umaPessoa("Bruno Faltante"), 4, 0, regua)));

        ConteudoRelatorio conteudo = geradorDeRelatorioDePosEncontro.gerar(escopo);

        assertThat(conteudo.totalLinhas()).isEqualTo(2);
        assertThat(conteudo.linhas().getFirst()).containsExactly("Ana Assídua", "4", "4", "100", "ALTA");
        assertThat(conteudo.linhas().get(1)).containsExactly("Bruno Faltante", "4", "0", "0", "BAIXA");
    }

    @Test
    void shouldGerarSoOCabecalhoQuandoNaoHaAcompanhados() {
        UUID paroquiaId = UUID.randomUUID();
        EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(paroquiaId);
        when(encontroDoRelatorioResolver.paroquiaDoEscopo(escopo)).thenReturn(paroquiaId);
        when(listarPrioridadesUseCase.execute(paroquiaId)).thenReturn(List.of());

        ConteudoRelatorio conteudo = geradorDeRelatorioDePosEncontro.gerar(escopo);

        assertThat(conteudo.totalLinhas()).isZero();
        assertThat(conteudo.colunas()).startsWith("Jovem");
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
