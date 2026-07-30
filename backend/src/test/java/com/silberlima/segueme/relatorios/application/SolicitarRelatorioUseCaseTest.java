package com.silberlima.segueme.relatorios.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.RelatorioSolicitadoEvent;
import com.silberlima.segueme.relatorios.domain.StatusRelatorio;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

/** Cobre o pedido de relatório (UC-22): autorização por perfil e disparo da geração assíncrona. */
@ExtendWith(MockitoExtension.class)
class SolicitarRelatorioUseCaseTest {

    @Mock
    private RelatorioRepository relatorioRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private SolicitarRelatorioUseCase solicitarRelatorioUseCase;

    private final EscopoTerritorial escopo = new EscopoTerritorial.DeParoquia(UUID.randomUUID());

    @Test
    void shouldRegistrarOPedidoComoPendente() {
        when(relatorioRepository.save(any(Relatorio.class))).thenAnswer(chamada -> chamada.getArgument(0));

        Relatorio relatorio = solicitarRelatorioUseCase.execute(
                TipoRelatorio.POS_ENCONTRO, "user-a", List.of(Perfil.POS_ENCONTRO), escopo);

        assertThat(relatorio.getStatus()).isEqualTo(StatusRelatorio.PENDENTE);
        assertThat(relatorio.getTipo()).isEqualTo(TipoRelatorio.POS_ENCONTRO);
        assertThat(relatorio.getSolicitanteId()).isEqualTo("user-a");
    }

    @Test
    void shouldPublicarOEventoQueDisparaAGeracao() {
        when(relatorioRepository.save(any(Relatorio.class))).thenAnswer(chamada -> chamada.getArgument(0));

        solicitarRelatorioUseCase.execute(
                TipoRelatorio.POS_ENCONTRO, "user-a", List.of(Perfil.POS_ENCONTRO), escopo);

        verify(eventPublisher).publishEvent(any(RelatorioSolicitadoEvent.class));
    }

    @Test
    void shouldThrowExceptionWhenPerfilNaoPodeGerarOTipo() {
        assertThatThrownBy(() -> solicitarRelatorioUseCase.execute(
                        TipoRelatorio.FINANCEIRO, "user-a", List.of(Perfil.POS_ENCONTRO), escopo))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("não pode gerar");

        verify(relatorioRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any(RelatorioSolicitadoEvent.class));
    }
}
