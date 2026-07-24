package com.silberlima.segueme.inscricoes.domain;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CadastroReservaRepository extends JpaRepository<CadastroReserva, UUID> {

    /**
     * Fila de reserva de toda a paróquia (não só do Encontro informado), ordenada por chegada:
     * dá prioridade automática a candidatos represados em Encontros anteriores da mesma
     * paróquia (RN-INSCRICAO-04), sem precisar de um campo de "encontro de destino".
     */
    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia"})
    List<CadastroReserva> findByEncontro_Paroquia_IdAndStatusOrderByCreatedAtAsc(
            UUID paroquiaId, StatusReserva status);

    long countByEncontro_Paroquia_IdAndStatus(UUID paroquiaId, StatusReserva status);

    @EntityGraph(attributePaths = {"pessoa", "pessoa.paroquia", "encontro"})
    Optional<CadastroReserva> findByIdAndDeletedFalse(UUID id);
}
