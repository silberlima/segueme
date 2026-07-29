package com.silberlima.segueme.posencontro.application;

import com.silberlima.segueme.pessoas.domain.Pessoa;
import com.silberlima.segueme.pessoas.domain.PessoaRepository;
import com.silberlima.segueme.pessoas.domain.SituacaoPessoa;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Pool de jovens acompanhados no pós-encontro de uma paróquia: quem já fez o Encontro, ou seja
 * {@link SituacaoPessoa#SEGUIMISTA} e {@link SituacaoPessoa#SEGUIDOR} (`pos-encontro.md` 1 —
 * "jovens participantes após o Encontro"). Quem já virou Seguidor permanece no acompanhamento para
 * que o fator de frequência do motor de sugestões da Fase 6 tenha dado sobre ele.
 *
 * <p>Compartilhado pelas telas Presenças, Frequência e Prioridades, que apuram sempre sobre o mesmo
 * pool — inclusive quem nunca teve presença registrada, que aparece com 0 presenças em vez de sumir
 * da lista.
 */
@Service
public class ConsultarAcompanhadosUseCase {

    private static final Set<SituacaoPessoa> SITUACOES_ACOMPANHADAS =
            EnumSet.of(SituacaoPessoa.SEGUIMISTA, SituacaoPessoa.SEGUIDOR);

    private final PessoaRepository pessoaRepository;

    public ConsultarAcompanhadosUseCase(PessoaRepository pessoaRepository) {
        this.pessoaRepository = pessoaRepository;
    }

    @Transactional(readOnly = true)
    public List<Pessoa> execute(UUID paroquiaId) {
        return pessoaRepository.findByParoquia_IdAndSituacaoInAndDeletedFalseOrderByNomeCompletoAsc(
                paroquiaId, SITUACOES_ACOMPANHADAS);
    }
}
