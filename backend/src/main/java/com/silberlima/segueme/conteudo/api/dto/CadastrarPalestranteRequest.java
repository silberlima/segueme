package com.silberlima.segueme.conteudo.api.dto;

import com.silberlima.segueme.conteudo.domain.TipoPalestrante;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

/**
 * Cadastra um palestrante (RF-052). {@code nome} é obrigatório para EXTERNO/CLERO e ignorado para
 * SEGUIDOR (o nome vem do cadastro de Pessoa); {@code pessoaId} é obrigatório só para SEGUIDOR —
 * a regra cruzada é validada no domínio ({@link com.silberlima.segueme.conteudo.domain.Palestrante}),
 * não aqui (RN-CONTEUDO-02).
 */
public record CadastrarPalestranteRequest(@NotNull TipoPalestrante tipo, String nome, UUID pessoaId) {
}
