package com.silberlima.segueme.convites.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReenviarConviteUseCaseTest {

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Test
    void shouldResendPendingConvite() {
        UUID conviteId = UUID.randomUUID();
        IntegranteEquipe convite = umConvite();
        when(integranteEquipeRepository.findWithEquipeAndPessoaById(conviteId)).thenReturn(Optional.of(convite));

        IntegranteEquipe resultado = new ReenviarConviteUseCase(integranteEquipeRepository).execute(conviteId);

        assertThat(resultado.getReenvios()).isEqualTo(1);
    }

    @Test
    void shouldThrowWhenConviteAlreadyRespondido() {
        UUID conviteId = UUID.randomUUID();
        IntegranteEquipe convite = umConvite();
        convite.aceitar();
        when(integranteEquipeRepository.findWithEquipeAndPessoaById(conviteId)).thenReturn(Optional.of(convite));

        assertThatThrownBy(() -> new ReenviarConviteUseCase(integranteEquipeRepository).execute(conviteId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenConviteNotFound() {
        UUID conviteId = UUID.randomUUID();
        when(integranteEquipeRepository.findWithEquipeAndPessoaById(conviteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ReenviarConviteUseCase(integranteEquipeRepository).execute(conviteId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private IntegranteEquipe umConvite() {
        Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor Teste", new Diocese("Diocese Teste")));
        Pessoa diretor = umaPessoa("Diretor", paroquia);
        Encontro encontro = new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
        Equipe equipe = new Equipe("Cozinha", 2, 4, null);
        return new IntegranteEquipe(encontro, equipe, umaPessoa("Candidato", paroquia), PapelIntegrante.INTEGRANTE);
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
