package com.silberlima.segueme.posencontro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.posencontro.domain.ContagemEncontrosPos;
import com.silberlima.segueme.posencontro.domain.ContagemPresencas;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Cobre a porta que a Fase 6 consome (fator de 20% do score de match, `plano-implementacao.md` seção 19). */
@ExtendWith(MockitoExtension.class)
class ConsultarPercentualFrequenciaUseCaseTest {

    @Mock
    private EncontroPosRepository encontroPosRepository;

    @Mock
    private FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    @InjectMocks
    private ConsultarPercentualFrequenciaUseCase consultarPercentualFrequenciaUseCase;

    @Test
    void shouldApurarPercentualComDenominadorDaParoquiaDoEncontroPos() {
        UUID paroquiaA = UUID.randomUUID();
        UUID paroquiaB = UUID.randomUUID();
        UUID assiduo = UUID.randomUUID();
        UUID esporadico = UUID.randomUUID();

        when(encontroPosRepository.contarPorParoquia(any(), any()))
                .thenReturn(List.of(umaContagemDeEncontros(paroquiaA, 10L), umaContagemDeEncontros(paroquiaB, 4L)));
        when(frequenciaPosEncontroRepository.contarPresencas(any(), any()))
                .thenReturn(List.of(umaContagemDePresencas(assiduo, paroquiaA, 9L),
                        umaContagemDePresencas(esporadico, paroquiaB, 1L)));

        Map<UUID, Integer> percentuais = consultarPercentualFrequenciaUseCase.execute();

        assertThat(percentuais).containsExactlyInAnyOrderEntriesOf(Map.of(assiduo, 90, esporadico, 25));
    }

    @Test
    void shouldIgnorarPessoaSemDenominadorConhecido() {
        UUID pessoaId = UUID.randomUUID();

        when(encontroPosRepository.contarPorParoquia(any(), any())).thenReturn(List.of());
        when(frequenciaPosEncontroRepository.contarPresencas(any(), any()))
                .thenReturn(List.of(umaContagemDePresencas(pessoaId, UUID.randomUUID(), 3L)));

        assertThat(consultarPercentualFrequenciaUseCase.execute()).isEmpty();
    }

    private ContagemEncontrosPos umaContagemDeEncontros(UUID paroquiaId, long total) {
        return new ContagemEncontrosPos() {
            @Override
            public UUID getParoquiaId() {
                return paroquiaId;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    private ContagemPresencas umaContagemDePresencas(UUID pessoaId, UUID paroquiaId, long presencas) {
        return new ContagemPresencas() {
            @Override
            public UUID getPessoaId() {
                return pessoaId;
            }

            @Override
            public UUID getParoquiaId() {
                return paroquiaId;
            }

            @Override
            public Long getPresencas() {
                return presencas;
            }
        };
    }
}
