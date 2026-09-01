package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.EquipeRepository;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.StatusEmprestimo;
import com.silberlima.segueme.pessoas.domain.Competencia;
import com.silberlima.segueme.pessoas.domain.CompetenciaRepository;
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
class SolicitarEmprestimoUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeRepository equipeRepository;

    @Mock
    private CompetenciaRepository competenciaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    private final Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
    private final Pessoa diretor = new Pessoa(
            "Diretor",
            PessoasDeTeste.cpf(),
            LocalDate.of(1980, 1, 1),
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
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldCreateSolicitadoEmprestimoWithCompetencia() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        UUID competenciaId = UUID.randomUUID();
        Competencia competencia = new Competencia("Cozinha");
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(competenciaRepository.findByIdAndDeletedFalse(competenciaId)).thenReturn(Optional.of(competencia));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = new SolicitarEmprestimoUseCase(encontroRepository, equipeRepository, competenciaRepository, emprestimoRepository)
                .execute(encontroId, equipeId, competenciaId);

        assertThat(emprestimo.getStatus()).isEqualTo(StatusEmprestimo.SOLICITADO);
        assertThat(emprestimo.getCompetenciaRequerida()).isEqualTo(competencia);
        assertThat(emprestimo.getEncontro()).isEqualTo(encontro);
        assertThat(emprestimo.getEquipe()).isEqualTo(equipe);
    }

    @Test
    void shouldCreateEmprestimoWithoutCompetencia() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(equipeRepository.findByIdAndDeletedFalse(equipeId)).thenReturn(Optional.of(equipe));
        when(emprestimoRepository.save(any(Emprestimo.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Emprestimo emprestimo = new SolicitarEmprestimoUseCase(encontroRepository, equipeRepository, competenciaRepository, emprestimoRepository)
                .execute(encontroId, equipeId, null);

        assertThat(emprestimo.getCompetenciaRequerida()).isNull();
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new SolicitarEmprestimoUseCase(encontroRepository, equipeRepository, competenciaRepository, emprestimoRepository)
                        .execute(encontroId, UUID.randomUUID(), null))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
