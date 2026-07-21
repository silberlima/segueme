package com.silberlima.segueme.notificacao.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.notificacao.api.dto.NotificacaoResponse;
import com.silberlima.segueme.notificacao.application.ListarNotificacoesUseCase;
import com.silberlima.segueme.notificacao.application.MarcarNotificacoesComoLidasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificações", description = "Central de notificações do usuário autenticado (RN-SISTEMA-01/02)")
public class NotificacaoController {

    private final ListarNotificacoesUseCase listarNotificacoesUseCase;
    private final MarcarNotificacoesComoLidasUseCase marcarNotificacoesComoLidasUseCase;
    private final NotificacaoResponseMapper notificacaoResponseMapper;

    public NotificacaoController(
            ListarNotificacoesUseCase listarNotificacoesUseCase,
            MarcarNotificacoesComoLidasUseCase marcarNotificacoesComoLidasUseCase,
            NotificacaoResponseMapper notificacaoResponseMapper) {
        this.listarNotificacoesUseCase = listarNotificacoesUseCase;
        this.marcarNotificacoesComoLidasUseCase = marcarNotificacoesComoLidasUseCase;
        this.notificacaoResponseMapper = notificacaoResponseMapper;
    }

    @GetMapping
    @Operation(summary = "Lista as notificações do usuário autenticado, mais recentes primeiro")
    public ResponseEntity<PageResponse<NotificacaoResponse>> listar(
            @AuthenticationPrincipal Jwt jwt,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        var pagina = listarNotificacoesUseCase.execute(jwt.getSubject(), pageable)
                .map(notificacaoResponseMapper::toResponse);
        return ResponseEntity.ok(PageResponse.from(pagina));
    }

    @PatchMapping("/marcar-lidas")
    @Operation(summary = "Marca todas as notificações não lidas do usuário autenticado como lidas")
    public ResponseEntity<Void> marcarLidas(@AuthenticationPrincipal Jwt jwt) {
        marcarNotificacoesComoLidasUseCase.execute(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }
}
