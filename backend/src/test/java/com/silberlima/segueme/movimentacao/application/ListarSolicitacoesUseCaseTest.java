package com.silberlima.segueme.movimentacao.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.application.dto.TipoSolicitacao;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.EmprestimoRepository;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import com.silberlima.segueme.movimentacao.domain.TransferenciaRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ListarSolicitacoesUseCaseTest {

    @Mock
    private TransferenciaRepository transferenciaRepository;

    @Mock
    private EmprestimoRepository emprestimoRepository;

    @Test
    void shouldMergeBothTypesSortedByCreatedAtDesc() {
        Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
        Pessoa diretor = new Pessoa(
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
        Encontro encontro = new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
        Equipe equipe = new Equipe("Cozinha", 2, 4, null);

        Transferencia transferencia = new Transferencia(diretor, paroquia, paroquia, null);
        ReflectionTestUtils.setField(transferencia, "createdAt", java.time.Instant.now().minusSeconds(60));
        Emprestimo emprestimo = new Emprestimo(encontro, equipe, null);
        ReflectionTestUtils.setField(emprestimo, "createdAt", java.time.Instant.now());

        when(transferenciaRepository.findAll(any(Specification.class))).thenReturn(List.of(transferencia));
        when(emprestimoRepository.findAll(any(Specification.class))).thenReturn(List.of(emprestimo));

        List<SolicitacaoItem> solicitacoes = new ListarSolicitacoesUseCase(transferenciaRepository, emprestimoRepository)
                .execute(new EscopoTerritorial.Nacional());

        assertThat(solicitacoes).hasSize(2);
        assertThat(solicitacoes.get(0).tipo()).isEqualTo(TipoSolicitacao.EMPRESTIMO);
        assertThat(solicitacoes.get(1).tipo()).isEqualTo(TipoSolicitacao.TRANSFERENCIA);
    }
}
