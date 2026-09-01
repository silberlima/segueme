package com.silberlima.segueme.conteudo.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.conteudo.domain.DesignacaoPalestra;
import com.silberlima.segueme.conteudo.domain.DesignacaoPalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestra;
import com.silberlima.segueme.conteudo.domain.PalestraRepository;
import com.silberlima.segueme.conteudo.domain.Palestrante;
import com.silberlima.segueme.conteudo.domain.PalestranteRepository;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
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
class DesignarPalestranteUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private PalestraRepository palestraRepository;

    @Mock
    private PalestranteRepository palestranteRepository;

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
    private final Palestra palestra = new Palestra(1, "Tema 1");
    private final Palestrante palestrante = Palestrante.externo("Convidado");

    private DesignarPalestranteUseCase useCase() {
        return new DesignarPalestranteUseCase(
                encontroRepository, palestraRepository, palestranteRepository, designacaoPalestraRepository);
    }

    @Test
    void shouldCreateDesignacaoWhenNoneExists() {
        UUID encontroId = UUID.randomUUID();
        UUID palestraId = UUID.randomUUID();
        UUID palestranteId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(palestraRepository.findById(palestraId)).thenReturn(Optional.of(palestra));
        when(palestranteRepository.findByIdAndDeletedFalse(palestranteId)).thenReturn(Optional.of(palestrante));
        when(designacaoPalestraRepository.findByEncontro_IdAndPalestra_IdAndDeletedFalse(encontroId, palestraId))
                .thenReturn(Optional.empty());
        when(designacaoPalestraRepository.save(any(DesignacaoPalestra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DesignacaoPalestra resultado = useCase().execute(encontroId, palestraId, palestranteId);

        assertThat(resultado.getPalestrante()).isEqualTo(palestrante);
        assertThat(resultado.getEncontro()).isEqualTo(encontro);
    }

    @Test
    void shouldReplacePalestranteOfExistingDesignacao() {
        UUID encontroId = UUID.randomUUID();
        UUID palestraId = UUID.randomUUID();
        UUID palestranteId = UUID.randomUUID();
        DesignacaoPalestra existente = new DesignacaoPalestra(encontro, palestra);
        existente.designar(Palestrante.externo("Antigo"));

        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(palestraRepository.findById(palestraId)).thenReturn(Optional.of(palestra));
        when(palestranteRepository.findByIdAndDeletedFalse(palestranteId)).thenReturn(Optional.of(palestrante));
        when(designacaoPalestraRepository.findByEncontro_IdAndPalestra_IdAndDeletedFalse(encontroId, palestraId))
                .thenReturn(Optional.of(existente));
        when(designacaoPalestraRepository.save(any(DesignacaoPalestra.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DesignacaoPalestra resultado = useCase().execute(encontroId, palestraId, palestranteId);

        assertThat(resultado).isSameAs(existente);
        assertThat(resultado.getPalestrante()).isEqualTo(palestrante);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> useCase().execute(encontroId, UUID.randomUUID(), UUID.randomUUID()))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
