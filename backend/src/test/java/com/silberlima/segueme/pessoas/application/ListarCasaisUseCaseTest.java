package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
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
class ListarCasaisUseCaseTest {

    @Mock
    private CasalRepository casalRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    private final Pageable pageable = PageRequest.of(0, 10);

    @Test
    void shouldDelegateToUnrestrictedFinderWhenNacional() {
        Page<Casal> pagina = new PageImpl<>(List.of());
        when(casalRepository.findByDeletedFalse(pageable)).thenReturn(pagina);

        Page<Casal> resultado = new ListarCasaisUseCase(casalRepository, pessoaRepository)
                .execute(new EscopoTerritorial.Nacional(), pageable);

        assertThat(resultado).isSameAs(pagina);
        verify(casalRepository).findByDeletedFalse(pageable);
    }

    @Test
    void shouldDelegateToDioceseScopedFinderWhenDeDiocese() {
        UUID dioceseId = UUID.randomUUID();
        Page<Casal> pagina = new PageImpl<>(List.of());
        when(casalRepository.findByPessoa1_Paroquia_Setor_Diocese_IdAndDeletedFalse(dioceseId, pageable))
                .thenReturn(pagina);

        Page<Casal> resultado = new ListarCasaisUseCase(casalRepository, pessoaRepository)
                .execute(new EscopoTerritorial.DeDiocese(dioceseId), pageable);

        assertThat(resultado).isSameAs(pagina);
    }

    @Test
    void shouldDelegateToSetorScopedFinderWhenDeSetor() {
        UUID setorId = UUID.randomUUID();
        Page<Casal> pagina = new PageImpl<>(List.of());
        when(casalRepository.findByPessoa1_Paroquia_Setor_IdAndDeletedFalse(setorId, pageable)).thenReturn(pagina);

        Page<Casal> resultado = new ListarCasaisUseCase(casalRepository, pessoaRepository)
                .execute(new EscopoTerritorial.DeSetor(setorId), pageable);

        assertThat(resultado).isSameAs(pagina);
    }

    @Test
    void shouldDelegateToParoquiaScopedFinderWhenDeParoquia() {
        UUID paroquiaId = UUID.randomUUID();
        Page<Casal> pagina = new PageImpl<>(List.of());
        when(casalRepository.findByPessoa1_Paroquia_IdAndDeletedFalse(paroquiaId, pageable)).thenReturn(pagina);

        Page<Casal> resultado = new ListarCasaisUseCase(casalRepository, pessoaRepository)
                .execute(new EscopoTerritorial.DeParoquia(paroquiaId), pageable);

        assertThat(resultado).isSameAs(pagina);
    }
}
