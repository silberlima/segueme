package com.silberlima.segueme.montagem.application.dto;

import java.util.UUID;

/** Dados de entrada de {@code RegistrarSubstituicaoUseCase} (RF-032), desacoplados do request da API. */
public record DadosSubstituicao(
        UUID encontroId, UUID equipeId, UUID pessoaSaiId, String motivoSaida, UUID pessoaEntraId, String justificativa) {
}
