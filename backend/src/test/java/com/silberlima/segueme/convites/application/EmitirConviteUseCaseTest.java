package com.silberlima.segueme.convites.application;

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
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
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
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class EmitirConviteUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    private final Paroquia paroquia = umaParoquia("Paróquia Teste");
    private final Encontro encontro = umEncontro(paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);
    private final Pessoa pessoa = umaPessoa("Candidato", paroquia);

    private EmitirConviteUseCase useCase() {
        return new EmitirConviteUseCase(encontroRepository, equipeRepository, integranteEquipeRepository, pessoaRepository);
    }

    @Test
    void shouldEmitirConviteDeCoordenadorSemPreCondicao() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        stubEncontroEquipePessoa(encontroId, equipeId, pessoaId);
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                        encontroId, equipeId, StatusIntegrante.RECUSADO))
                .thenReturn(0L);
        when(integranteEquipeRepository.save(any(IntegranteEquipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IntegranteEquipe convite = useCase().execute(encontroId, equipeId, pessoaId, PapelIntegrante.COORDENADOR);

        assertThat(convite.getPapel()).isEqualTo(PapelIntegrante.COORDENADOR);
        assertThat(convite.getStatus()).isEqualTo(StatusIntegrante.PENDENTE);
    }

    @Test
    void shouldThrowWhenEmitindoConviteDeIntegranteSemCoordenadorAceito() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        stubEncontroEquipePessoa(encontroId, equipeId, pessoaId);
        when(integranteEquipeRepository.existsByEncontro_IdAndEquipe_IdAndPapelAndStatus(
                        encontroId, equipeId, PapelIntegrante.COORDENADOR, StatusIntegrante.ACEITO))
                .thenReturn(false);

        assertThatThrownBy(() -> useCase().execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldEmitirConviteDeIntegranteQuandoCoordenadorJaAceitou() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID pessoaId = UUID.randomUUID();
        stubEncontroEquipePessoa(encontroId, equipeId, pessoaId);
        when(integranteEquipeRepository.existsByEncontro_IdAndEquipe_IdAndPapelAndStatus(
                        encontroId, equipeId, PapelIntegrante.COORDENADOR, StatusIntegrante.ACEITO))
                .thenReturn(true);
        when(integranteEquipeRepository.findByEncontro_IdAndEquipe_IdAndPessoa_IdAndStatusNot(
                        encontroId, equipeId, pessoaId, StatusIntegrante.RECUSADO))
                .thenReturn(Optional.empty());
        when(integranteEquipeRepository.countByEncontro_IdAndEquipe_IdAndStatusNot(
                        encontroId, equipeId, StatusIntegrante.RECUSADO))
                .thenReturn(0L);
        when(integranteEquipeRepository.save(any(IntegranteEquipe.class))).thenAnswer(invocation -> invocation.getArgument(0));

        IntegranteEquipe convite = useCase().execute(encontroId, equipeId, pessoaId, PapelIntegrante.INTEGRANTE);

        assertThat(convite.getPapel()).isEqualTo(PapelIntegrante.INTEGRANTE);
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

        assertThatThrownBy(() -> useCase().execute(encontroId, equipeId, pessoaId, PapelIntegrante.COORDENADOR))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(encontroId, UUID.randomUUID(), UUID.randomUUID(), PapelIntegrante.COORDENADOR))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private void stubEncontroEquipePessoa(UUID encontroId, UUID equipeId, UUID pessoaId) {
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
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
