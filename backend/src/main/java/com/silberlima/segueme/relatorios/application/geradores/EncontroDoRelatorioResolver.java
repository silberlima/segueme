package com.silberlima.segueme.relatorios.application.geradores;

import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.Encontro;
import com.silberlima.segueme.encontros.domain.EncontroCorrenteResolver;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import java.time.LocalDate;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Resolve o Encontro sobre o qual falam os relatórios de Inscrições, Equipes e Financeiro.
 *
 * <p>Segue o mesmo critério dos painéis das equipes (Fase 12): esses relatórios não recebem
 * {@code encontroId} porque a tela Relatórios não tem seletor de Encontro no protótipo — falam
 * sempre do Encontro corrente da paróquia do solicitante.
 */
@Service
public class EncontroDoRelatorioResolver {

    private final EncontroCorrenteResolver encontroCorrenteResolver;

    public EncontroDoRelatorioResolver(EncontroCorrenteResolver encontroCorrenteResolver) {
        this.encontroCorrenteResolver = encontroCorrenteResolver;
    }

    public UUID paroquiaDoEscopo(EscopoTerritorial escopo) {
        if (escopo instanceof EscopoTerritorial.DeParoquia deParoquia) {
            return deParoquia.paroquiaId();
        }
        throw new BusinessRuleViolationException(
                "Este relatório é sobre o Encontro corrente de uma paróquia e exige um usuário com escopo de paróquia.");
    }

    public Encontro encontroCorrente(EscopoTerritorial escopo) {
        UUID paroquiaId = paroquiaDoEscopo(escopo);
        return encontroCorrenteResolver
                .resolver(paroquiaId, LocalDate.now())
                .orElseThrow(() -> new BusinessRuleViolationException(
                        "A paróquia ainda não tem nenhum Encontro para relatar."));
    }
}
