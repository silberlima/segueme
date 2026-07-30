package com.silberlima.segueme.relatorios.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

/**
 * Percorre uma listagem paginada até o fim, em lotes, para os relatórios que cobrem um escopo
 * inteiro. Existe porque os use cases reaproveitados (Pessoas, Encontros) são paginados por
 * exigência da API (skills/03) — e um relatório não pode sair truncado na primeira página nem
 * carregar o escopo inteiro numa consulta só.
 */
public final class VarreduraPaginada {

    private static final int TAMANHO_DO_LOTE = 500;

    private VarreduraPaginada() {
    }

    public static <T> List<T> coletar(Function<Pageable, Page<T>> consulta) {
        List<T> todos = new ArrayList<>();
        Page<T> pagina;
        int numero = 0;
        do {
            pagina = consulta.apply(PageRequest.of(numero, TAMANHO_DO_LOTE));
            todos.addAll(pagina.getContent());
            numero++;
        } while (pagina.hasNext());
        return todos;
    }
}
