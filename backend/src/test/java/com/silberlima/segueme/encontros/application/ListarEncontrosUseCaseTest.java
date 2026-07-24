package com.silberlima.segueme.encontros.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
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
class ListarEncontrosUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Test
    void shouldDelegateToRepositoryWithCombinedSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Encontro> pagina = new PageImpl<>(List.of());
        when(encontroRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<Encontro> resultado = new ListarEncontrosUseCase(encontroRepository)
                .execute(new EscopoTerritorial.Nacional(), null, null, null, pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
