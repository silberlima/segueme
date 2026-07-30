package com.silberlima.segueme.dashboards.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.dashboards.application.dto.DashboardSetorialResult;
import com.silberlima.segueme.dashboards.application.dto.ParoquiaDoSetor;
import com.silberlima.segueme.domain.model.ContagemPorTerritorio;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.ProximoEncontroDaParoquia;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.movimentacao.application.ListarSolicitacoesUseCase;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.application.dto.TipoSolicitacao;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

/**
 * Cobre o Painel do Setor (RF-053): KPIs de movimentação, recorte da lista de solicitações e a lista
 * de paróquias com o próximo Encontro de cada uma.
 */
@ExtendWith(MockitoExtension.class)
class ConsultarDashboardSetorialUseCaseTest {

    @Mock
    private ListarSolicitacoesUseCase listarSolicitacoesUseCase;

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroRepository encontroRepository;

    @InjectMocks
    private ConsultarDashboardSetorialUseCase consultarDashboardSetorialUseCase;

    @Test
    void shouldConsolidarOsKpisDeMovimentacaoDoSetor() {
        UUID setorId = UUID.randomUUID();
        when(listarSolicitacoesUseCase.execute(any())).thenReturn(List.of(umaSolicitacao(), umaSolicitacao()));
        when(transferenciaRepository.count(anySpecTransferencia())).thenReturn(7L);
        when(emprestimoRepository.count(anySpecEmprestimo())).thenReturn(3L);
        when(pessoaRepository.countByParoquia_Setor_IdAndSituacaoAndDeletedFalse(setorId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(88L);
        semParoquias(setorId);

        DashboardSetorialResult painel = consultarDashboardSetorialUseCase.execute(setorId);

        assertThat(painel.solicitacoesPendentes()).isEqualTo(2);
        assertThat(painel.transferenciasNoMes()).isEqualTo(7);
        assertThat(painel.emprestimosAtivos()).isEqualTo(3);
        assertThat(painel.seguidores()).isEqualTo(88);
    }

    @Test
    void shouldExibirApenasAsCincoSolicitacoesMaisRecentes() {
        UUID setorId = UUID.randomUUID();
        List<SolicitacaoItem> oito =
                IntStream.range(0, 8).mapToObj(indice -> umaSolicitacao()).toList();
        when(listarSolicitacoesUseCase.execute(any())).thenReturn(oito);
        kpisZerados(setorId);
        semParoquias(setorId);

        DashboardSetorialResult painel = consultarDashboardSetorialUseCase.execute(setorId);

        assertThat(painel.solicitacoesPendentes()).isEqualTo(8);
        assertThat(painel.solicitacoes()).hasSize(5);
    }

    @Test
    void shouldListarParoquiaSemEncontroComProximoEncontroNulo() {
        UUID setorId = UUID.randomUUID();
        Setor setor = new Setor("Setor 1", new Diocese("Diocese Central"));
        Paroquia comEncontro = umaParoquia("Paróquia São José", setor);
        Paroquia semEncontro = umaParoquia("Paróquia Santa Rita", setor);
        when(listarSolicitacoesUseCase.execute(any())).thenReturn(List.of());
        kpisZerados(setorId);
        when(paroquiaRepository.findBySetor_IdAndDeletedFalseOrderByNomeAsc(setorId))
                .thenReturn(List.of(comEncontro, semEncontro));
        when(pessoaRepository.contarPorParoquiaDoSetor(setorId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(List.of(new ContagemProjecao(comEncontro.getId(), 12L)));
        when(encontroRepository.proximoEncontroPorParoquiaDoSetor(any(), any()))
                .thenReturn(List.of(new ProximoEncontroProjecao(comEncontro.getId(), LocalDate.of(2026, 9, 4))));

        DashboardSetorialResult painel = consultarDashboardSetorialUseCase.execute(setorId);

        assertThat(painel.paroquias())
                .containsExactly(
                        new ParoquiaDoSetor(comEncontro.getId(), "Paróquia São José", 12, LocalDate.of(2026, 9, 4)),
                        new ParoquiaDoSetor(semEncontro.getId(), "Paróquia Santa Rita", 0, null));
    }

    private void kpisZerados(UUID setorId) {
        when(transferenciaRepository.count(anySpecTransferencia())).thenReturn(0L);
        when(emprestimoRepository.count(anySpecEmprestimo())).thenReturn(0L);
        when(pessoaRepository.countByParoquia_Setor_IdAndSituacaoAndDeletedFalse(setorId, SituacaoPessoa.SEGUIDOR))
                .thenReturn(0L);
    }

    private void semParoquias(UUID setorId) {
        when(paroquiaRepository.findBySetor_IdAndDeletedFalseOrderByNomeAsc(setorId)).thenReturn(List.of());
        when(pessoaRepository.contarPorParoquiaDoSetor(any(), any())).thenReturn(List.of());
        when(encontroRepository.proximoEncontroPorParoquiaDoSetor(any(), any())).thenReturn(List.of());
    }

    private Specification<Transferencia> anySpecTransferencia() {
        return any();
    }

    private Specification<Emprestimo> anySpecEmprestimo() {
        return any();
    }

    private Paroquia umaParoquia(String nome, Setor setor) {
        Paroquia paroquia = new Paroquia(nome, setor);
        ReflectionTestUtils.setField(paroquia, "id", UUID.randomUUID());
        return paroquia;
    }

    private SolicitacaoItem umaSolicitacao() {
        return new SolicitacaoItem(TipoSolicitacao.EMPRESTIMO, null, null, Instant.now());
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

    private record ProximoEncontroProjecao(UUID territorioId, LocalDate data)
            implements ProximoEncontroDaParoquia {

        @Override
        public UUID getTerritorioId() {
            return territorioId;
        }

        @Override
        public LocalDate getData() {
            return data;
        }
    }
}
