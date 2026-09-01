package com.silberlima.segueme.posencontro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.ContagemPresencas;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FaixaPrioridade;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import com.silberlima.segueme.posencontro.infrastructure.PosEncontroProperties;
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
class ConsultarFrequenciaUseCaseTest {

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroPosRepository encontroPosRepository;

    @Mock
    private FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    @Mock
    private ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase;

    private final UUID paroquiaId = UUID.randomUUID();
    private final Paroquia paroquia = new Paroquia("Paróquia Teste", new Setor("Setor", new Diocese("Diocese")));

    private ConsultarFrequenciaUseCase umUseCase() {
        return new ConsultarFrequenciaUseCase(
                paroquiaRepository,
                encontroPosRepository,
                frequenciaPosEncontroRepository,
                consultarAcompanhadosUseCase,
                new PosEncontroProperties(75, 40));
    }

    @Test
    void shouldApurarTodoOPoolInclusiveQuemNuncaTevePresenca() {
        Pessoa assiduo = umaPessoa("Assíduo");
        Pessoa ausente = umaPessoa("Ausente Sempre");

        when(paroquiaRepository.existsByIdAndDeletedFalse(paroquiaId)).thenReturn(true);
        when(consultarAcompanhadosUseCase.execute(paroquiaId)).thenReturn(List.of(assiduo, ausente));
        when(encontroPosRepository.countByParoquia_IdAndDataBetweenAndDeletedFalse(eq(paroquiaId), any(), any()))
                .thenReturn(12L);
        when(frequenciaPosEncontroRepository.contarPresencasNaParoquia(eq(paroquiaId), any(), any()))
                .thenReturn(List.of(umaContagem(assiduo.getId(), 12L)));

        List<FrequenciaPessoa> frequencias = umUseCase().execute(paroquiaId, null, null);

        assertThat(frequencias).hasSize(2);
        assertThat(frequencias.get(0).percentual()).isEqualTo(100);
        assertThat(frequencias.get(0).faixa()).isEqualTo(FaixaPrioridade.ALTA);
        assertThat(frequencias.get(1).presencas()).isZero();
        assertThat(frequencias.get(1).totalEncontros()).isEqualTo(12L);
        assertThat(frequencias.get(1).faixa()).isEqualTo(FaixaPrioridade.BAIXA);
    }

    @Test
    void shouldThrowWhenParoquiaNotFound() {
        when(paroquiaRepository.existsByIdAndDeletedFalse(paroquiaId)).thenReturn(false);

        assertThatThrownBy(() -> umUseCase().execute(paroquiaId, null, null))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Pessoa umaPessoa(String nome) {
        Pessoa pessoa = new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2005, 1, 1),
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
        ReflectionTestUtils.setField(pessoa, "id", UUID.randomUUID());
        return pessoa;
    }

    private ContagemPresencas umaContagem(UUID pessoaId, long presencas) {
        return new ContagemPresencas() {
            @Override
            public UUID getPessoaId() {
                return pessoaId;
            }

            @Override
            public UUID getParoquiaId() {
                return paroquiaId;
            }

            @Override
            public Long getPresencas() {
                return presencas;
            }
        };
    }
}
