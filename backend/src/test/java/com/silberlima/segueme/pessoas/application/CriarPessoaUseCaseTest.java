package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.application.dto.DadosNovaPessoa;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEvento;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.pessoas.domain.TipoEventoHistorico;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriarPessoaUseCaseTest {

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    private final Paroquia paroquia = umaParoquia();

    @Test
    void shouldCreatePessoaWithPreInscritoSituacaoAndRegisterHistorico() {
        CriarPessoaUseCase useCase = new CriarPessoaUseCase(
                pessoaRepository, paroquiaRepository, competenciaRepository, historicoEventoRepository);
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovaPessoa dados = new DadosNovaPessoa(
                "Maria Souza",
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of());

        Pessoa pessoa = useCase.execute(dados);

        assertThat(pessoa.getSituacao()).isEqualTo(SituacaoPessoa.PRE_INSCRITO);
        assertThat(pessoa.getNomeCompleto()).isEqualTo("Maria Souza");

        ArgumentCaptor<HistoricoEvento> captor = ArgumentCaptor.forClass(HistoricoEvento.class);
        verify(historicoEventoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoEventoHistorico.INSCRICAO_CADASTRO_NACIONAL);
        assertThat(captor.getValue().getPessoa()).isSameAs(pessoa);
    }

    @Test
    void shouldThrowWhenParoquiaNotFound() {
        CriarPessoaUseCase useCase = new CriarPessoaUseCase(
                pessoaRepository, paroquiaRepository, competenciaRepository, historicoEventoRepository);
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.empty());

        DadosNovaPessoa dados = new DadosNovaPessoa(
                "Maria Souza",
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of());

        assertThatThrownBy(() -> useCase.execute(dados)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAttachCompetenciasWhenProvided() {
        CriarPessoaUseCase useCase = new CriarPessoaUseCase(
                pessoaRepository, paroquiaRepository, competenciaRepository, historicoEventoRepository);
        UUID paroquiaId = UUID.randomUUID();
        Competencia canta = new Competencia("Canta");
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(competenciaRepository.findAllById(any())).thenReturn(List.of(canta));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovaPessoa dados = new DadosNovaPessoa(
                "Maria Souza",
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                Set.of(UUID.randomUUID()));

        Pessoa pessoa = useCase.execute(dados);

        assertThat(pessoa.getCompetencias()).containsExactly(canta);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }
}
