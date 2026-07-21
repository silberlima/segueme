package com.silberlima.segueme.acesso.domain;

import java.util.List;

/**
 * Porta implementada por cada domínio pesquisável na busca global (RN-ACESSO-01/RN-04).
 * Cada implementação é responsável por aplicar seu próprio filtro de escopo territorial
 * ao usuário autenticado antes de retornar resultados.
 *
 * <p>Nenhuma implementação existe ainda: Pessoa (Fase 3) e Encontro (Fase 4) são as
 * primeiras fontes de dados da busca e devem registrar um bean {@code BuscaProvider}
 * quando essas fases forem implementadas. Até lá, {@code GET /api/v1/search} responde
 * com lista vazia.</p>
 */
public interface BuscaProvider {

    BuscaTipo tipo();

    List<BuscaResultado> buscar(String query, String usuarioId);
}
