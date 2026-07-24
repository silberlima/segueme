package com.silberlima.segueme.inscricoes.application;

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
import com.silberlima.segueme.inscricoes.application.dto.DadosNovaPreInscricao;
import com.silberlima.segueme.inscricoes.application.dto.ResultadoPreInscricao;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.PreInscricao;
import com.silberlima.segueme.inscricoes.domain.PreInscricaoRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class PreInscreverUseCaseTest {

    @Mock
    private PreInscricaoRepository preInscricaoRepository;

    @Mock
    private CadastroReservaRepository cadastroReservaRepository;

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    private final Paroquia paroquia = umaParoquia();
    private final Pessoa pessoa = umaPessoa();

    @Test
    void shouldCreatePreInscricaoWhenVagasAvailable() {
        UUID pessoaId = UUID.randomUUID();
        UUID encontroId = UUID.randomUUID();
        Encontro encontro = umEncontro(2);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(any(), any())).thenReturn(false);
        when(preInscricaoRepository.countByEncontro_Id(encontroId)).thenReturn(0L);
        when(preInscricaoRepository.save(any(PreInscricao.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoPreInscricao resultado = new PreInscreverUseCase(
                        preInscricaoRepository, cadastroReservaRepository, encontroRepository, pessoaRepository)
                .execute(new DadosNovaPreInscricao(pessoaId, encontroId));

        assertThat(resultado.entrouEmReserva()).isFalse();
        assertThat(resultado.preInscricao()).isNotNull();
    }

    @Test
    void shouldFallBackToReservaWhenVagasEsgotadas() {
        UUID pessoaId = UUID.randomUUID();
        UUID encontroId = UUID.randomUUID();
        Encontro encontro = umEncontro(2);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(any(), any())).thenReturn(false);
        when(preInscricaoRepository.countByEncontro_Id(encontroId)).thenReturn(2L);
        when(cadastroReservaRepository.save(any(CadastroReserva.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ResultadoPreInscricao resultado = new PreInscreverUseCase(
                        preInscricaoRepository, cadastroReservaRepository, encontroRepository, pessoaRepository)
                .execute(new DadosNovaPreInscricao(pessoaId, encontroId));

        assertThat(resultado.entrouEmReserva()).isTrue();
        assertThat(resultado.cadastroReserva()).isNotNull();
    }

    @Test
    void shouldThrowWhenPessoaAlreadyPreInscrita() {
        UUID pessoaId = UUID.randomUUID();
        UUID encontroId = UUID.randomUUID();
        Encontro encontro = umEncontro(10);
        when(pessoaRepository.findByIdAndDeletedFalse(pessoaId)).thenReturn(Optional.of(pessoa));
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(preInscricaoRepository.existsByPessoa_IdAndEncontro_Id(any(), any())).thenReturn(true);

        assertThatThrownBy(() -> new PreInscreverUseCase(
                                preInscricaoRepository, cadastroReservaRepository, encontroRepository, pessoaRepository)
                        .execute(new DadosNovaPreInscricao(pessoaId, encontroId)))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID pessoaId = UUID.randomUUID();
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new PreInscreverUseCase(
                                preInscricaoRepository, cadastroReservaRepository, encontroRepository, pessoaRepository)
                        .execute(new DadosNovaPreInscricao(pessoaId, encontroId)))
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
                LocalDate.of(2002, 3, 10),
                "(11) 90000-0000",
                EstadoCivil.SOLTEIRO,
                paroquia,
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private Encontro umEncontro(int vagas) {
        return new Encontro(
                1, "São José", pessoa, "Sítio Teste", LocalDate.now(), LocalDate.now().plusDays(2), vagas, paroquia);
    }
}
