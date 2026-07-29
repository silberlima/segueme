package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.posencontro.application.dto.FrequenciaPessoa;
import com.silberlima.segueme.posencontro.domain.ContagemPresencas;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import com.silberlima.segueme.posencontro.domain.JanelaApuracao;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
import com.silberlima.segueme.posencontro.infrastructure.PosEncontroProperties;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Frequência de cada acompanhado de uma paróquia numa janela de datas (RF-059, tela Frequência),
 * ordenada por nome. Apura sobre o pool inteiro: quem nunca teve presença registrada aparece com 0.
 */
@Service
public class ConsultarFrequenciaUseCase {

    private final ParoquiaRepository paroquiaRepository;
    private final EncontroPosRepository encontroPosRepository;
    private final FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;
    private final ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase;
    private final PosEncontroProperties posEncontroProperties;

    public ConsultarFrequenciaUseCase(
            ParoquiaRepository paroquiaRepository,
            EncontroPosRepository encontroPosRepository,
            FrequenciaPosEncontroRepository frequenciaPosEncontroRepository,
            ConsultarAcompanhadosUseCase consultarAcompanhadosUseCase,
            PosEncontroProperties posEncontroProperties) {
        this.paroquiaRepository = paroquiaRepository;
        this.encontroPosRepository = encontroPosRepository;
        this.frequenciaPosEncontroRepository = frequenciaPosEncontroRepository;
        this.consultarAcompanhadosUseCase = consultarAcompanhadosUseCase;
        this.posEncontroProperties = posEncontroProperties;
    }

    @Transactional(readOnly = true)
    public List<FrequenciaPessoa> execute(UUID paroquiaId, LocalDate de, LocalDate ate) {
        if (!paroquiaRepository.existsByIdAndDeletedFalse(paroquiaId)) {
            throw new ResourceNotFoundException("Paróquia não encontrada: " + paroquiaId);
        }

        JanelaApuracao janela = JanelaApuracao.normalizar(de, ate);
        ReguaPrioridade regua = posEncontroProperties.regua();

        long totalEncontros = encontroPosRepository.countByParoquia_IdAndDataBetweenAndDeletedFalse(
                paroquiaId, janela.de(), janela.ate());

        Map<UUID, Long> presencasPorPessoa = frequenciaPosEncontroRepository
                .contarPresencasNaParoquia(paroquiaId, janela.de(), janela.ate())
                .stream()
                .collect(Collectors.toMap(ContagemPresencas::getPessoaId, ContagemPresencas::getPresencas));

        return consultarAcompanhadosUseCase.execute(paroquiaId).stream()
                .map(pessoa -> FrequenciaPessoa.apurar(
                        pessoa, totalEncontros, presencasPorPessoa.getOrDefault(pessoa.getId(), 0L), regua))
                .toList();
    }
}
