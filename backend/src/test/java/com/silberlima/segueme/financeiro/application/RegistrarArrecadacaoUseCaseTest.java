package com.silberlima.segueme.financeiro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.financeiro.domain.Campanha;
import com.silberlima.segueme.financeiro.domain.CampanhaRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RegistrarArrecadacaoUseCaseTest {

    @Mock
    private CampanhaRepository campanhaRepository;

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
    void shouldAccumulateArrecadacao() {
        UUID campanhaId = UUID.randomUUID();
        Campanha campanha = new Campanha(encontro, "Festa Junina", new BigDecimal("1000.00"));
        when(campanhaRepository.findByIdAndDeletedFalse(campanhaId)).thenReturn(Optional.of(campanha));

        Campanha resultado = new RegistrarArrecadacaoUseCase(campanhaRepository).execute(campanhaId, new BigDecimal("250.00"));

        assertThat(resultado.getValorArrecadado()).isEqualByComparingTo(new BigDecimal("250.00"));
    }

    @Test
    void shouldThrowWhenCampanhaNotFound() {
        UUID campanhaId = UUID.randomUUID();
        when(campanhaRepository.findByIdAndDeletedFalse(campanhaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new RegistrarArrecadacaoUseCase(campanhaRepository).execute(campanhaId, new BigDecimal("10.00")))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
