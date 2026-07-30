package com.silberlima.segueme.relatorios.application;

import com.silberlima.segueme.domain.exception.ResourceNotFoundException;
import com.silberlima.segueme.notificacao.application.PublicarNotificacaoUseCase;
import com.silberlima.segueme.notificacao.domain.TipoNotificacao;
import com.silberlima.segueme.relatorios.domain.ConteudoRelatorio;
import com.silberlima.segueme.relatorios.domain.GeradorDeRelatorio;
import com.silberlima.segueme.relatorios.domain.Relatorio;
import com.silberlima.segueme.relatorios.domain.RelatorioRepository;
import com.silberlima.segueme.relatorios.domain.TipoRelatorio;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Executa a geração de um relatório já solicitado e avisa o solicitante pela central de
 * notificações (RN-SISTEMA-01) — é como ele descobre que o arquivo ficou pronto sem ficar
 * recarregando a tela.
 *
 * <p>Deliberadamente sem {@code @Transactional}: cada gerador abre a própria transação de leitura,
 * e o registro do desfecho (concluído ou falhou) tem que ser gravado mesmo quando a leitura falhou
 * — o que seria impossível dentro de uma transação já marcada para rollback.
 */
@Service
public class GerarRelatorioUseCase {

    private static final Logger log = LoggerFactory.getLogger(GerarRelatorioUseCase.class);

    private final RelatorioRepository relatorioRepository;
    private final PublicarNotificacaoUseCase publicarNotificacaoUseCase;
    private final Map<TipoRelatorio, GeradorDeRelatorio> geradoresPorTipo = new EnumMap<>(TipoRelatorio.class);

    public GerarRelatorioUseCase(
            RelatorioRepository relatorioRepository,
            PublicarNotificacaoUseCase publicarNotificacaoUseCase,
            List<GeradorDeRelatorio> geradores) {
        this.relatorioRepository = relatorioRepository;
        this.publicarNotificacaoUseCase = publicarNotificacaoUseCase;
        geradores.forEach(gerador -> geradoresPorTipo.put(gerador.suporta(), gerador));
    }

    public void execute(UUID relatorioId) {
        Relatorio relatorio = relatorioRepository
                .findByIdAndDeletedFalse(relatorioId)
                .orElseThrow(() -> new ResourceNotFoundException("Relatório não encontrado: " + relatorioId));

        try {
            ConteudoRelatorio conteudo = gerador(relatorio.getTipo()).gerar(relatorio.escopo());
            relatorio.concluir(conteudo);
        } catch (RuntimeException excecao) {
            log.error("Falha ao gerar o relatório {} ({})", relatorioId, relatorio.getTipo(), excecao);
            relatorio.falhar(excecao.getMessage());
        }
        relatorioRepository.save(relatorio);
        notificarSolicitante(relatorio);
    }

    private GeradorDeRelatorio gerador(TipoRelatorio tipo) {
        GeradorDeRelatorio gerador = geradoresPorTipo.get(tipo);
        if (gerador == null) {
            throw new IllegalStateException("Nenhum gerador registrado para o relatório de " + tipo + ".");
        }
        return gerador;
    }

    private void notificarSolicitante(Relatorio relatorio) {
        boolean concluido = relatorio.getErro() == null;
        publicarNotificacaoUseCase.execute(
                relatorio.getSolicitanteId(),
                concluido ? TipoNotificacao.POSITIVA : TipoNotificacao.NEGATIVA,
                concluido
                        ? "Relatório pronto: " + relatorio.getTipo().getTitulo()
                        : "Relatório não gerado: " + relatorio.getTipo().getTitulo(),
                concluido
                        ? "O relatório de " + relatorio.getTipo().getTitulo() + " ficou pronto com "
                                + relatorio.getTotalLinhas() + " linha(s) e já pode ser baixado."
                        : "Não foi possível gerar o relatório de " + relatorio.getTipo().getTitulo() + ": "
                                + relatorio.getErro());
    }
}
