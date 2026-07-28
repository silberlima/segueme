package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
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
class ListarTransferenciasUseCaseTest {

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Test
    void shouldDelegateToRepositoryWithCombinedSpecification() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Transferencia> pagina = new PageImpl<>(List.of());
        when(transferenciaRepository.findAll(any(Specification.class), eq(pageable))).thenReturn(pagina);

        Page<Transferencia> resultado = new ListarTransferenciasUseCase(transferenciaRepository)
                .execute(new EscopoTerritorial.Nacional(), null, null, pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
