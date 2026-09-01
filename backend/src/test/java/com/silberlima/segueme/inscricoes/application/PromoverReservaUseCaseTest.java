package com.silberlima.segueme.inscricoes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PromoverReservaUseCaseTest {

    @Mock
    private CadastroReservaRepository cadastroReservaRepository;

    @Mock
    private PreInscricaoRepository preInscricaoRepository;

    @Mock
    private EncontroRepository encontroRepository;

    private final Paroquia paroquia = umaParoquia();
    private final Pessoa pessoa = umaPessoa();

    @Test
    void shouldPromoteCandidatoToPreInscricaoNoEncontroDestino() {
        UUID reservaId = UUID.randomUUID();
        UUID encontroDestinoId = UUID.randomUUID();
        CadastroReserva reserva = new CadastroReserva(pessoa, umEncontro());
        Encontro encontroDestino = umEncontro();
        when(cadastroReservaRepository.findByIdAndDeletedFalse(reservaId)).thenReturn(Optional.of(reserva));
        when(encontroRepository.findByIdAndDeletedFalse(encontroDestinoId)).thenReturn(Optional.of(encontroDestino));
        when(preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(any(), any())).thenReturn(false);
        when(preInscricaoRepository.save(any(PreInscricao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PreInscricao preInscricao =
                new PromoverReservaUseCase(cadastroReservaRepository, preInscricaoRepository, encontroRepository)
                        .execute(reservaId, encontroDestinoId);

        assertThat(preInscricao).isNotNull();
        assertThat(reserva.getStatus().name()).isEqualTo("PROMOVIDO");
    }

    @Test
    void shouldThrowWhenReservaNotFound() {
        UUID reservaId = UUID.randomUUID();
        UUID encontroDestinoId = UUID.randomUUID();
        when(cadastroReservaRepository.findByIdAndDeletedFalse(reservaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new PromoverReservaUseCase(
                                cadastroReservaRepository, preInscricaoRepository, encontroRepository)
                        .execute(reservaId, encontroDestinoId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenPessoaAlreadyPreInscritaNoDestino() {
        UUID reservaId = UUID.randomUUID();
        UUID encontroDestinoId = UUID.randomUUID();
        CadastroReserva reserva = new CadastroReserva(pessoa, umEncontro());
        Encontro encontroDestino = umEncontro();
        when(cadastroReservaRepository.findByIdAndDeletedFalse(reservaId)).thenReturn(Optional.of(reserva));
        when(encontroRepository.findByIdAndDeletedFalse(encontroDestinoId)).thenReturn(Optional.of(encontroDestino));
        when(preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> new PromoverReservaUseCase(
                                cadastroReservaRepository, preInscricaoRepository, encontroRepository)
                        .execute(reservaId, encontroDestinoId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }

    private Pessoa umaPessoa() {
        return new Pessoa(
                "Candidato Teste",
                PessoasDeTeste.cpf(),
                LocalDate.of(2002, 3, 10),
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

    private Encontro umEncontro() {
        return new Encontro(
                1, "São José", pessoa, "Sítio Teste", LocalDate.now(), LocalDate.now().plusDays(2), 40, paroquia);
    }
}
