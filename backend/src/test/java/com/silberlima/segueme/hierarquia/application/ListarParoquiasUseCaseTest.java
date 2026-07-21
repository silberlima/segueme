package com.silberlima.segueme.hierarquia.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import java.util.List;
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
class ListarParoquiasUseCaseTest {

    @Mock
    private ParoquiaRepository paroquiaRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void shouldDelegateToUnrestrictedFinderWhenNacional() {
        Page<Paroquia> pagina = new PageImpl<>(List.of(umaParoquia()));
        when(paroquiaRepository.findByDeletedFalse(pageable)).thenReturn(pagina);

        ListarParoquiasUseCase useCase = new ListarParoquiasUseCase(paroquiaRepository);
        Page<Paroquia> resultado = useCase.execute(new EscopoTerritorial.Nacional(), pageable);

        assertThat(resultado).isSameAs(pagina);
        verify(paroquiaRepository).findByDeletedFalse(pageable);
    }

    @Test
    void shouldDelegateToDioceseScopedFinderWhenDeDiocese() {
        UUID dioceseId = UUID.randomUUID();
        Page<Paroquia> pagina = new PageImpl<>(List.of(umaParoquia()));
        when(paroquiaRepository.findBySetor_Diocese_IdAndDeletedFalse(dioceseId, pageable)).thenReturn(pagina);

        ListarParoquiasUseCase useCase = new ListarParoquiasUseCase(paroquiaRepository);
        Page<Paroquia> resultado = useCase.execute(new EscopoTerritorial.DeDiocese(dioceseId), pageable);

        assertThat(resultado).isSameAs(pagina);
        verify(paroquiaRepository).findBySetor_Diocese_IdAndDeletedFalse(dioceseId, pageable);
    }

    @Test
    void shouldDelegateToSetorScopedFinderWhenDeSetor() {
        UUID setorId = UUID.randomUUID();
        Page<Paroquia> pagina = new PageImpl<>(List.of(umaParoquia()));
        when(paroquiaRepository.findBySetor_IdAndDeletedFalse(setorId, pageable)).thenReturn(pagina);

        ListarParoquiasUseCase useCase = new ListarParoquiasUseCase(paroquiaRepository);
        Page<Paroquia> resultado = useCase.execute(new EscopoTerritorial.DeSetor(setorId), pageable);

        assertThat(resultado).isSameAs(pagina);
        verify(paroquiaRepository).findBySetor_IdAndDeletedFalse(setorId, pageable);
    }

    @Test
    void shouldWrapSingleParoquiaWhenDeParoquia() {
        UUID paroquiaId = UUID.randomUUID();
        Paroquia paroquia = umaParoquia();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(java.util.Optional.of(paroquia));

        ListarParoquiasUseCase useCase = new ListarParoquiasUseCase(paroquiaRepository);
        Page<Paroquia> resultado = useCase.execute(new EscopoTerritorial.DeParoquia(paroquiaId), pageable);

        assertThat(resultado.getContent()).containsExactly(paroquia);
        assertThat(resultado.getTotalElements()).isEqualTo(1);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }
}
