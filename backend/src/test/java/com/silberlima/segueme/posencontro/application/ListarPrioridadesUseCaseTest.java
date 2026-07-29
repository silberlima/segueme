package com.silberlima.segueme.posencontro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/** Cobre o ranking de prioridade (RF-059, RN-POS-02/03): maior frequência primeiro, empate por nome. */
@ExtendWith(MockitoExtension.class)
class ListarPrioridadesUseCaseTest {

    @Mock
    private ConsultarFrequenciaUseCase consultarFrequenciaUseCase;

    @InjectMocks
    private ListarPrioridadesUseCase listarPrioridadesUseCase;

    private final ReguaPrioridade regua = new ReguaPrioridade(75, 40);
    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));

    @Test
    void shouldOrdenarPelaMaiorFrequencia() {
        UUID paroquiaId = UUID.randomUUID();
        when(consultarFrequenciaUseCase.execute(eq(paroquiaId), isNull(), isNull()))
                .thenReturn(List.of(
                        FrequenciaPessoa.apurar(umaPessoa("Zeca Pouco Assíduo"), 10, 2, regua),
                        FrequenciaPessoa.apurar(umaPessoa("Ana Assídua"), 10, 10, regua),
                        FrequenciaPessoa.apurar(umaPessoa("Bruno Assíduo"), 10, 10, regua)));

        List<FrequenciaPessoa> ranking = listarPrioridadesUseCase.execute(paroquiaId);

        assertThat(ranking)
                .extracting(frequencia -> frequencia.pessoa().getNomeCompleto())
                .containsExactly("Ana Assídua", "Bruno Assíduo", "Zeca Pouco Assíduo");
    }

    private Pessoa umaPessoa(String nome) {
        return new Pessoa(
                nome,
                LocalDate.of(2005, 1, 1),
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
}
