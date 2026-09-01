package com.silberlima.segueme.posencontro.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.posencontro.application.dto.PresencaRegistro;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontro;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import com.silberlima.segueme.support.PessoasDeTeste;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class RegistrarPresencasUseCaseTest {

    @Mock
    private EncontroPosRepository encontroPosRepository;

    @Mock
    private FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private ConsultarPresencasUseCase consultarPresencasUseCase;

    @InjectMocks
    private RegistrarPresencasUseCase registrarPresencasUseCase;

    private final UUID encontroPosId = UUID.randomUUID();
    private final Paroquia paroquia = umaParoquia("Paróquia do Encontro Pós");
    private final EncontroPos encontroPos = umEncontroPos();

    @Test
    void shouldRemarcarPresencaJaRegistradaEmVezDeDuplicar() {
        Pessoa pessoa = umaPessoa("Jovem Acompanhado", paroquia);
        FrequenciaPosEncontro existente = new FrequenciaPosEncontro(encontroPos, pessoa, false);

        when(encontroPosRepository.findByIdAndDeletedFalse(encontroPosId)).thenReturn(Optional.of(encontroPos));
        when(pessoaRepository.findByIdAndDeletedFalse(pessoa.getId())).thenReturn(Optional.of(pessoa));
        when(frequenciaPosEncontroRepository.findByEncontroPos_IdAndPessoa_IdAndDeletedFalse(
                        encontroPosId, pessoa.getId()))
                .thenReturn(Optional.of(existente));
        when(consultarPresencasUseCase.execute(encontroPosId)).thenReturn(List.of());

        registrarPresencasUseCase.execute(encontroPosId, List.of(new PresencaRegistro(pessoa.getId(), true)));

        ArgumentCaptor<FrequenciaPosEncontro> salvo = ArgumentCaptor.forClass(FrequenciaPosEncontro.class);
        verify(frequenciaPosEncontroRepository).save(salvo.capture());
        assertThat(salvo.getValue()).isSameAs(existente);
        assertThat(salvo.getValue().isPresente()).isTrue();
    }

    @Test
    void shouldRejeitarPessoaDeOutraParoquia() {
        Pessoa forasteiro = umaPessoa("Forasteiro", umaParoquia("Outra Paróquia"));

        when(encontroPosRepository.findByIdAndDeletedFalse(encontroPosId)).thenReturn(Optional.of(encontroPos));
        when(pessoaRepository.findByIdAndDeletedFalse(forasteiro.getId())).thenReturn(Optional.of(forasteiro));

        assertThatThrownBy(() -> registrarPresencasUseCase.execute(
                        encontroPosId, List.of(new PresencaRegistro(forasteiro.getId(), true))))
                .isInstanceOf(BusinessRuleViolationException.class);
        verify(frequenciaPosEncontroRepository, never()).save(any());
    }

    @Test
    void shouldThrowWhenEncontroPosNotFound() {
        when(encontroPosRepository.findByIdAndDeletedFalse(encontroPosId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> registrarPresencasUseCase.execute(
                        encontroPosId, List.of(new PresencaRegistro(UUID.randomUUID(), true))))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Paroquia umaParoquia(String nome) {
        Paroquia novaParoquia = new Paroquia(nome, new Setor("Setor " + nome, new Diocese("Diocese " + nome)));
        ReflectionTestUtils.setField(novaParoquia, "id", UUID.randomUUID());
        return novaParoquia;
    }

    private EncontroPos umEncontroPos() {
        EncontroPos novoEncontroPos = new EncontroPos(paroquia, LocalDate.now(), null);
        ReflectionTestUtils.setField(novoEncontroPos, "id", encontroPosId);
        return novoEncontroPos;
    }

    private Pessoa umaPessoa(String nome, Paroquia paroquiaDaPessoa) {
        Pessoa pessoa = new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(2005, 1, 1),
                "(11) 90000-0000",
                null,
                EstadoCivil.SOLTEIRO,
                paroquiaDaPessoa,
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
}
