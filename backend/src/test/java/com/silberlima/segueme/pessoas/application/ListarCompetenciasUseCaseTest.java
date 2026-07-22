package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.pessoas.application.dto.CompetenciaComContagem;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.ContagemPorCompetencia;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarCompetenciasUseCaseTest {

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Test
    void shouldReturnCatalogWithCountsFromAggregateQuery() {
        Competencia canta = new Competencia("Canta");
        UUID cantaId = UUID.randomUUID();
        atribuirId(canta, cantaId);
        when(competenciaRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of(canta));
        when(pessoaRepository.contarPorCompetencia()).thenReturn(List.of(umaContagem(cantaId, 3L)));

        List<CompetenciaComContagem> resultado =
                new ListarCompetenciasUseCase(competenciaRepository, pessoaRepository).execute();

        assertThat(resultado).hasSize(1);
        assertThat(resultado.get(0).competencia()).isSameAs(canta);
        assertThat(resultado.get(0).numPessoas()).isEqualTo(3L);
    }

    @Test
    void shouldDefaultToZeroWhenCompetenciaHasNoContagem() {
        Competencia semPessoas = new Competencia("Fotografia");
        when(competenciaRepository.findByDeletedFalseOrderByNomeAsc()).thenReturn(List.of(semPessoas));
        when(pessoaRepository.contarPorCompetencia()).thenReturn(List.of());

        List<CompetenciaComContagem> resultado =
                new ListarCompetenciasUseCase(competenciaRepository, pessoaRepository).execute();

        assertThat(resultado.get(0).numPessoas()).isZero();
    }

    private ContagemPorCompetencia umaContagem(UUID competenciaId, long total) {
        return new ContagemPorCompetencia() {
            @Override
            public UUID getCompetenciaId() {
                return competenciaId;
            }

            @Override
            public Long getTotal() {
                return total;
            }
        };
    }

    private void atribuirId(Object entidade, UUID id) {
        try {
            Field idField = entidade.getClass().getSuperclass().getDeclaredField("id");
            idField.setAccessible(true);
            idField.set(entidade, id);
        } catch (ReflectiveOperationException e) {
            throw new IllegalStateException(e);
        }
    }
}
