package com.silberlima.segueme.montagem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class AdicionarIntegranteUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    private final Paroquia paroquia = umaParoquia("Paróquia Teste");
    private final Encontro encontro = umEncontro(paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);
    private final Pessoa pessoa = umaPessoa("Candidato", paroquia);

    @Test
    void shouldAddIntegranteAsPendenteWhenEligible() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                        encontroId, equipeId, StatusIntegrante.RECUSADO))
                .thenReturn(0L);
        when(integranteEquipeRepository.save(any(IntegranteEquipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IntegranteEquipe integrante = new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE);

        assertThat(integrante.getStatus()).isEqualTo(StatusIntegrante.PENDENTE);
        assertThat(integrante.getPessoa()).isEqualTo(pessoa);
    }

    @Test
    void shouldThrowWhenCandidateIsFromAnotherParoquia() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        Pessoa pessoaDeOutraParoquia = umaPessoa("Forasteiro", umaParoquia("Outra Paróquia"));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoaDeOutraParoquia));

        assertThatThrownBy(() -> new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenAlreadyIntegranteOfTeam() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.of(new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.INTEGRANTE)));

        assertThatThrownBy(() -> new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenTeamReachedMaximum() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                        encontroId, equipeId, StatusIntegrante.RECUSADO))
                .thenReturn((long) equipe.getMaximo());

        assertThatThrownBy(() -> new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, UUID.randomUUID(), UUID.randomUUID(), PapelIntegrante.INTEGRANTE))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAllowCandidateFromAnotherParoquiaWhenViaEmprestimoAprovado() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        Pessoa pessoaDeOutraParoquia = umaPessoa("Emprestado", umaParoquia("Outra Paróquia"));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoaDeOutraParoquia));
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                        encontroId, equipeId, StatusIntegrante.RECUSADO))
                .thenReturn(0L);
        when(integranteEquipeRepository.save(any(IntegranteEquipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IntegranteEquipe integrante = new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE, true);

        assertThat(integrante.getStatus()).isEqualTo(StatusIntegrante.PENDENTE);
        assertThat(integrante.getPessoa()).isEqualTo(pessoaDeOutraParoquia);
        ArgumentCaptor<HistoricoEvento> captor = ArgumentCaptor.forClass(HistoricoEvento.class);
        verify(historicoEventoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoEventoHistorico.EMPRESTIMO_OUTRA_PAROQUIA);
    }

    @Test
    void shouldStillThrowWhenAnotherParoquiaAndNotViaEmprestimoAprovado() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        Pessoa pessoaDeOutraParoquia = umaPessoa("Forasteiro", umaParoquia("Outra Paróquia"));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoaDeOutraParoquia));

        assertThatThrownBy(() -> new AdicionarIntegranteUseCase(
                        encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository, historicoEventoRepository)
                .execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE, false))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    private Paroquia umaParoquia(String nome) {
        Diocese diocese = new Diocese("Diocese " + nome);
        Setor setor = new Setor("Setor " + nome, diocese);
        Paroquia paroquia = new Paroquia(nome, setor);
        // Entidades não persistidas nascem com id null (RN-mapeamento GenerationType.UUID) — atribuído
        // aqui via reflection só para permitir comparação determinística em teste puro de unidade.
        ReflectionTestUtils.setField(paroquia, "id", UUID.randomUUID());
        return paroquia;
    }

    private Encontro umEncontro(Paroquia paroquia) {
        return new Encontro(
                1, "São José", umaPessoa("Diretor", paroquia), "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
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
