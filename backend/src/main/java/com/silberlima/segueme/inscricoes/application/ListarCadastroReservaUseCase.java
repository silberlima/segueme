package com.silberlima.segueme.inscricoes.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.inscricoes.domain.CadastroReserva;
import com.silberlima.segueme.inscricoes.domain.CadastroReservaRepository;
import com.silberlima.segueme.inscricoes.domain.StatusReserva;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Lista o Cadastro de Reserva da paróquia de um Encontro, em ordem de chegada (`inscricoes.md`
 * 2.3). A busca é por paróquia — não só pelo Encontro informado — para que candidatos represados
 * em Encontros anteriores da mesma paróquia apareçam com prioridade automática (RN-INSCRICAO-04).
 */
@Service
public class ListarCadastroReservaUseCase {

    private final CadastroReservaRepository cadastroReservaRepository;
    private final EncontroRepository encontroRepository;

    public ListarCadastroReservaUseCase(
            CadastroReservaRepository cadastroReservaRepository, EncontroRepository encontroRepository) {
        this.cadastroReservaRepository = cadastroReservaRepository;
        this.encontroRepository = encontroRepository;
    }

    @Transactional(readOnly = true)
    public List<CadastroReserva> execute(UUID encontroId) {
        Encontro encontro = encontroRepository
                .findByIdAndDeletedFalse(encontroId)
                .orElseThrow(() -> new ResourceNotFoundException("Encontro não encontrado: " + encontroId));
        return cadastroReservaRepository.findByEncontro_Paroquia_IdAndStatusOrderByCreatedAtAsc(
                encontro.getParoquia().getId(), StatusReserva.AGUARDANDO);
    }
}
