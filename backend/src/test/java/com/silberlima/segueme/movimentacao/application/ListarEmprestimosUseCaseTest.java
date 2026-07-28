package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ListarEmprestimosUseCaseTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Test
    void shouldDelegateToRepositoryWithCombinedSpecification() {
        List<Emprestimo> lista = List.of();
        when(emprestimoRepository.findAll(any(Specification.class))).thenReturn(lista);

        List<Emprestimo> resultado = new ListarEmprestimosUseCase(emprestimoRepository)
                .execute(new EscopoTerritorial.Nacional(), null, null, null);

        assertThat(resultado).isSameAs(lista);
    }
}
