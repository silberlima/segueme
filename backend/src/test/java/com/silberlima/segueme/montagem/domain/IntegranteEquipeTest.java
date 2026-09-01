package com.silberlima.segueme.montagem.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

/** Cobre o ciclo de convite (Fase 7, RN-CONVITE-01/02) sobre {@link IntegranteEquipe}. */
class IntegranteEquipeTest {

    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor Teste", new Diocese("Diocese Teste")));
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
    private final Pessoa pessoa = new Pessoa(
            "Candidato",
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

    @Test
    void shouldStartPendenteWithDataEnvioSet() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.COORDENADOR);

        assertThat(convite.getStatus()).isEqualTo(StatusIntegrante.PENDENTE);
        assertThat(convite.getDataEnvioConvite()).isNotNull();
        assertThat(convite.getDataResposta()).isNull();
        assertThat(convite.getReenvios()).isZero();
    }

    @Test
    void shouldAcceptPendingInvite() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.COORDENADOR);

        convite.aceitar();

        assertThat(convite.getStatus()).isEqualTo(StatusIntegrante.ACEITO);
        assertThat(convite.getDataResposta()).isNotNull();
    }

    @Test
    void shouldNotAcceptTwice() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.COORDENADOR);
        convite.aceitar();

        assertThatThrownBy(convite::aceitar).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldRefusePendingInviteWithJustificativa() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.INTEGRANTE);

        convite.recusar("Não posso participar deste encontro.");

        assertThat(convite.getStatus()).isEqualTo(StatusIntegrante.RECUSADO);
        assertThat(convite.getJustificativaRecusa()).isEqualTo("Não posso participar deste encontro.");
        assertThat(convite.getDataResposta()).isNotNull();
    }

    @Test
    void shouldRequireJustificativaToRefuse() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.INTEGRANTE);

        assertThatThrownBy(() -> convite.recusar("  ")).isInstanceOf(BusinessRuleViolationException.class);
        assertThatThrownBy(() -> convite.recusar(null)).isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldResendPendingInviteAndIncrementCounter() throws InterruptedException {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.INTEGRANTE);
        var dataEnvioOriginal = convite.getDataEnvioConvite();
        Thread.sleep(5);

        convite.reenviarConvite();

        assertThat(convite.getReenvios()).isEqualTo(1);
        assertThat(convite.getDataEnvioConvite()).isAfter(dataEnvioOriginal);
    }

    @Test
    void shouldNotResendAlreadyRespondedInvite() {
        IntegranteEquipe convite = new IntegranteEquipe(encontro, equipe, pessoa, PapelIntegrante.INTEGRANTE);
        convite.recusar("Não posso.");

        assertThatThrownBy(convite::reenviarConvite).isInstanceOf(BusinessRuleViolationException.class);
    }
}
