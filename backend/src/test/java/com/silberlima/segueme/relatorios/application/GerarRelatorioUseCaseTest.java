package com.silberlima.segueme.relatorios.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.StatusRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobre a geração assíncrona: o desfecho é sempre gravado (concluído ou falhou) e o solicitante é
 * avisado pela central de notificações.
 */
@ExtendWith(MockitoExtension.class)
class GerarRelatorioUseCaseTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private PublicarNotificacaoUseCase publicarNotificacaoUseCase;

    private final UUID relatorioId = UUID.randomUUID();

    @Test
    void shouldConcluirComOConteudoDoGerador() {
        Relatorio relatorio = umRelatorio();
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        umUseCaseCom(geradorQueDevolve(new ConteudoRelatorio(List.of("Jovem"), List.of(List.of("Ana")))))
                .execute(relatorioId);

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.CONCLUIDO);
        assertThat(relatorio.getTotalLinhas()).isEqualTo(1);
        verify(relatorioRepository).save(relatorio);
    }

    @Test
    void shouldNotificarOSolicitanteQuandoOArquivoFicaPronto() {
        Relatorio relatorio = umRelatorio();
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        umUseCaseCom(geradorQueDevolve(ConteudoRelatorio.vazio(List.of("Jovem")))).execute(relatorioId);

        verify(publicarNotificacaoUseCase)
                .execute(eq("user-a"), eq(TipoNotificacao.POSITIVA), contains("Relatório pronto"), any());
    }

    @Test
    void shouldRegistrarFalhaQuandoAGeracaoLancaExcecao() {
        Relatorio relatorio = umRelatorio();
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        umUseCaseCom(geradorQueFalha("A paróquia ainda não tem nenhum Encontro para relatar."))
                .execute(relatorioId);

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.FALHOU);
        assertThat(relatorio.getErro()).contains("nenhum Encontro");
        verify(relatorioRepository).save(relatorio);
    }

    @Test
    void shouldNotificarOSolicitanteQuandoAGeracaoFalha() {
        Relatorio relatorio = umRelatorio();
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        umUseCaseCom(geradorQueFalha("sem Encontro")).execute(relatorioId);

        verify(publicarNotificacaoUseCase)
                .execute(eq("user-a"), eq(TipoNotificacao.NEGATIVA), contains("não gerado"), any());
    }

    @Test
    void shouldRegistrarFalhaQuandoNaoHaGeradorParaOTipo() {
        Relatorio relatorio = umRelatorio();
        when(relatorioRepository.findByIdAndDeletedFalse(relatorioId)).thenReturn(Optional.of(relatorio));

        umUseCaseCom(List.of()).execute(relatorioId);

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.FALHOU);
        assertThat(relatorio.getErro()).contains("Nenhum gerador registrado");
    }

    private GerarRelatorioUseCase umUseCaseCom(GeradorDeRelatorio gerador) {
        return umUseCaseCom(List.of(gerador));
    }

    private GerarRelatorioUseCase umUseCaseCom(List<GeradorDeRelatorio> geradores) {
        return new GerarRelatorioUseCase(relatorioRepository, publicarNotificacaoUseCase, geradores);
    }

    private Relatorio umRelatorio() {
        return new Relatorio(
                TipoRelatorio.POS_ENCONTRO, "user-a", new EscopoTerritorial.DeParoquia(UUID.randomUUID()));
    }

    private GeradorDeRelatorio geradorQueDevolve(ConteudoRelatorio conteudo) {
        return new GeradorDeRelatorio() {
            @Override
            public TipoRelatorio suporta() {
                return TipoRelatorio.POS_ENCONTRO;
            }

            @Override
            public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
                return conteudo;
            }
        };
    }

    private GeradorDeRelatorio geradorQueFalha(String motivo) {
        return new GeradorDeRelatorio() {
            @Override
            public TipoRelatorio suporta() {
                return TipoRelatorio.POS_ENCONTRO;
            }

            @Override
            public ConteudoRelatorio gerar(EscopoTerritorial escopo) {
                throw new BusinessRuleViolationException(motivo);
            }
        };
    }
}
