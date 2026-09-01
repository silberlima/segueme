package com.silberlima.segueme.conteudo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
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
class CadastrarPalestranteUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private PalestranteRepository palestranteRepository;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa pessoa = new Pessoa(
            "Seguidor",
            PessoasDeTeste.cpf(),
            LocalDate.of(1990, 1, 1),
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

    private CadastrarPalestranteUseCase useCase() {
        return new CadastrarPalestranteUseCase(pessoaRepository, palestranteRepository);
    }

    @Test
    void shouldCreateExterno() {
        when(palestranteRepository.save(any(Palestrante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Palestrante resultado = useCase().execute(TipoPalestrante.EXTERNO, "Convidado", null);

        assertThat(resultado.getTipo()).isEqualTo(TipoPalestrante.EXTERNO);
        assertThat(resultado.getPessoa()).isNull();
    }

    @Test
    void shouldCreateSeguidorLoadingPessoa() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(palestranteRepository.save(any(Palestrante.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Palestrante resultado = useCase().execute(TipoPalestrante.SEGUIDOR, null, pessoaId);

        assertThat(resultado.getTipo()).isEqualTo(TipoPalestrante.SEGUIDOR);
        assertThat(resultado.getPessoa()).isEqualTo(pessoa);
    }

    @Test
    void shouldRejectSeguidorWithoutPessoaId() {
        assertThatThrownBy(() -> useCase().execute(TipoPalestrante.SEGUIDOR, null, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenPessoaNotFound() {
        UUID pessoaId = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(TipoPalestrante.SEGUIDOR, null, pessoaId))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
