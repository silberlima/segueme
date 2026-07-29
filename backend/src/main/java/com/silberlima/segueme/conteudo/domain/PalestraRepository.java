package com.silberlima.segueme.conteudo.domain;

import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PalestraRepository extends JpaRepository<Palestra, UUID> {

    List<Palestra> findByDeletedFalseOrderByNumeroAsc();
}
