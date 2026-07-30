package com.silberlima.segueme.convites.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.convites.domain.ConviteRecusadoEvent;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RecusarConviteUseCaseTest {

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Test
    void shouldRefuseAndPublishEventWithCreatedByAsDestinatario() {
        UUID conviteId = UUID.randomUUID();
        IntegranteEquipe convite = umConvite();
        ReflectionTestUtils.setField(convite, "createdBy", "comando-user-1");
        when(integranteEquipeRepository.findWithEquipeAndPessoaById(conviteId)).thenReturn(Optional.of(convite));

        IntegranteEquipe resultado = new RecusarConviteUseCase(integranteEquipeRepository, eventPublisher)
                .execute(conviteId, "Não poderei participar.");

        assertThat(resultado.getStatus()).isEqualTo(StatusIntegrante.RECUSADO);
        var captor = org.mockito.ArgumentCaptor.forClass(ConviteRecusadoEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().destinatarioId()).isEqualTo("comando-user-1");
        assertThat(captor.getValue().justificativa()).isEqualTo("Não poderei participar.");
    }

    @Test
    void shouldThrowWhenConviteNotFound() {
        UUID conviteId = UUID.randomUUID();
        when(integranteEquipeRepository.findWithEquipeAndPessoaById(conviteId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RecusarConviteUseCase(integranteEquipeRepository, eventPublisher)
                        .execute(conviteId, "Justificativa"))
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
