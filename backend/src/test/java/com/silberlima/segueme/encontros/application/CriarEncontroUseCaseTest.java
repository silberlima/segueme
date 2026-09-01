package com.silberlima.segueme.encontros.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.application.dto.DadosNovoEncontro;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.EquipeComandoRepository;
import com.silberlima.segueme.encontros.domain.EquipeDirigente;
import com.silberlima.segueme.encontros.domain.EquipeDirigenteRepository;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigenteRepository;
import com.silberlima.segueme.encontros.domain.PastaDirigente;
import com.silberlima.segueme.encontros.domain.TipoMembroDirigente;
import com.silberlima.segueme.hierarquia.domain.Diocese;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Setor;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.EstadoCivil;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
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
class CriarEncontroUseCaseTest {

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private EquipeDirigenteRepository equipeDirigenteRepository;

    @Mock
    private MembroEquipeDirigenteRepository membroEquipeDirigenteRepository;

    @Mock
    private EquipeComandoRepository equipeComandoRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private PessoaRepository pessoaRepository;

    @Mock
    private CasalRepository casalRepository;

    private final Paroquia paroquia = umaParoquia();
    private final Pessoa diretorEspiritual = umaPessoa("Padre Teste");
    private final Pessoa jovem = umaPessoa("Jovem Teste");
    private final Pessoa coordenadorGeral = umaPessoa("Coordenador Geral");
    private final Casal casal = new Casal(umaPessoa("Pessoa 1"), umaPessoa("Pessoa 2"), LocalDate.of(2020, 1, 1));

    @Test
    void shouldCreateEncontroWithFirstNumeroAndDeliverToComando() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(pessoaRepository.findByIdAndDeletedFalse(coordenadorId)).thenReturn(Optional.of(coordenadorGeral));
        when(casalRepository.findById(casalId)).thenReturn(Optional.of(casal));
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.empty());
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipeDirigenteRepository.save(any(EquipeDirigente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(),
                coordenadorId,
                casalId);

        Encontro encontro = new CriarEncontroUseCase(
                        encontroRepository,
                        equipeDirigenteRepository,
                        membroEquipeDirigenteRepository,
                        equipeComandoRepository,
                        paroquiaRepository,
                        pessoaRepository,
                        casalRepository)
                .execute(dados);

        assertThat(encontro.getNumero()).isEqualTo(1);
        assertThat(encontro.getPadroeiro()).isEqualTo("São José");
        assertThat(encontro.getStatus().name()).isEqualTo("ATIVO");
    }

    @Test
    void shouldIncrementNumeroSequencialPerParoquia() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        Encontro anterior = new Encontro(
                5, "Encontro Anterior", diretorEspiritual, "Local", LocalDate.now(), LocalDate.now(), 30, paroquia);
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(pessoaRepository.findByIdAndDeletedFalse(coordenadorId)).thenReturn(Optional.of(coordenadorGeral));
        when(casalRepository.findById(casalId)).thenReturn(Optional.of(casal));
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.of(anterior));
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipeDirigenteRepository.save(any(EquipeDirigente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(),
                coordenadorId,
                casalId);

        Encontro encontro = new CriarEncontroUseCase(
                        encontroRepository,
                        equipeDirigenteRepository,
                        membroEquipeDirigenteRepository,
                        equipeComandoRepository,
                        paroquiaRepository,
                        pessoaRepository,
                        casalRepository)
                .execute(dados);

        assertThat(encontro.getNumero()).isEqualTo(6);
    }

    @Test
    void shouldAllowIncompletePastaDirigente() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        UUID jovemId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(pessoaRepository.findByIdAndDeletedFalse(coordenadorId)).thenReturn(Optional.of(coordenadorGeral));
        when(pessoaRepository.findByIdAndDeletedFalse(jovemId)).thenReturn(Optional.of(jovem));
        when(casalRepository.findById(casalId)).thenReturn(Optional.of(casal));
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.empty());
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipeDirigenteRepository.save(any(EquipeDirigente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(new DadosNovoEncontro.MembroDirigente(
                        PastaDirigente.FINANCAS, TipoMembroDirigente.JOVEM, jovemId, null)),
                coordenadorId,
                casalId);

        Encontro encontro = new CriarEncontroUseCase(
                        encontroRepository,
                        equipeDirigenteRepository,
                        membroEquipeDirigenteRepository,
                        equipeComandoRepository,
                        paroquiaRepository,
                        pessoaRepository,
                        casalRepository)
                .execute(dados);

        assertThat(encontro).isNotNull();
    }

    @Test
    void shouldThrowWhenMoreThanTwoJovensInSamePasta() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.empty());
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(
                        new DadosNovoEncontro.MembroDirigente(
                                PastaDirigente.FINANCAS, TipoMembroDirigente.JOVEM, UUID.randomUUID(), null),
                        new DadosNovoEncontro.MembroDirigente(
                                PastaDirigente.FINANCAS, TipoMembroDirigente.JOVEM, UUID.randomUUID(), null),
                        new DadosNovoEncontro.MembroDirigente(
                                PastaDirigente.FINANCAS, TipoMembroDirigente.JOVEM, UUID.randomUUID(), null)),
                coordenadorId,
                casalId);

        assertThatThrownBy(() -> new CriarEncontroUseCase(
                                encontroRepository,
                                equipeDirigenteRepository,
                                membroEquipeDirigenteRepository,
                                equipeComandoRepository,
                                paroquiaRepository,
                                pessoaRepository,
                                casalRepository)
                        .execute(dados))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenMoreThanOneCasalInSamePasta() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.empty());
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(
                        new DadosNovoEncontro.MembroDirigente(
                                PastaDirigente.FICHAS, TipoMembroDirigente.CASAL, null, UUID.randomUUID()),
                        new DadosNovoEncontro.MembroDirigente(
                                PastaDirigente.FICHAS, TipoMembroDirigente.CASAL, null, UUID.randomUUID())),
                coordenadorId,
                casalId);

        assertThatThrownBy(() -> new CriarEncontroUseCase(
                                encontroRepository,
                                equipeDirigenteRepository,
                                membroEquipeDirigenteRepository,
                                equipeComandoRepository,
                                paroquiaRepository,
                                pessoaRepository,
                                casalRepository)
                        .execute(dados))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenParoquiaNotFound() {
        UUID paroquiaId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.empty());

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                UUID.randomUUID(),
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(),
                UUID.randomUUID(),
                UUID.randomUUID());

        assertThatThrownBy(() -> new CriarEncontroUseCase(
                                encontroRepository,
                                equipeDirigenteRepository,
                                membroEquipeDirigenteRepository,
                                equipeComandoRepository,
                                paroquiaRepository,
                                pessoaRepository,
                                casalRepository)
                        .execute(dados))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    @Test
    void shouldThrowWhenCasalCoordenadorNotFound() {
        UUID paroquiaId = UUID.randomUUID();
        UUID diretorId = UUID.randomUUID();
        UUID coordenadorId = UUID.randomUUID();
        UUID casalId = UUID.randomUUID();
        when(paroquiaRepository.findByIdAndDeletedFalse(paroquiaId)).thenReturn(Optional.of(paroquia));
        when(pessoaRepository.findByIdAndDeletedFalse(diretorId)).thenReturn(Optional.of(diretorEspiritual));
        when(pessoaRepository.findByIdAndDeletedFalse(coordenadorId)).thenReturn(Optional.of(coordenadorGeral));
        when(casalRepository.findById(casalId)).thenReturn(Optional.empty());
        when(encontroRepository.findTopByParoquia_IdOrderByNumeroDesc(paroquiaId)).thenReturn(Optional.empty());
        when(encontroRepository.save(any(Encontro.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(equipeDirigenteRepository.save(any(EquipeDirigente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DadosNovoEncontro dados = new DadosNovoEncontro(
                "São José",
                diretorId,
                "Sítio Bom Retiro",
                LocalDate.now().plusDays(30),
                LocalDate.now().plusDays(32),
                40,
                paroquiaId,
                List.of(),
                coordenadorId,
                casalId);

        assertThatThrownBy(() -> new CriarEncontroUseCase(
                                encontroRepository,
                                equipeDirigenteRepository,
                                membroEquipeDirigenteRepository,
                                equipeComandoRepository,
                                paroquiaRepository,
                                pessoaRepository,
                                casalRepository)
                        .execute(dados))
                .isInstanceOf(ResourceNotFoundException.class);
    }

    private Paroquia umaParoquia() {
        Diocese diocese = new Diocese("Diocese Teste");
        Setor setor = new Setor("Setor Teste", diocese);
        return new Paroquia("Paróquia Teste", setor);
    }

    private Pessoa umaPessoa(String nome) {
        return new Pessoa(
                nome,
                PessoasDeTeste.cpf(),
                LocalDate.of(1999, 5, 20),
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
