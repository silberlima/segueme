package com.silberlima.segueme.convites.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.montagem.domain.Equipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipe;
import com.silberlima.segueme.montagem.domain.IntegranteEquipeRepository;
import com.silberlima.segueme.montagem.domain.PapelIntegrante;
import com.silberlima.segueme.montagem.domain.StatusIntegrante;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class ListarConvitesUseCaseTest {

    @Mock
    private IntegranteEquipeRepository integranteEquipeRepository;

    @Test
    void shouldFilterByEquipeAndPapelInMemory() {
        UUID encontroId = UUID.randomUUID();
        UUID equipeAlvoId = UUID.randomUUID();
        Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor Teste", new Diocese("Diocese Teste")));
        Pessoa diretor = umaPessoa("Diretor", paroquia);
        Encontro encontro = new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);

        Equipe equipeAlvo = new Equipe("Cozinha", 2, 4, null);
        ReflectionTestUtils.setField(equipeAlvo, "id", equipeAlvoId);
        Equipe outraEquipe = new Equipe("Som", 2, 4, null);
        ReflectionTestUtils.setField(outraEquipe, "id", UUID.randomUUID());

        IntegranteEquipe coordenadorAlvo = new IntegranteEquipe(encontro, equipeAlvo, umaPessoa("A", paroquia), PapelIntegrante.COORDENADOR);
        IntegranteEquipe integranteAlvo = new IntegranteEquipe(encontro, equipeAlvo, umaPessoa("B", paroquia), PapelIntegrante.INTEGRANTE);
        IntegranteEquipe deOutraEquipe = new IntegranteEquipe(encontro, outraEquipe, umaPessoa("C", paroquia), PapelIntegrante.INTEGRANTE);

        when(integranteEquipeRepository.findByEncontro_Id(encontroId))
                .thenReturn(List.of(coordenadorAlvo, integranteAlvo, deOutraEquipe));

        List<IntegranteEquipe> resultado = new ListarConvitesUseCase(integranteEquipeRepository)
                .execute(encontroId, null, equipeAlvoId, PapelIntegrante.COORDENADOR);

        assertThat(resultado).containsExactly(coordenadorAlvo);
    }

    @Test
    void shouldDelegateToStatusQueryWhenStatusInformado() {
        UUID encontroId = UUID.randomUUID();
        when(integranteEquipeRepository.findByEncontro_IdAndStatus(encontroId, StatusIntegrante.RECUSADO))
                .thenReturn(List.of());

        List<IntegranteEquipe> resultado = new ListarConvitesUseCase(integranteEquipeRepository)
                .execute(encontroId, StatusIntegrante.RECUSADO, null, null);

        assertThat(resultado).isEmpty();
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquia) {
        return new Pessoa(
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
    }
}
