package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoCandidatoRepository;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ListarCandidatosEmprestimoUseCaseTest {

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Mock
    private EmprestimoCandidatoRepository emprestimoCandidatoRepository;

    private final Paroquia paroquiaDoEncontro = umaParoquia("Paróquia do Encontro");
    private final Paroquia outraParoquia = umaParoquia("Outra Paróquia");
    private final Pessoa diretor = umaPessoa("Diretor", paroquiaDoEncontro);
    private final Encontro encontro =
            new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquiaDoEncontro);
    private final Equipe equipe = new Equipe("Cozinha", 2, 4, null);

    @Test
    void shouldExcludeSameParoquiaAlreadyVinculadoAndAlreadyCandidato() {
        UUID emprestimoId = UUID.randomUUID();
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        ReflectionTestUtils.setField(emprestimo, "id", emprestimoId);

        Pessoa elegivel = umaPessoa("Elegível", outraParoquia);
        Pessoa mesmaParoquia = umaPessoa("Mesma Paróquia", paroquiaDoEncontro);
        Pessoa jaVinculado = umaPessoa("Já Vinculado", outraParoquia);
        Pessoa jaCandidato = umaPessoa("Já Candidato", outraParoquia);

        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.of(emprestimo));
        when(integranteEquipeRepository.idsPessoaVinculadaNaEquipe(encontro.getId(), equipe.getId()))
                .thenReturn(Set.of(jaVinculado.getId()));
        when(emprestimoCandidatoRepository.idsPessoaCandidataAoEmprestimo(emprestimoId))
                .thenReturn(Set.of(jaCandidato.getId()));
        when(pessoaRepository.findAll(any(Specification.class)))
                .thenReturn(List.of(elegivel, mesmaParoquia, jaVinculado, jaCandidato));

        List<Pessoa> candidatos = new ListarCandidatosEmprestimoUseCase(
                        emprestimoRepository, pessoaRepository, integranteEquipeRepository, emprestimoCandidatoRepository)
                .execute(emprestimoId, new EscopoTerritorial.Nacional(), null, null);

        assertThat(candidatos).containsExactly(elegivel);
    }

    @Test
    void shouldThrowWhenEmprestimoNotFound() {
        UUID emprestimoId = UUID.randomUUID();
        when(emprestimoRepository.findByIdAndDeletedFalse(emprestimoId)).thenReturn(Optional.empty());

        org.assertj.core.api.Assertions.assertThatThrownBy(() -> new ListarCandidatosEmprestimoUseCase(
                        emprestimoRepository, pessoaRepository, integranteEquipeRepository, emprestimoCandidatoRepository)
                .execute(emprestimoId, new EscopoTerritorial.Nacional(), null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Paroquia umaParoquia(String nome) {
        Paroquia paroquia = new Paroquia(nome, new Setor("Setor " + nome, new Diocese("Diocese " + nome)));
        ReflectionTestUtils.setField(paroquia, "id", UUID.randomUUID());
        return paroquia;
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        Pessoa pessoa = new Pessoa(
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
        ReflectionTestUtils.setField(pessoa, "id", UUID.randomUUID());
        return pessoa;
    }
}
