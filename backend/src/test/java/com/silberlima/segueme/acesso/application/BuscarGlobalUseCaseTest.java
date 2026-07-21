package com.silberlima.segueme.acesso.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.acesso.domain.BuscaProvider;
import com.silberlima.segueme.acesso.domain.BuscaResultado;
import com.silberlima.segueme.acesso.domain.BuscaTipo;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class BuscarGlobalUseCaseTest {

    @Test
    void shouldReturnEmptyListWhenNoProvidersRegistered() {
        BuscarGlobalUseCase useCase = new BuscarGlobalUseCase(List.of());

        List<BuscaResultado> resultados = useCase.execute("maria", "user-1");

        assertThat(resultados).isEmpty();
    }

    @Test
    void shouldAggregateAndSortResultsFromMultipleProviders() {
        BuscaResultado encontro = new BuscaResultado(BuscaTipo.ENCONTRO, UUID.randomUUID(), "Encontro 12", "Paróquia X");
        BuscaResultado pessoaB = new BuscaResultado(BuscaTipo.PESSOA, UUID.randomUUID(), "Beatriz Souza", "Seguidora");
        BuscaResultado pessoaA = new BuscaResultado(BuscaTipo.PESSOA, UUID.randomUUID(), "Ana Lima", "Seguimista");

        BuscaProvider encontroProvider = stubProvider(BuscaTipo.ENCONTRO, List.of(encontro));
        BuscaProvider pessoaProvider = stubProvider(BuscaTipo.PESSOA, List.of(pessoaB, pessoaA));

        BuscarGlobalUseCase useCase = new BuscarGlobalUseCase(List.of(encontroProvider, pessoaProvider));

        List<BuscaResultado> resultados = useCase.execute("a", "user-1");

        assertThat(resultados).containsExactly(pessoaA, pessoaB, encontro);
    }

    private BuscaProvider stubProvider(BuscaTipo tipo, List<BuscaResultado> resultados) {
        return new BuscaProvider() {
            @Override
            public BuscaTipo tipo() {
                return tipo;
            }

            @Override
            public List<BuscaResultado> buscar(String query, String usuarioId) {
                return resultados;
            }
        };
    }
}
