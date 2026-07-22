package com.silberlima.segueme.pessoas.api.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CasalResponse(UUID id, PessoaResumoResponse pessoa1, PessoaResumoResponse pessoa2, LocalDate dataVinculo) {
}
