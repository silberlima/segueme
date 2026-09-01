package com.silberlima.segueme.inscricoes.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.inscricoes.domain.FichaInscricao;
import com.silberlima.segueme.inscricoes.domain.FichaInscricaoRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreencherFichaUseCaseTest {

    @Mock
    private PreInscricaoRepository preInscricaoRepository;

    @Mock
    private FichaInscricaoRepository fichaInscricaoRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    private final Paroquia paroquia = umaParoquia();
    private final Pessoa pessoa = umaPessoa();

    @Test
    void shouldConfirmFichaAndPromotePessoaToSeguimista() {
        UUID preInscricaoId = UUID.randomUUID();
        Encontro encontro = umEncontro();
        PreInscricao preInscricao = new PreInscricao(pessoa, encontro);
        when(preInscricaoRepository.findByIdAndDeletedFalse(preInscricaoId)).thenReturn(Optional.of(preInscricao));
        when(fichaInscricaoRepository.existsByPreInscricao_Id(preInscricaoId)).thenReturn(false);
        when(fichaInscricaoRepository.save(any(FichaInscricao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        FichaInscricao ficha = new PreencherFichaUseCase(
                        preInscricaoRepository, fichaInscricaoRepository, historicoEventoRepository)
                .execute(preInscricaoId);

        assertThat(ficha).isNotNull();
        assertThat(pessoa.getSituacao()).isEqualTo(SituacaoPessoa.SEGUIMISTA);
        assertThat(preInscricao.getStatus().name()).isEqualTo("CONFIRMADA");
    }

    @Test
    void shouldThrowWhenFichaAlreadyExists() {
        UUID preInscricaoId = UUID.randomUUID();
        PreInscricao preInscricao = new PreInscricao(pessoa, umEncontro());
        when(preInscricaoRepository.findByIdAndDeletedFalse(preInscricaoId)).thenReturn(Optional.of(preInscricao));
        when(fichaInscricaoRepository.existsByPreInscricao_Id(preInscricaoId)).thenReturn(true);

        assertThatThrownBy(() -> new PreencherFichaUseCase(
                                preInscricaoRepository, fichaInscricaoRepository, historicoEventoRepository)
                        .execute(preInscricaoId))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenPreInscricaoNotFound() {
        UUID preInscricaoId = UUID.randomUUID();
        when(preInscricaoRepository.findByIdAndDeletedFalse(preInscricaoId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new PreencherFichaUseCase(
                                preInscricaoRepository, fichaInscricaoRepository, historicoEventoRepository)
                        .execute(preInscricaoId))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }

    private Pessoa umaPessoa() {
        return new Pessoa(
                "Candidato Teste",
                PessoasDeTeste.cpf(),
                LocalDate.of(2002, 3, 10),
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

    private Encontro umEncontro() {
        return new Encontro(
                1, "São José", pessoa, "Sítio Teste", LocalDate.now(), LocalDate.now().plusDays(2), 40, paroquia);
    }
}
