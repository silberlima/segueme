package com.silberlima.segueme.movimentacao.api;

import com.silberlima.segueme.movimentacao.api.dto.SolicitacaoResponse;
import com.silberlima.segueme.movimentacao.application.dto.SolicitacaoItem;
import com.silberlima.segueme.movimentacao.application.dto.TipoSolicitacao;
import com.silberlima.segueme.movimentacao.domain.Emprestimo;
import com.silberlima.segueme.movimentacao.domain.Transferencia;
import org.mapstruct.Mapper;

/**
 * Monta a descrição textual de cada item do inbox (RF-046) via métodos {@code default} — e não
 * {@code @Mapping} declarativo — porque {@link SolicitacaoItem} tem exatamente um dos dois lados
 * nulo conforme {@link TipoSolicitacao} (mesma técnica de composição usada em
 * {@code MontagemResponseMapper}).
 */
@Mapper(componentModel = "spring")
public interface SolicitacaoResponseMapper {

    default SolicitacaoResponse toResponse(SolicitacaoItem item) {
        return switch (item.tipo()) {
            case TRANSFERENCIA -> toResponse(item.transferencia());
            case EMPRESTIMO -> toResponse(item.emprestimo());
        };
    }

    private SolicitacaoResponse toResponse(Transferencia transferencia) {
        String descricao = transferencia.getPessoa().getNomeCompleto() + " solicita transferência de "
                + transferencia.getParoquiaOrigem().getNome() + " para " + transferencia.getParoquiaDestino().getNome() + ".";
        return new SolicitacaoResponse(
                transferencia.getId(),
                TipoSolicitacao.TRANSFERENCIA,
                transferencia.getCreatedAt(),
                descricao,
                transferencia.getPessoa().getNomeCompleto(),
                transferencia.getParoquiaOrigem().getNome(),
                transferencia.getParoquiaDestino().getNome(),
                null,
                null,
                null);
    }

    private SolicitacaoResponse toResponse(Emprestimo emprestimo) {
        String competenciaNome = emprestimo.getCompetenciaRequerida() != null
                ? emprestimo.getCompetenciaRequerida().getNome()
                : null;
        String descricao = "Equipe " + emprestimo.getEquipe().getNome() + " do Encontro #"
                + emprestimo.getEncontro().getNumero() + " (" + emprestimo.getEncontro().getParoquia().getNome()
                + ") precisa de um empréstimo do setor" + (competenciaNome != null ? " — habilidade: " + competenciaNome : "") + ".";
        return new SolicitacaoResponse(
                emprestimo.getId(),
                TipoSolicitacao.EMPRESTIMO,
                emprestimo.getCreatedAt(),
                descricao,
                null,
                null,
                null,
                emprestimo.getEquipe().getNome(),
                emprestimo.getEncontro().getNumero(),
                competenciaNome);
    }
}
