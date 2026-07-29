package com.silberlima.segueme.posencontro.application.dto;

import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.posencontro.domain.FaixaPrioridade;
import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;

/**
 * Frequência apurada de uma pessoa no pós-encontro (RF-059, RN-POS-03) e a faixa de prioridade
 * resultante. O denominador é o total de encontros pós que a paróquia realizou na janela apurada —
 * não o número de registros da pessoa — para reproduzir os exemplos do doc (12/12 = Alta, 3/12 =
 * Baixa): quem faltou sem registro conta como ausente.
 *
 * @param percentual 0–100; vale 0 quando a paróquia ainda não realizou nenhum encontro pós no período
 */
public record FrequenciaPessoa(
        Pessoa pessoa, long totalEncontros, long presencas, int percentual, FaixaPrioridade faixa) {

    public static FrequenciaPessoa apurar(
            Pessoa pessoa, long totalEncontros, long presencas, ReguaPrioridade regua) {
        int percentual = totalEncontros == 0 ? 0 : (int) Math.round((presencas * 100.0) / totalEncontros);
        return new FrequenciaPessoa(pessoa, totalEncontros, presencas, percentual, regua.classificar(percentual));
    }
}
