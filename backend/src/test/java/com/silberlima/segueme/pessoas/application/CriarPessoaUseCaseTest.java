package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
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
import com.silberlima.segueme.support.PessoasDeTeste;
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
        CriarPessoaUseCase useCase = umUseCase();
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pessoa pessoa = useCase.execute(dadosDe(paroquiaId, PessoasDeTeste.cpf(), Set.of()));

        assertThat(pessoa.getSituacao()).isEqualTo(SituacaoPessoa.PRE_INSCRITO);
        assertThat(pessoa.getNomeCompleto()).isEqualTo("Maria Souza");

        ArgumentCaptor<HistoricoEvento> captor = ArgumentCaptor.forClass(HistoricoEvento.class);
        verify(historicoEventoRepository).save(captor.capture());
        assertThat(captor.getValue().getTipo()).isEqualTo(TipoEventoHistorico.INSCRICAO_CADASTRO_NACIONAL);
        assertThat(captor.getValue().getPessoa()).isSameAs(pessoa);
    }

    /** O CPF chega como o usuário digitou; o cadastro guarda a forma canônica. */
    @Test
    void shouldStoreCpfWithDigitsOnly() {
        CriarPessoaUseCase useCase = umUseCase();
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pessoa pessoa = useCase.execute(dadosDe(paroquiaId, "529.982.247-25", Set.of()));

        assertThat(pessoa.getCpf()).isEqualTo("52998224725");
    }

    /** RN-PESSOA-01: o cadastro nacional é único, e o CPF é o que passa a garantir isso. */
    @Test
    void shouldRejectDuplicatedCpf() {
        CriarPessoaUseCase useCase = umUseCase();
        when(pessoaRepository.existsByCpfAndDeletedFalse("52998224725")).thenReturn(true);

        DadosNovaPessoa dados = dadosDe(UUID.randomUUID(), "529.982.247-25", Set.of());

        assertThatThrownBy(() -> useCase.execute(dados))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("CPF");
        verify(pessoaRepository, never()).save(any(Pessoa.class));
    }

    @Test
    void shouldThrowWhenParoquiaNotFound() {
        CriarPessoaUseCase useCase = umUseCase();
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.empty());

        DadosNovaPessoa dados = dadosDe(paroquiaId, PessoasDeTeste.cpf(), Set.of());

        assertThatThrownBy(() -> useCase.execute(dados)).isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldAttachCompetenciasWhenProvided() {
        CriarPessoaUseCase useCase = umUseCase();
        UUID paroquiaId = UUID.randomUUID();
        Competencia canta = new Competencia("Canta");
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(competenciaRepository.findAllById(any())).thenReturn(List.of(canta));
        when(pessoaRepository.save(any(Pessoa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Pessoa pessoa = useCase.execute(dadosDe(paroquiaId, PessoasDeTeste.cpf(), Set.of(UUID.randomUUID())));

        assertThat(pessoa.getCompetencias()).containsExactly(canta);
    }

    private CriarPessoaUseCase umUseCase() {
        return new CriarPessoaUseCase(
                pessoaRepository, paroquiaRepository, competenciaRepository, historicoEventoRepository);
    }

    private DadosNovaPessoa dadosDe(UUID paroquiaId, String cpf, Set<UUID> competenciaIds) {
        return new DadosNovaPessoa(
                "Maria Souza",
                cpf,
                LocalDate.of(2000, 1, 1),
                "(11) 91234-5678",
                null,
                EstadoCivil.SOLTEIRO,
                paroquiaId,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                competenciaIds);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }
}
