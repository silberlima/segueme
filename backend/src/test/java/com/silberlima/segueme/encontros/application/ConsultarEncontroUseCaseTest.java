package com.silberlima.segueme.encontros.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.application.dto.EncontroDetalhado;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.EquipeComando;
import com.silberlima.segueme.encontros.domain.EquipeComandoRepository;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigenteRepository;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ConsultarEncontroUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private MembroEquipeDirigenteRepository membroEquipeDirigenteRepository;

    @Mock
    private EquipeComandoRepository equipeComandoRepository;

    @Test
    void shouldReturnEncontroDetalhadoWhenFound() {
        UUID id = UUID.randomUUID();
        Encontro encontro = umEncontro();
        EquipeComando equipeComando = new EquipeComando(encontro, umaPessoa(), umCasal());
        when(encontroRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(encontro));
        when(membroEquipeDirigenteRepository.findByEquipeDirigente_Encontro_Id(id)).thenReturn(List.of());
        when(equipeComandoRepository.findByEncontro_Id(id)).thenReturn(Optional.of(equipeComando));

        EncontroDetalhado resultado = new ConsultarEncontroUseCase(
                        encontroRepository, membroEquipeDirigenteRepository, equipeComandoRepository)
                .execute(id);

        assertThat(resultado.encontro()).isSameAs(encontro);
        assertThat(resultado.equipeComando()).isSameAs(equipeComando);
    }

    @Test
    void shouldThrowWhenEncontroNotFound() {
        UUID id = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarEncontroUseCase(
                                encontroRepository, membroEquipeDirigenteRepository, equipeComandoRepository)
                        .execute(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenEquipeComandoNotFound() {
        UUID id = UUID.randomUUID();
        when(encontroRepository.findByIdAndDeletedFalse(id)).thenReturn(Optional.of(umEncontro()));
        when(membroEquipeDirigenteRepository.findByEquipeDirigente_Encontro_Id(id)).thenReturn(List.of());
        when(equipeComandoRepository.findByEncontro_Id(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> new ConsultarEncontroUseCase(
                                encontroRepository, membroEquipeDirigenteRepository, equipeComandoRepository)
                        .execute(id))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Encontro umEncontro() {
        Paroquia paroquia = umaParoquia();
        return new Encontro(
                1, "São José", umaPessoa(), "Sítio Bom Retiro", LocalDate.now(), LocalDate.now().plusDays(2), 40, paroquia);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }

    private Pessoa umaPessoa() {
        return new Pessoa(
                "Pessoa Teste",
                PessoasDeTeste.cpf(),
                LocalDate.of(1999, 5, 20),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                umaParoquia(),
                PessoasDeTeste.endereco(),
                null,
                null,
                null,
                null,
                null,
                null,
                null);
    }

    private Casal umCasal() {
        return new Casal(umaPessoa(), umaPessoa(), LocalDate.of(2020, 1, 1));
    }
}
