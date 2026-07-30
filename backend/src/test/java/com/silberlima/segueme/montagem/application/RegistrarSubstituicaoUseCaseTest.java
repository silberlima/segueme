package com.silberlima.segueme.montagem.application;

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
import com.silberlima.segueme.montagem.application.dto.DadosSubstituicao;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.montagem.domain.Substituicao;
import com.silberlima.segueme.montagem.domain.SubstituicaoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RegistrarSubstituicaoUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private SubstituicaoRepository substituicaoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    private final Paroquia paroquia = umaParoquia("Paróquia Teste");
    private final Encontro encontro = umEncontro(paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);
    private final Pessoa pessoaSai = umaPessoa("Vai Sair", paroquia);
    private final Pessoa pessoaEntra = umaPessoa("Vai Entrar", paroquia);

    @Test
    void shouldMarkOutgoingAsRecusadoAndCreateNewIntegranteAsPendente() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaSaiId = UUID.randomUUID();
        UUID pessoaEntraId = UUID.randomUUID();
        IntegranteEquipe integranteSai = new IntegranteEquipe(encontro, equipe, pessoaSai, PapelIntegrante.INTEGRANTE);
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaSaiId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.of(integranteSai));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaEntraId)).thenReturn(Optional.of(pessoaEntra));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaEntraId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.save(any(IntegranteEquipe.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(substituicaoRepository.save(any(Substituicao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Substituicao substituicao = new RegistrarSubstituicaoUseCase(
                        encontroRepository,
                        equipeRepository,
                        integranteEquipeRepository,
                        substituicaoRepository,
                        pessoaRepository,
                        historicoEventoRepository,
                        eventPublisher)
                .execute(new DadosSubstituicao(encontroId, equipeId, pessoaSaiId, "Recusou o convite", pessoaEntraId, "Justificativa válida"));

        assertThat(integranteSai.getStatus()).isEqualTo(StatusIntegrante.RECUSADO);
        assertThat(substituicao.getPessoaSai()).isEqualTo(pessoaSai);
        assertThat(substituicao.getPessoaEntra()).isEqualTo(pessoaEntra);
        assertThat(substituicao.getJustificativa()).isEqualTo("Justificativa válida");
    }

    @Test
    void shouldThrowWhenOutgoingPersonDoesNotIntegrateTeam() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaSaiId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaSaiId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RegistrarSubstituicaoUseCase(
                        encontroRepository,
                        equipeRepository,
                        integranteEquipeRepository,
                        substituicaoRepository,
                        pessoaRepository,
                        historicoEventoRepository,
                        eventPublisher)
                .execute(new DadosSubstituicao(encontroId, equipeId, pessoaSaiId, "Motivo", UUID.randomUUID(), "Justificativa")))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenIncomingPersonIsFromAnotherParoquia() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaSaiId = UUID.randomUUID();
        UUID pessoaEntraId = UUID.randomUUID();
        Pessoa pessoaDeOutraParoquia = umaPessoa("Forasteiro", umaParoquia("Outra Paróquia"));
        IntegranteEquipe integranteSai = new IntegranteEquipe(encontro, equipe, pessoaSai, PapelIntegrante.INTEGRANTE);
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaSaiId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.of(integranteSai));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaEntraId)).thenReturn(Optional.of(pessoaDeOutraParoquia));

        assertThatThrownBy(() -> new RegistrarSubstituicaoUseCase(
                        encontroRepository,
                        equipeRepository,
                        integranteEquipeRepository,
                        substituicaoRepository,
                        pessoaRepository,
                        historicoEventoRepository,
                        eventPublisher)
                .execute(new DadosSubstituicao(encontroId, equipeId, pessoaSaiId, "Motivo", pessoaEntraId, "Justificativa")))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Paroquia umaParoquia(String nome) {
        Diocese diocese = new Diocese("Diocese " + nome);
        Setor setor = new Setor("Setor " + nome, diocese);
        Paroquia paroquia = new Paroquia(nome, setor);
        ReflectionTestUtils.setField(paroquia, "id", UUID.randomUUID());
        return paroquia;
    }

    private Encontro umEncontro(Paroquia paroquia) {
        return new Encontro(
                1, "São José", umaPessoa("Diretor", paroquia), "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome, LocalDate.of(2000, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia, null, null, null, null, null, null, null);
    }
}
