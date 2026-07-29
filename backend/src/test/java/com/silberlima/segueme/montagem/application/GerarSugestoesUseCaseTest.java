package com.silberlima.segueme.montagem.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.application.dto.CandidatoSugerido;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.posencontro.application.ConsultarPercentualFrequenciaUseCase;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class GerarSugestoesUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ConsultarPercentualFrequenciaUseCase consultarPercentualFrequenciaUseCase;

    private final Paroquia paroquiaDoEncontro = umaParoquia("Paróquia do Encontro");
    private final Encontro encontro = umEncontro(paroquiaDoEncontro);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldExcludeCandidatesAlreadyLinkedToTeam() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        Pessoa jaVinculado = umaPessoa("Já Vinculado", paroquiaDoEncontro);
        ReflectionTestUtils.setField(jaVinculado, "id", UUID.randomUUID());
        Pessoa candidato = umaPessoa("Candidato Livre", paroquiaDoEncontro);
        ReflectionTestUtils.setField(candidato, "id", UUID.randomUUID());

        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.idsPessoaVinculadaNaEquipe(encontroId, equipeId))
                .thenReturn(Set.of(jaVinculado.getId()));
        when(pessoaRepository.findBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR))
                .thenReturn(List.of(jaVinculado, candidato));

        List<CandidatoSugerido> sugestoes = umUseCase().execute(encontroId, equipeId);

        assertThat(sugestoes).extracting(CandidatoSugerido::pessoa).containsExactly(candidato);
    }

    @Test
    void shouldFlagCandidateFromAnotherParoquiaAsRequiringEmprestimo() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        Pessoa deOutraParoquia = umaPessoa("Forasteiro", umaParoquia("Outra Paróquia"));
        ReflectionTestUtils.setField(deOutraParoquia, "id", UUID.randomUUID());

        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.idsPessoaVinculadaNaEquipe(encontroId, equipeId)).thenReturn(Set.of());
        when(pessoaRepository.findBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR))
                .thenReturn(List.of(deOutraParoquia));

        List<CandidatoSugerido> sugestoes = umUseCase().execute(encontroId, equipeId);

        assertThat(sugestoes).hasSize(1);
        assertThat(sugestoes.get(0).mesmaParoquia()).isFalse();
        assertThat(sugestoes.get(0).justificativas()).contains("Requer empréstimo do setor");
    }

    /**
     * O fator de frequência (20% do score, RN-09/RN-MONTAGEM-03) nasceu zerado na Fase 6 e passou a
     * usar a apuração real do pós-encontro na Fase 11: quem tem dado pontua, quem não tem fica em 0
     * sem ser confundido com quem tem 0% de presença.
     */
    @Test
    void shouldScoreFrequenciaPosEncontroFromApuracaoReal() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        Pessoa frequente = umaPessoa("Frequente", paroquiaDoEncontro);
        ReflectionTestUtils.setField(frequente, "id", UUID.randomUUID());
        Pessoa semDado = umaPessoa("Sem Dado", paroquiaDoEncontro);
        ReflectionTestUtils.setField(semDado, "id", UUID.randomUUID());

        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(integranteEquipeRepository.idsPessoaVinculadaNaEquipe(encontroId, equipeId)).thenReturn(Set.of());
        when(pessoaRepository.findBySituacaoAndDeletedFalse(SituacaoPessoa.SEGUIDOR))
                .thenReturn(List.of(frequente, semDado));
        when(consultarPercentualFrequenciaUseCase.execute()).thenReturn(Map.of(frequente.getId(), 80));

        List<CandidatoSugerido> sugestoes = umUseCase().execute(encontroId, equipeId);

        assertThat(sugestoes).extracting(CandidatoSugerido::pessoa).containsExactly(frequente, semDado);
        assertThat(sugestoes.get(0).score().frequenciaPosEncontro()).isEqualTo(80);
        assertThat(sugestoes.get(0).justificativas()).contains("80% de frequência no pós-encontro");
        assertThat(sugestoes.get(1).score().frequenciaPosEncontro()).isZero();
        assertThat(sugestoes.get(1).justificativas()).noneMatch(justificativa -> justificativa.contains("frequência"));
    }

    @Test
    void shouldThrowWhenEquipeNotFound() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> umUseCase().execute(encontroId, equipeId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private GerarSugestoesUseCase umUseCase() {
        return new GerarSugestoesUseCase(
                encontroRepository,
                equipeRepository,
                integranteEquipeRepository,
                pessoaRepository,
                consultarPercentualFrequenciaUseCase);
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
