package com.silberlima.segueme.conteudo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.conteudo.application.dto.PalestraDoEncontro;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestra;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestra;
import com.silberlima.segueme.conteudo.domain.PalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
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
class ListarPalestrasDoEncontroUseCaseTest {

    @Mock
    private PalestraRepository palestraRepository;

    @Mock
    private DesignacaoPalestraRepository designacaoPalestraRepository;

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

    @Test
    void shouldMergeCatalogoComDesignacoesExistentes() {
        UUID encontroId = UUID.randomUUID();
        Palestra tema1 = new Palestra(1, "Tema 1");
        Palestra tema2 = new Palestra(2, "Tema 2");
        ReflectionTestUtils.setField(tema1, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(tema2, "id", UUID.randomUUID());
        when(palestraRepository.findByDeletedFalseOrderByNumeroAsc()).thenReturn(List.of(tema1, tema2));

        DesignacaoPalestra designacaoTema1 = new DesignacaoPalestra(encontro, tema1);
        Palestrante palestrante = Palestrante.externo("Convidado");
        designacaoTema1.designar(palestrante);
        when(designacaoPalestraRepository.findByEncontro_IdAndDeletedFalse(encontroId))
                .thenReturn(List.of(designacaoTema1));

        List<PalestraDoEncontro> resultado =
                new ListarPalestrasDoEncontroUseCase(palestraRepository, designacaoPalestraRepository).execute(encontroId);

        assertThat(resultado).hasSize(2);
        assertThat(resultado.get(0).palestrante()).isEqualTo(palestrante);
        assertThat(resultado.get(1).palestrante()).isNull();
    }
}
