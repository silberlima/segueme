package com.silberlima.segueme.financeiro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
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
class ListarCampanhasUseCaseTest {

    @Mock
    private CampanhaRepository campanhaRepository;

    @Test
    void shouldDelegateToRepositoryWithCombinedSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Campanha> pagina = new PageImpl<>(List.of());
        when(campanhaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<Campanha> resultado = new ListarCampanhasUseCase(campanhaRepository)
                .execute(new EscopoTerritorial.Nacional(), null, null, pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
