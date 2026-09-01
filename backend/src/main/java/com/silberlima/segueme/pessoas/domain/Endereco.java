package com.silberlima.segueme.pessoas.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Endereço residencial da Pessoa, obrigatório no cadastro.
 *
 * <p>Guardado em campos separados, e não como texto livre: cidade e UF são o que permite cruzar o
 * cadastro com a hierarquia territorial (diocese/setor/paróquia) e agrupar pessoas por região nos
 * relatórios. Um único varchar preenchido à mão não sustenta essa consulta.
 *
 * <p>{@code complemento} é o único campo opcional — apartamento, bloco e ponto de referência não
 * existem em todo endereço, e exigir um deles obrigaria a inventar valor.
 */
@Embeddable
public class Endereco {

    @Column(name = "endereco_cep", nullable = false, length = 9)
    private String cep;

    @Column(name = "endereco_logradouro", nullable = false)
    private String logradouro;

    @Column(name = "endereco_numero", nullable = false, length = 20)
    private String numero;

    @Column(name = "endereco_complemento")
    private String complemento;

    @Column(name = "endereco_bairro", nullable = false)
    private String bairro;

    @Column(name = "endereco_cidade", nullable = false)
    private String cidade;

    @Column(name = "endereco_uf", nullable = false, length = 2)
    private String uf;

    protected Endereco() {
    }

    public Endereco(
            String cep,
            String logradouro,
            String numero,
            String complemento,
            String bairro,
            String cidade,
            String uf) {
        this.cep = cep;
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.uf = uf == null ? null : uf.toUpperCase();
    }

    public String getCep() {
        return cep;
    }

    public String getLogradouro() {
        return logradouro;
    }

    public String getNumero() {
        return numero;
    }

    public String getComplemento() {
        return complemento;
    }

    public String getBairro() {
        return bairro;
    }

    public String getCidade() {
        return cidade;
    }

    public String getUf() {
        return uf;
    }
}
