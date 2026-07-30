package com.silberlima.segueme.relatorios.domain;

import com.silberlima.segueme.acesso.domain.Perfil;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Os seis tipos de relatório da tela Relatórios (RF-056, UC-22).
 *
 * <p><strong>Pendência #15-sistema de {@code docs/pendencias-gerais.md}:</strong> o protótipo mostra
 * seis cartões sem dizer quais são. Adotamos um tipo por módulo de dados já implementado — assim
 * todo relatório sai de dado real, e trocar a lista quando o PO a definir é mexer só neste enum.
 *
 * <p>{@code perfisAutorizados} não é decoração: {@code SolicitarRelatorioUseCase} recusa o tipo que
 * o perfil ativo não pode gerar, e a tela só exibe os cartões liberados. {@code SEGUIDOR} não gera
 * relatório algum.
 */
public enum TipoRelatorio {

    PESSOAS(
            "Pessoas",
            "Cadastro de pessoas do seu escopo territorial, com paróquia, situação e competências.",
            Set.of(Perfil.NACIONAL, Perfil.DIOCESE, Perfil.SETORIAL, Perfil.PAROQUIA, Perfil.EQUIPE_FICHAS)),

    ENCONTROS(
            "Encontros",
            "Encontros do seu escopo territorial, com paróquia, datas, vagas e situação.",
            Set.of(Perfil.NACIONAL, Perfil.DIOCESE, Perfil.SETORIAL, Perfil.PAROQUIA)),

    INSCRICOES(
            "Inscrições",
            "Funil de inscrição do Encontro corrente da paróquia, etapa a etapa.",
            Set.of(Perfil.PAROQUIA, Perfil.EQUIPE_FICHAS)),

    EQUIPES(
            "Equipes e convites",
            "Composição das equipes do Encontro corrente, com o estado do convite de cada integrante.",
            Set.of(Perfil.PAROQUIA, Perfil.EQUIPE_MONTAGEM, Perfil.EQUIPE_COMANDO)),

    FINANCEIRO(
            "Financeiro",
            "Lançamentos de receita e despesa do Encontro corrente da paróquia.",
            Set.of(Perfil.PAROQUIA, Perfil.EQUIPE_FINANCAS)),

    POS_ENCONTRO(
            "Pós-encontro",
            "Frequência acumulada dos jovens acompanhados e a faixa de prioridade de cada um.",
            Set.of(Perfil.PAROQUIA, Perfil.POS_ENCONTRO));

    private final String titulo;
    private final String descricao;
    private final Set<Perfil> perfisAutorizados;

    TipoRelatorio(String titulo, String descricao, Set<Perfil> perfisAutorizados) {
        this.titulo = titulo;
        this.descricao = descricao;
        this.perfisAutorizados = perfisAutorizados;
    }

    public String getTitulo() {
        return titulo;
    }

    public String getDescricao() {
        return descricao;
    }

    public boolean autorizadoPara(List<Perfil> perfis) {
        return perfis.stream().anyMatch(perfisAutorizados::contains);
    }

    /** Tipos que o usuário pode gerar — os cartões que a tela Relatórios exibe para ele. */
    public static List<TipoRelatorio> disponiveisPara(List<Perfil> perfis) {
        return Arrays.stream(values()).filter(tipo -> tipo.autorizadoPara(perfis)).toList();
    }

    /** Nome do arquivo baixado, sem depender de o frontend saber montar. */
    public String nomeDoArquivo() {
        return "relatorio-" + name().toLowerCase().replace('_', '-') + ".csv";
    }
}
