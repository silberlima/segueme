package com.silberlima.segueme.acesso.api;

import com.silberlima.segueme.acesso.api.dto.BuscaResultadoResponse;
import com.silberlima.segueme.acesso.application.BuscarGlobalUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/search")
@Validated
@Tag(name = "Acesso", description = "Busca global de pessoas e encontros")
public class BuscaController {

    private final BuscarGlobalUseCase buscarGlobalUseCase;

    public BuscaController(BuscarGlobalUseCase buscarGlobalUseCase) {
        this.buscarGlobalUseCase = buscarGlobalUseCase;
    }

    @GetMapping
    @Operation(summary = "Busca pessoas e encontros no escopo do usuário autenticado (RN-ACESSO-01)")
    public ResponseEntity<List<BuscaResultadoResponse>> search(
            @RequestParam @NotBlank @Size(min = 2, max = 100) String q,
            @AuthenticationPrincipal Jwt jwt) {
        List<BuscaResultadoResponse> resultados = buscarGlobalUseCase.execute(q, jwt.getSubject()).stream()
                .map(BuscaResultadoResponse::from)
                .toList();
        return ResponseEntity.ok(resultados);
    }
}
