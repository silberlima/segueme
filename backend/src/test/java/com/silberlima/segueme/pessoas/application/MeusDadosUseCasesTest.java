package com.silberlima.segueme.pessoas.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.application.dto.DadosPessoaisEditaveis;
import com.silberlima.segueme.pessoas.domain.Endereco;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.HistoricoEventoRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** "Meus dados" do perfil Seguidor: ver, alterar e ler o próprio histórico. */
@ExtendWith(MockitoExtension.class)
class MeusDadosUseCasesTest {

    private static final String USUARIO = "sub-do-keycloak";

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private HistoricoEventoRepository historicoEventoRepository;

    private Pessoa umaPessoa() {
        Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
        return new Pessoa(
                "Maria da Silva",
                PessoasDeTeste.cpf(),
                LocalDate.of(2000, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquia,
                PessoasDeTeste.endereco(),
                "Nenhuma",
                null,
                null,
                "Contato Antigo",
                "(11) 91111-1111",
                "Mãe",
                null);
    }

    @Test
    void shouldReturnPessoaLinkedToAuthenticatedUser() {
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));

        Pessoa resultado = new ConsultarMeusDadosUseCase(pessoaRepository).execute(USUARIO);

        assertThat(resultado).isSameAs(pessoa);
    }

    @Test
    void shouldThrowWhenUserHasNoPessoaLinked() {
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarMeusDadosUseCase(pessoaRepository).execute(USUARIO))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldUpdateContactHealthAndIdentityFields() {
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));

        new AtualizarMeusDadosUseCase(pessoaRepository)
                .execute(
                        USUARIO,
                        new DadosPessoaisEditaveis(
                                "529.982.247-25",
                                LocalDate.of(1999, 12, 31),
                                "(11) 98888-8888",
                                "@maria.silva",
                                new Endereco("20040-020", "Avenida Rio Branco", "156", "Sala 8", "Centro",
                                        "Rio de Janeiro", "rj"),
                                "Amendoim",
                                "Sem lactose",
                                "Losartana",
                                "Contato Novo",
                                "(11) 97777-7777",
                                "Irmã"));

        assertThat(pessoa.getCpf()).isEqualTo("52998224725");
        assertThat(pessoa.getDataNascimento()).isEqualTo(LocalDate.of(1999, 12, 31));
        assertThat(pessoa.getTelefone()).isEqualTo("(11) 98888-8888");
        assertThat(pessoa.getInstagram()).isEqualTo("maria.silva");
        assertThat(pessoa.getEndereco().getCidade()).isEqualTo("Rio de Janeiro");
        assertThat(pessoa.getEndereco().getUf()).isEqualTo("RJ");
        assertThat(pessoa.getAlergias()).isEqualTo("Amendoim");
        assertThat(pessoa.getRestricaoAlimentar()).isEqualTo("Sem lactose");
        assertThat(pessoa.getMedicacaoUsoContinuo()).isEqualTo("Losartana");
        assertThat(pessoa.getContatoEmergenciaNome()).isEqualTo("Contato Novo");
        assertThat(pessoa.getContatoEmergenciaParentesco()).isEqualTo("Irmã");
    }

    /**
     * O que a pessoa NÃO muda sobre si: nome e vínculo. Paróquia só por Transferência aprovada
     * pelo Setorial; situação é consequência do fluxo de inscrição.
     */
    @Test
    void shouldNotTouchNameOrVinculoWhenUpdatingOwnData() {
        Pessoa pessoa = umaPessoa();
        String nomeOriginal = pessoa.getNomeCompleto();
        var paroquiaOriginal = pessoa.getParoquia();
        var situacaoOriginal = pessoa.getSituacao();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));

        new AtualizarMeusDadosUseCase(pessoaRepository).execute(USUARIO, umaEdicao(PessoasDeTeste.cpf()));

        assertThat(pessoa.getNomeCompleto()).isEqualTo(nomeOriginal);
        assertThat(pessoa.getParoquia()).isSameAs(paroquiaOriginal);
        assertThat(pessoa.getSituacao()).isEqualTo(situacaoOriginal);
    }

    /**
     * O CPF é editável, mas continua único no cadastro (RN-PESSOA-01): corrigir o próprio não pode
     * virar porta para assumir o de outra pessoa.
     */
    @Test
    void shouldRejectCpfAlreadyUsedByAnotherPessoa() {
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existsByCpfAndIdNotAndDeletedFalse("52998224725", pessoa.getId()))
                .thenReturn(true);

        var useCase = new AtualizarMeusDadosUseCase(pessoaRepository);
        var dados = umaEdicao("529.982.247-25");

        assertThatThrownBy(() -> useCase.execute(USUARIO, dados))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("CPF");
    }

    /** Manter o próprio CPF não é colisão: a checagem ignora a própria pessoa. */
    @Test
    void shouldAcceptKeepingOwnCpf() {
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));
        when(pessoaRepository.existsByCpfAndIdNotAndDeletedFalse(pessoa.getCpf(), pessoa.getId()))
                .thenReturn(false);

        new AtualizarMeusDadosUseCase(pessoaRepository).execute(USUARIO, umaEdicao(pessoa.getCpf()));

        assertThat(pessoa.getTelefone()).isEqualTo("(11) 98888-8888");
    }

    @Test
    void shouldThrowWhenUpdatingWithoutPessoaLinked() {
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.empty());

        var useCase = new AtualizarMeusDadosUseCase(pessoaRepository);
        var dados = umaEdicao(PessoasDeTeste.cpf());

        assertThatThrownBy(() -> useCase.execute(USUARIO, dados)).isInstanceOf(ResourceNotFoundException.class);
    }

    private DadosPessoaisEditaveis umaEdicao(String cpf) {
        return new DadosPessoaisEditaveis(
                cpf,
                LocalDate.of(2000, 1, 1),
                "(11) 98888-8888",
                null,
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null);
    }

    @Test
    void shouldListOwnHistory() {
        Pessoa pessoa = umaPessoa();
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.of(pessoa));
        when(historicoEventoRepository.findByPessoa_IdOrderByDataEventoAsc(pessoa.getId())).thenReturn(List.of());

        var eventos = new ListarMeuHistoricoUseCase(pessoaRepository, historicoEventoRepository).execute(USUARIO);

        assertThat(eventos).isEmpty();
    }

    @Test
    void shouldThrowWhenReadingHistoryWithoutPessoaLinked() {
        when(pessoaRepository.findByUsuarioIdAndDeletedFalse(USUARIO)).thenReturn(Optional.empty());

        assertThatThrownBy(() ->
                        new ListarMeuHistoricoUseCase(pessoaRepository, historicoEventoRepository).execute(USUARIO))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
