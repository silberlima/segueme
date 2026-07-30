package com.silberlima.segueme.notificacao.api;

import com.silberlima.segueme.api.dto.PageResponse;
import com.silberlima.segueme.notificacao.api.dto.AtualizarPreferenciasRequest;
import com.silberlima.segueme.notificacao.api.dto.NotificacaoResponse;
import com.silberlima.segueme.notificacao.api.dto.NotificacoesNaoLidasResponse;
import com.silberlima.segueme.notificacao.api.dto.PreferenciaNotificacaoResponse;
import com.silberlima.segueme.notificacao.application.AtualizarPreferenciasDeNotificacaoUseCase;
import com.silberlima.segueme.notificacao.application.ConsultarPreferenciasDeNotificacaoUseCase;
import com.silberlima.segueme.notificacao.application.ContarNotificacoesNaoLidasUseCase;
import com.silberlima.segueme.notificacao.application.ListarNotificacoesUseCase;
import com.silberlima.segueme.notificacao.application.MarcarNotificacoesComoLidasUseCase;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notificacoes")
@Tag(name = "Notificações", description = "Central de notificações do usuário autenticado (RN-SISTEMA-01/02)")
public class NotificacaoController {

    private final ListarNotificacoesUseCase listarNotificacoesUseCase;
    private final MarcarNotificacoesComoLidasUseCase marcarNotificacoesComoLidasUseCase;
    private final ContarNotificacoesNaoLidasUseCase contarNotificacoesNaoLidasUseCase;
    private final ConsultarPreferenciasDeNotificacaoUseCase consultarPreferenciasDeNotificacaoUseCase;
    private final AtualizarPreferenciasDeNotificacaoUseCase atualizarPreferenciasDeNotificacaoUseCase;
    private final NotificacaoResponseMapper notificacaoResponseMapper;

    public NotificacaoController(
            ListarNotificacoesUseCase listarNotificacoesUseCase,
            MarcarNotificacoesComoLidasUseCase marcarNotificacoesComoLidasUseCase,
            ContarNotificacoesNaoLidasUseCase contarNotificacoesNaoLidasUseCase,
            ConsultarPreferenciasDeNotificacaoUseCase consultarPreferenciasDeNotificacaoUseCase,
            AtualizarPreferenciasDeNotificacaoUseCase atualizarPreferenciasDeNotificacaoUseCase,
            NotificacaoResponseMapper notificacaoResponseMapper) {
        this.listarNotificacoesUseCase = listarNotificacoesUseCase;
        this.marcarNotificacoesComoLidasUseCase = marcarNotificacoesComoLidasUseCase;
        this.contarNotificacoesNaoLidasUseCase = contarNotificacoesNaoLidasUseCase;
        this.consultarPreferenciasDeNotificacaoUseCase = consultarPreferenciasDeNotificacaoUseCase;
        this.atualizarPreferenciasDeNotificacaoUseCase = atualizarPreferenciasDeNotificacaoUseCase;
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

    @GetMapping("/nao-lidas")
    @Operation(summary = "Total de notificações não lidas do usuário autenticado (RN-SISTEMA-02)")
    public ResponseEntity<NotificacoesNaoLidasResponse> naoLidas(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(
                new NotificacoesNaoLidasResponse(contarNotificacoesNaoLidasUseCase.execute(jwt.getSubject())));
    }

    @PatchMapping("/marcar-lidas")
    @Operation(summary = "Marca todas as notificações não lidas do usuário autenticado como lidas")
    public ResponseEntity<Void> marcarLidas(@AuthenticationPrincipal Jwt jwt) {
        marcarNotificacoesComoLidasUseCase.execute(jwt.getSubject());
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/preferencias")
    @Operation(summary = "Preferências de notificação do usuário autenticado, por tipo (RF-057)")
    public ResponseEntity<List<PreferenciaNotificacaoResponse>> preferencias(@AuthenticationPrincipal Jwt jwt) {
        return ResponseEntity.ok(PreferenciaNotificacaoResponse.from(
                consultarPreferenciasDeNotificacaoUseCase.execute(jwt.getSubject())));
    }

    @PutMapping("/preferencias")
    @Operation(summary = "Atualiza as preferências de notificação do próprio usuário autenticado (RF-057)")
    public ResponseEntity<List<PreferenciaNotificacaoResponse>> atualizarPreferencias(
            @AuthenticationPrincipal Jwt jwt, @Valid @RequestBody AtualizarPreferenciasRequest request) {
        return ResponseEntity.ok(PreferenciaNotificacaoResponse.from(
                atualizarPreferenciasDeNotificacaoUseCase.execute(jwt.getSubject(), request.paraEscolhas())));
    }
}
