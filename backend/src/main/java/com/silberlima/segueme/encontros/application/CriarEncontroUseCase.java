package com.silberlima.segueme.encontros.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.application.dto.DadosNovoEncontro;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.encontros.domain.EquipeComando;
import com.silberlima.segueme.encontros.domain.EquipeComandoRepository;
import com.silberlima.segueme.encontros.domain.EquipeDirigente;
import com.silberlima.segueme.encontros.domain.EquipeDirigenteRepository;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigente;
import com.silberlima.segueme.encontros.domain.MembroEquipeDirigenteRepository;
import com.silberlima.segueme.encontros.domain.PastaDirigente;
import com.silberlima.segueme.encontros.domain.TipoMembroDirigente;
import com.silberlima.segueme.hierarquia.domain.Paroquia;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.pessoas.domain.Casal;
import com.silberlima.segueme.pessoas.domain.CasalRepository;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Orquestra os 5 passos do wizard "Criar Encontro" (RF-015 a RF-020) num único fluxo
 * transacional: cria o Encontro com o próximo número sequencial da paróquia (RN-16), monta a
 * Equipe Dirigente permitindo pastas incompletas (RN-21, validando apenas o limite máximo de 2
 * jovens + 1 casal por pasta — RN-20) e entrega o encontro pronto à Equipe Comando (RN-COMANDO-01).
 */
@Service
public class CriarEncontroUseCase {

    private final EncontroRepository encontroRepository;
    private final EquipeDirigenteRepository equipeDirigenteRepository;
    private final MembroEquipeDirigenteRepository membroEquipeDirigenteRepository;
    private final EquipeComandoRepository equipeComandoRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final PessoaRepository pessoaRepository;
    private final CasalRepository casalRepository;

    public CriarEncontroUseCase(
            EncontroRepository encontroRepository,
            EquipeDirigenteRepository equipeDirigenteRepository,
            MembroEquipeDirigenteRepository membroEquipeDirigenteRepository,
            EquipeComandoRepository equipeComandoRepository,
            ParoquiaRepository paroquiaRepository,
            PessoaRepository pessoaRepository,
            CasalRepository casalRepository) {
        this.encontroRepository = encontroRepository;
        this.equipeDirigenteRepository = equipeDirigenteRepository;
        this.membroEquipeDirigenteRepository = membroEquipeDirigenteRepository;
        this.equipeComandoRepository = equipeComandoRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.pessoaRepository = pessoaRepository;
        this.casalRepository = casalRepository;
    }

    @Transactional
    public Encontro execute(DadosNovoEncontro dados) {
        Paroquia paroquia = paroquiaRepository
                .findByIdAndDeletedFalse(dados.paroquiaId())
                .orElseThrow(() -> new ResourceNotFoundException("Paróquia não encontrada: " + dados.paroquiaId()));
        Pessoa diretorEspiritual = buscarPessoa(dados.diretorEspiritualId(), "Diretor espiritual");

        int proximoNumero = encontroRepository
                .findTopByParoquia_IdOrderByNumeroDesc(dados.paroquiaId())
                .map(anterior -> anterior.getNumero() + 1)
                .orElse(1);

        Encontro encontro = encontroRepository.save(new Encontro(
                proximoNumero,
                dados.padroeiro(),
                diretorEspiritual,
                dados.local(),
                dados.dataInicio(),
                dados.dataTermino(),
                dados.vagas(),
                paroquia));

        criarEquipeDirigente(encontro, dados.equipeDirigente());
        criarEquipeComando(encontro, dados.coordenadorGeralId(), dados.casalCoordenadorId());

        return encontro;
    }

    private void criarEquipeDirigente(Encontro encontro, List<DadosNovoEncontro.MembroDirigente> membrosInformados) {
        validarComposicao(membrosInformados);
        EquipeDirigente equipeDirigente = equipeDirigenteRepository.save(new EquipeDirigente(encontro));
        for (DadosNovoEncontro.MembroDirigente input : membrosInformados) {
            Pessoa pessoa = null;
            Casal casal = null;
            if (input.tipoMembro() == TipoMembroDirigente.JOVEM) {
                if (input.pessoaId() == null) {
                    throw new BusinessRuleViolationException(
                            "Membro do tipo JOVEM requer pessoaId (pasta " + input.pasta() + ").");
                }
                pessoa = buscarPessoa(input.pessoaId(), "Membro da Equipe Dirigente");
            } else {
                if (input.casalId() == null) {
                    throw new BusinessRuleViolationException(
                            "Membro do tipo CASAL requer casalId (pasta " + input.pasta() + ").");
                }
                casal = buscarCasal(input.casalId(), "Casal da Equipe Dirigente");
            }
            membroEquipeDirigenteRepository.save(
                    new MembroEquipeDirigente(equipeDirigente, input.pasta(), input.tipoMembro(), pessoa, casal));
        }
    }

    private void criarEquipeComando(Encontro encontro, UUID coordenadorGeralId, UUID casalCoordenadorId) {
        Pessoa coordenadorGeral = buscarPessoa(coordenadorGeralId, "Coordenador geral");
        Casal casalCoordenador = buscarCasal(casalCoordenadorId, "Casal coordenador");
        equipeComandoRepository.save(new EquipeComando(encontro, coordenadorGeral, casalCoordenador));
    }

    private void validarComposicao(List<DadosNovoEncontro.MembroDirigente> membros) {
        Map<PastaDirigente, Long> jovensPorPasta = membros.stream()
                .filter(membro -> membro.tipoMembro() == TipoMembroDirigente.JOVEM)
                .collect(Collectors.groupingBy(DadosNovoEncontro.MembroDirigente::pasta, Collectors.counting()));
        jovensPorPasta.forEach((pasta, total) -> {
            if (total > 2) {
                throw new BusinessRuleViolationException(
                        "A pasta " + pasta + " não pode ter mais de 2 jovens (RN-20).");
            }
        });

        Map<PastaDirigente, Long> casaisPorPasta = membros.stream()
                .filter(membro -> membro.tipoMembro() == TipoMembroDirigente.CASAL)
                .collect(Collectors.groupingBy(DadosNovoEncontro.MembroDirigente::pasta, Collectors.counting()));
        casaisPorPasta.forEach((pasta, total) -> {
            if (total > 1) {
                throw new BusinessRuleViolationException(
                        "A pasta " + pasta + " não pode ter mais de 1 casal (RN-20).");
            }
        });
    }

    private Pessoa buscarPessoa(UUID id, String descricao) {
        return pessoaRepository
                .findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new ResourceNotFoundException(descricao + " não encontrado(a): " + id));
    }

    private Casal buscarCasal(UUID id, String descricao) {
        return casalRepository
                .findById(id)
                .filter(casal -> !casal.isDeleted())
                .orElseThrow(() -> new ResourceNotFoundException(descricao + " não encontrado(a): " + id));
    }
}
