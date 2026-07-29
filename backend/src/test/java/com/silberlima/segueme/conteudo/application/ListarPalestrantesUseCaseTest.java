package com.silberlima.segueme.conteudo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
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
class ListarPalestrantesUseCaseTest {

    @Mock
    private PalestranteRepository palestranteRepository;

    @Test
    void shouldDelegateToRepositoryWithSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Palestrante palestrante = Palestrante.externo("Convidado");
        Page<Palestrante> pagina = new PageImpl<>(List.of(palestrante));
        when(palestranteRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<Palestrante> resultado = new ListarPalestrantesUseCase(palestranteRepository).execute(null, pageable);

        assertThat(resultado.getContent()).containsExactly(palestrante);
    }
}
