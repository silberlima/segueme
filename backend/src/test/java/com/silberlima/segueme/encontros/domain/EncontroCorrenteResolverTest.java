package com.silberlima.segueme.encontros.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * Cobre o Encontro "corrente" que os painéis das equipes exibem: o próximo em aberto e, na falta
 * dele, o último realizado.
 */
@ExtendWith(MockitoExtension.class)
class EncontroCorrenteResolverTest {

    private static final LocalDate HOJE = LocalDate.of(2026, 7, 29);

    @Mock
    private EncontroRepository encontroRepository;

    @InjectMocks
    private EncontroCorrenteResolver encontroCorrenteResolver;

    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));

    @Test
    void shouldPreferirOProximoEncontroEmAberto() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro proximo = umEncontro(LocalDate.of(2026, 9, 1));
        when(encontroRepository
                        .findTopByParoquia_IdAndStatusAndDataTerminoGreaterThanEqualAndDeletedFalseOrderByDataInicioAsc(
                                eq(paroquiaId), eq(StatusEncontro.ATIVO), eq(HOJE)))
                .thenReturn(Optional.of(proximo));

        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, HOJE);

        assertThat(corrente).contains(proximo);
        verify(encontroRepository, never()).findTopByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(any());
    }

    @Test
    void shouldCairNoUltimoRealizadoQuandoNaoHaEncontroEmAberto() {
        UUID paroquiaId = UUID.randomUUID();
        Encontro ultimo = umEncontro(LocalDate.of(2026, 3, 1));
        when(encontroRepository
                        .findTopByParoquia_IdAndStatusAndDataTerminoGreaterThanEqualAndDeletedFalseOrderByDataInicioAsc(
                                eq(paroquiaId), eq(StatusEncontro.ATIVO), eq(HOJE)))
                .thenReturn(Optional.empty());
        when(encontroRepository.findTopByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(paroquiaId))
                .thenReturn(Optional.of(ultimo));

        Optional<Encontro> corrente = encontroCorrenteResolver.resolver(paroquiaId, HOJE);

        assertThat(corrente).contains(ultimo);
    }

    @Test
    void shouldReturnVazioQuandoAParoquiaNuncaTeveEncontro() {
        UUID paroquiaId = UUID.randomUUID();
        when(encontroRepository
                        .findTopByParoquia_IdAndStatusAndDataTerminoGreaterThanEqualAndDeletedFalseOrderByDataInicioAsc(
                                eq(paroquiaId), eq(StatusEncontro.ATIVO), eq(HOJE)))
                .thenReturn(Optional.empty());
        when(encontroRepository.findTopByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(paroquiaId))
                .thenReturn(Optional.empty());

        assertThat(encontroCorrenteResolver.resolver(paroquiaId, HOJE)).isEmpty();
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
        return new Encontro(
                1, "São José", diretor, "Casa de Retiros", dataInicio, dataInicio.plusDays(3), 60, paroquia);
    }
}
