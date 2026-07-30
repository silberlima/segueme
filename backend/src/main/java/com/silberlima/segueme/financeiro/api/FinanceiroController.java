package com.silberlima.segueme.financeiro.api;

import com.silberlima.segueme.financeiro.api.dto.ResumoFinanceiroResponse;
import com.silberlima.segueme.financeiro.application.ObterResumoFinanceiroUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.UUID;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Visão geral financeira de um Encontro (`screen_financeiro`, RN-FINANCEIRO-04). */
@RestController
@RequestMapping("/api/v1/financeiro")
@Tag(name = "Financeiro — Resumo", description = "Saldo, receitas, despesas e campanhas ativas de um Encontro")
public class FinanceiroController {

    private static final String PERFIS_LEITURA =
            "hasAnyRole('NACIONAL','DIOCESE','SETORIAL','PAROQUIA','EQUIPE_FINANCAS')";

    private final ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase;

    public FinanceiroController(ObterResumoFinanceiroUseCase obterResumoFinanceiroUseCase) {
        this.obterResumoFinanceiroUseCase = obterResumoFinanceiroUseCase;
    }

    @GetMapping("/resumo")
    @PreAuthorize(PERFIS_LEITURA + " and @escopoTerritorial.podeVerEncontro(#encontroId)")
    @Operation(summary = "Consolida saldo, receitas, despesas e campanhas ativas de um Encontro (RN-FINANCEIRO-04)")
    public ResumoFinanceiroResponse resumo(@RequestParam UUID encontroId) {
        var resumo = obterResumoFinanceiroUseCase.execute(encontroId);
        return new ResumoFinanceiroResponse(resumo.saldo(), resumo.totalReceitas(), resumo.totalDespesas(), resumo.campanhasAtivas());
    }
}
