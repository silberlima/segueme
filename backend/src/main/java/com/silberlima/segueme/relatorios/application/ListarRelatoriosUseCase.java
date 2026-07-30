package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.RelatorioResumo;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

/**
 * "Gerados recentemente" da tela Relatórios: os relatórios que o próprio usuário pediu.
 *
 * <p>A lista é por solicitante e não por território porque um relatório carrega o escopo de quem o
 * pediu — expor o de outro usuário mostraria dado apurado sob um escopo que não é o do leitor.
 */
@Service
public class ListarRelatoriosUseCase {

    private final RelatorioRepository relatorioRepository;

    public ListarRelatoriosUseCase(RelatorioRepository relatorioRepository) {
        this.relatorioRepository = relatorioRepository;
    }

    public Page<RelatorioResumo> execute(String solicitanteId, Pageable pageable) {
        return relatorioRepository.resumosDoSolicitante(solicitanteId, pageable);
    }
}
