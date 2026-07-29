package com.silberlima.segueme.posencontro.application.dto;

import com.silberlima.segueme.pessoas.domain.Pessoa;

/**
 * Uma pessoa acompanhada e sua marcação num encontro pós específico (tela Presenças, UC-20).
 *
 * @param presente {@code null} enquanto a presença dessa pessoa não foi registrada — distinto de
 *     {@code false}, que é a ausência já conferida pela equipe
 */
public record PresencaPessoa(Pessoa pessoa, Boolean presente) {
}
