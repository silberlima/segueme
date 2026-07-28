package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidato;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ListarCandidatosDoEmprestimoUseCaseTest {

    @Mock
    private EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    @Test
    void shouldDelegateToRepository() {
        UUID emprestimoId = UUID.randomUUID();
        List<EmprestimoCandidato> candidatos = List.of();
        when(emprestimoCandidatoRepository.findByEmprestimo_Id(emprestimoId)).thenReturn(candidatos);

        List<EmprestimoCandidato> resultado = new ListarCandidatosDoEmprestimoUseCase(emprestimoCandidatoRepository).execute(emprestimoId);

        assertThat(resultado).isSameAs(candidatos);
    }
}
