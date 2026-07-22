package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

@ExtendWith(MockitoExtension.class)
class ListarPessoasPorCompetenciaUseCaseTest {

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private ListarPessoasUseCase listarPessoasUseCase;

    @Test
    void shouldThrowWhenCompetenciaNotFound() {
        UUID competenciaId = UUID.randomUUID();
        when(competenciaRepository.findByIdAndDeletedFalse(competenciaId)).thenReturn(Optional.empty());
        Pageable pageable = PageRequest.of(0, 10);

        assertThatThrownBy(() -> new ListarPessoasPorCompetenciaUseCase(competenciaRepository, listarPessoasUseCase)
                        .execute(new EscopoTerritorial.Nacional(), competenciaId, pageable))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldDelegateToListarPessoasUseCaseWhenCompetenciaExists() {
        UUID competenciaId = UUID.randomUUID();
        when(competenciaRepository.findByIdAndDeletedFalse(competenciaId))
                .thenReturn(Optional.of(new Competencia("Canta")));
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pessoa> pagina = new PageImpl<>(List.of());
        EscopoTerritorial escopo = new EscopoTerritorial.Nacional();
        when(listarPessoasUseCase.execute(escopo, null, null, competenciaId, pageable)).thenReturn(pagina);

        Page<Pessoa> resultado = new ListarPessoasPorCompetenciaUseCase(competenciaRepository, listarPessoasUseCase)
                .execute(escopo, competenciaId, pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
