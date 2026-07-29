package com.silberlima.segueme.posencontro.infrastructure;

import com.silberlima.segueme.posencontro.domain.ReguaPrioridade;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Régua de corte das faixas de prioridade do pós-encontro, parametrizável por ambiente
 * ({@code app.pos-encontro.regua.*} em {@code application.yml}). A régua exata é pendência #14 de
 * {@code pendencias-gerais.md}: enquanto o PO não a validar, ela não pode virar constante no código
 * — o plano da Fase 11 exige explicitamente "regra parametrizável, não hardcoded".
 *
 * @param percentualMinimoAlta corte da faixa Alta; default 75
 * @param percentualMinimoMedia corte da faixa Média; default 40
 */
@ConfigurationProperties(prefix = "app.pos-encontro.regua")
public record PosEncontroProperties(Integer percentualMinimoAlta, Integer percentualMinimoMedia) {

    private static final int DEFAULT_PERCENTUAL_MINIMO_ALTA = 75;
    private static final int DEFAULT_PERCENTUAL_MINIMO_MEDIA = 40;

    public ReguaPrioridade regua() {
        return new ReguaPrioridade(
                percentualMinimoAlta == null ? DEFAULT_PERCENTUAL_MINIMO_ALTA : percentualMinimoAlta,
                percentualMinimoMedia == null ? DEFAULT_PERCENTUAL_MINIMO_MEDIA : percentualMinimoMedia);
    }
}
