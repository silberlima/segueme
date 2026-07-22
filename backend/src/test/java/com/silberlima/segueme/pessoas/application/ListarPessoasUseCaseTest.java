package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

@ExtendWith(MockitoExtension.class)
class ListarPessoasUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Test
    void shouldDelegateToRepositoryWithCombinedSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Pessoa> pagina = new PageImpl<>(List.of());
        when(pessoaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<Pessoa> resultado = new ListarPessoasUseCase(pessoaRepository)
                .execute(new EscopoTerritorial.Nacional(), "Maria", null, null, pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
