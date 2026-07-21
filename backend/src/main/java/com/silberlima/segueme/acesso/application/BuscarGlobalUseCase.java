package com.silberlima.segueme.acesso.application;

import com.silberlima.segueme.acesso.domain.BuscaProvider;
import com.silberlima.segueme.acesso.domain.BuscaResultado;
import java.util.Comparator;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Agrega os resultados de todos os {@link BuscaProvider} registrados (RN-ACESSO-01).
 * Enquanto Pessoa (Fase 3) e Encontro (Fase 4) não existem, a lista de providers
 * injetada está vazia e a busca sempre retorna uma lista vazia.
 */
@Service
public class BuscarGlobalUseCase {

    private static final int LIMITE_RESULTADOS = 20;

    private final List<BuscaProvider> providers;

    public BuscarGlobalUseCase(List<BuscaProvider> providers) {
        this.providers = providers;
    }

    public List<BuscaResultado> execute(String query, String usuarioId) {
        return providers.stream()
                .flatMap(provider -> provider.buscar(query, usuarioId).stream())
                .sorted(Comparator.comparing(BuscaResultado::tipo).thenComparing(BuscaResultado::titulo))
                .limit(LIMITE_RESULTADOS)
                .toList();
    }
}
