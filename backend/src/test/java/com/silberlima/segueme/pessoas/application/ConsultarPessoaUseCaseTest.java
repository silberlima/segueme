package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarPessoaUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Test
    void shouldReturnPessoaWhenFound() {
        UUID id = UUID.randomUUID();
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(pessoa));

        Pessoa resultado = new ConsultarPessoaUseCase(pessoaRepository).execute(id);

        assertThat(resultado).isSameAs(pessoa);
    }

    @Test
    void shouldThrowWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarPessoaUseCase(pessoaRepository).execute(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Pessoa umaPessoa() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        Paroquia paroquia = new Paroquia("Paróquia Teste", setor);
        return new Pessoa(
                "João Teste",
                PessoasDeTeste.cpf(),
                LocalDate.of(1999, 5, 20),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }
}
