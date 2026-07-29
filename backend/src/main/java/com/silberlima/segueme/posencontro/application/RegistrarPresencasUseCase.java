package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.posencontro.application.dto.PresencaPessoa;
import com.silberlima.segueme.posencontro.application.dto.PresencaRegistro;
import com.silberlima.segueme.posencontro.domain.EncontroPos;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontro;
import com.silberlima.segueme.posencontro.domain.FrequenciaPosEncontroRepository;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registra em lote a chamada de um encontro pós (RF-058, UC — Registrar presença): a tela envia a
 * lista inteira de marcações e cada pessoa tem no máximo um registro por encontro pós — remarcar
 * atualiza o registro existente (RN-POS-01).
 *
 * <p>Só quem é acompanhado naquela paróquia pode ser marcado: presença em encontro pós de outra
 * paróquia não é prevista em nenhum RN, e aceitá-la distorceria o denominador da régua de prioridade.
 */
@Service
public class RegistrarPresencasUseCase {

    private static final Logger log = LoggerFactory.getLogger(RegistrarPresencasUseCase.class);

    private final EncontroPosRepository encontroPosRepository;
    private final FrequenciaPosEncontroRepository frequenciaPosEncontroRepository;
    private final PessoaRepository pessoaRepository;
    private final ConsultarPresencasUseCase consultarPresencasUseCase;

    public RegistrarPresencasUseCase(
            EncontroPosRepository encontroPosRepository,
            FrequenciaPosEncontroRepository frequenciaPosEncontroRepository,
            PessoaRepository pessoaRepository,
            ConsultarPresencasUseCase consultarPresencasUseCase) {
        this.encontroPosRepository = encontroPosRepository;
        this.frequenciaPosEncontroRepository = frequenciaPosEncontroRepository;
        this.pessoaRepository = pessoaRepository;
        this.consultarPresencasUseCase = consultarPresencasUseCase;
    }

    @Transactional
    public List<PresencaPessoa> execute(UUID encontroPosId, List<PresencaRegistro> registros) {
        EncontroPos encontroPos = encontroPosRepository
                .findByIdAndDeletedFalse(encontroPosId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro pós não encontrado: " + encontroPosId));

        for (PresencaRegistro registro : registros) {
            registrar(encontroPos, registro);
        }

        log.info("Registradas {} presenças no encontro pós {} de {}", registros.size(), encontroPosId,
                encontroPos.getData());

        return consultarPresencasUseCase.execute(encontroPosId);
    }

    private void registrar(EncontroPos encontroPos, PresencaRegistro registro) {
        Pessoa pessoa = pessoaRepository
                .findByIdAndDeletedFalse(registro.pessoaId())
                .orElseThrow(() -> new ResourceNotFoundException("Pessoa não encontrada: " + registro.pessoaId()));

        if (!Objects.equals(pessoa.getParoquia().getId(), encontroPos.getParoquia().getId())) {
            throw new BusinessRuleViolationException(
                    "A pessoa " + pessoa.getNomeCompleto() + " não é acompanhada na paróquia deste encontro pós.");
        }

        FrequenciaPosEncontro frequencia = frequenciaPosEncontroRepository
                .findByEncontroPos_IdAndPessoa_IdAndDeletedFalse(encontroPos.getId(), pessoa.getId())
                .orElseGet(() -> new FrequenciaPosEncontro(encontroPos, pessoa, registro.presente()));
        frequencia.registrar(registro.presente());

        frequenciaPosEncontroRepository.save(frequencia);
    }
}
