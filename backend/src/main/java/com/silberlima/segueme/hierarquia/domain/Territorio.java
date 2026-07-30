package com.silberlima.segueme.hierarquia.domain;

import java.util.UUID;

/**
 * Coordenadas territoriais de um registro na hierarquia Nacional → Diocese → Setor → Paróquia
 * (RN-04): a paróquia a que ele pertence e os níveis acima dela.
 *
 * <p>Existe para que a checagem de escopo (RNF-06) não precise carregar a entidade inteira nem
 * navegar associações lazy fora de transação — o repositório projeta os três ids numa consulta só.
 */
public record Territorio(UUID paroquiaId, UUID setorId, UUID dioceseId) {
}
