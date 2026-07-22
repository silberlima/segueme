package com.silberlima.segueme.pessoas.domain;

/** Tipos de evento observados na Linha do Tempo de uma Pessoa (`cadastro-de-pessoas.md` 2.4). */
public enum TipoEventoHistorico {
    INSCRICAO_CADASTRO_NACIONAL,
    PRIMEIRA_PARTICIPACAO_SEGUIDOR,
    PRIMEIRA_EQUIPE_EQUIPISTA,
    TORNOU_SE_SEGUIMISTA,
    PARTICIPACAO_EQUIPE,
    VINCULO_CASAL_REGISTRADO,
    EMPRESTIMO_OUTRA_PAROQUIA,
    PAPEL_COORDENADOR
}
