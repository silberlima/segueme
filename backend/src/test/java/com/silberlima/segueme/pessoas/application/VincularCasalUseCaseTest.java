package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
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
class VincularCasalUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private CasalRepository casalRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    @Test
    void shouldCreateCasalAndRegisterHistoricoForBoth() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        Pessoa pessoa1 = umaPessoa("Pessoa Um");
        Pessoa pessoa2 = umaPessoa("Pessoa Dois");
        when(pessoaRepository.findByIdAndDeletedFalse(id1)).thenReturn(Optional.of(pessoa1));
        when(pessoaRepository.findByIdAndDeletedFalse(id2)).thenReturn(Optional.of(pessoa2));
        when(casalRepository.existsByPessoa1_IdOrPessoa2_Id(id1, id1)).thenReturn(false);
        when(casalRepository.existsByPessoa1_IdOrPessoa2_Id(id2, id2)).thenReturn(false);
        when(casalRepository.save(any(Casal.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Casal casal = new VincularCasalUseCase(pessoaRepository, casalRepository, historicoEventoRepository)
                .execute(id1, id2, LocalDate.of(2022, 3, 1));

        assertThat(casal.getPessoa1()).isSameAs(pessoa1);
        assertThat(casal.getPessoa2()).isSameAs(pessoa2);
        verify(historicoEventoRepository, times(2)).save(any());
    }

    @Test
    void shouldThrowWhenSamePersonTwice() {
        UUID id = UUID.randomUUID();

        assertThatThrownBy(() -> new VincularCasalUseCase(pessoaRepository, casalRepository, historicoEventoRepository)
                        .execute(id, id, LocalDate.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenPessoaNotFound() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(id1)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new VincularCasalUseCase(pessoaRepository, casalRepository, historicoEventoRepository)
                        .execute(id1, id2, LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenPessoaAlreadyInCasal() {
        UUID id1 = UUID.randomUUID();
        UUID id2 = UUID.randomUUID();
        when(pessoaRepository.findByIdAndDeletedFalse(id1)).thenReturn(Optional.of(umaPessoa("Pessoa Um")));
        when(pessoaRepository.findByIdAndDeletedFalse(id2)).thenReturn(Optional.of(umaPessoa("Pessoa Dois")));
        when(casalRepository.existsByPessoa1_IdOrPessoa2_Id(id1, id1)).thenReturn(true);

        assertThatThrownBy(() -> new VincularCasalUseCase(pessoaRepository, casalRepository, historicoEventoRepository)
                        .execute(id1, id2, LocalDate.now()))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Pessoa umaPessoa(String nome) {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        Paroquia paroquia = new Paroquia("Paróquia Teste", setor);
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(1995, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.CASADO,
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
