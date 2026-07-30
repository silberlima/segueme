package com.silberlima.segueme.encontros.domain;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Resolve qual é o Encontro "corrente" de uma paróquia. Os painéis das equipes (Fichas, Montagem,
 * Comando, Finanças) falam sempre de um único Encontro e não têm seletor no protótipo
 * (`dashboards-por-perfil.md` 3.5 a 3.8): o corrente é o próximo Encontro ativo que ainda não
 * terminou e, se não houver nenhum em aberto, o último realizado — assim o painel continua
 * mostrando dado real no intervalo entre dois Encontros.
 */
@Service
public class EncontroCorrenteResolver {

    private final EncontroRepository encontroRepository;

    public EncontroCorrenteResolver(EncontroRepository encontroRepository) {
        this.encontroRepository = encontroRepository;
    }

    public Optional<Encontro> resolver(UUID paroquiaId, LocalDate hoje) {
        return encontroRepository
                .findTopByParoquia_IdAndStatusAndDataTerminoGreaterThanEqualAndDeletedFalseOrderByDataInicioAsc(
                        paroquiaId, StatusEncontro.ATIVO, hoje)
                .or(() -> encontroRepository.findTopByParoquia_IdAndDeletedFalseOrderByDataInicioDesc(paroquiaId));
    }
}
