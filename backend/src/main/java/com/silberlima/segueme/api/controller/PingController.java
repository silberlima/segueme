package com.silberlima.segueme.api.controller;

import com.silberlima.segueme.api.dto.PingResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ping")
@Tag(name = "Infraestrutura", description = "Endpoints de verificação da fundação técnica")
public class PingController {

    @GetMapping
    @Operation(summary = "Verifica se a API está no ar e a autenticação JWT está configurada")
    public ResponseEntity<PingResponse> ping() {
        return ResponseEntity.ok(new PingResponse("pong"));
    }
}
