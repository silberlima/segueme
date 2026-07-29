package com.silberlima.segueme.financeiro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CriarCampanhaUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private CampanhaRepository campanhaRepository;

    @Test
    void shouldCreateAtivaCampanha() {
        UUID encontroId = UUID.randomUUID();
        Paroquia paroquia = new Paroquia("Paróquia", new Setor("Setor", new Diocese("Diocese")));
        Pessoa diretor = new Pessoa(
                "Diretor", LocalDate.of(1980, 1, 1), "(11) 90000-0000", EstadoCivil.SOLTEIRO, paroquia,
                null, null, null, null, null, null, null);
        Encontro encontro = new Encontro(1, "São José", diretor, "Local", LocalDate.now(), LocalDate.now(), 40, paroquia);
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.of(encontro));
        when(campanhaRepository.save(any(Campanha.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Campanha campanha = new CriarCampanhaUseCase(encontroRepository, campanhaRepository)
                .execute(encontroId, "Festa Junina", new BigDecimal("1000.00"));

        assertThat(campanha.getNome()).isEqualTo("Festa Junina");
        assertThat(campanha.getEncontro()).isEqualTo(encontro);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID encontroId = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(encontroId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new CriarCampanhaUseCase(encontroRepository, campanhaRepository)
                        .execute(encontroId, "Festa Junina", new BigDecimal("1000.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
