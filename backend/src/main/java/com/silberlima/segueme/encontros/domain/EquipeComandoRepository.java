package com.silberlima.segueme.encontros.domain;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EquipeComandoRepository extends JpaRepository<EquipeComando, UUID> {

    @EntityGraph(attributePaths = {"coordenadorGeral", "casalCoordenador", "casalCoordenador.pessoa1", "casalCoordenador.pessoa2"})
    Optional<EquipeComando> findByEncontro_Id(UUID encontroId);
}
