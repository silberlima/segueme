package com.silberlima.segueme.inscricoes.api.dto;

/**
 * Resultado de {@code POST /pre-inscricoes}: exatamente um dos dois campos vem preenchido,
 * dependendo de a vaga do Encontro estar disponível ou esgotada (RN-INSCRICAO-03). Não é um erro
 * — cair na reserva é o comportamento automático esperado, por isso a resposta é sempre 201.
 */
public record CriarPreInscricaoResponse(
        boolean emReserva, PreInscricaoResponse preInscricao, CadastroReservaResponse cadastroReserva) {
}
