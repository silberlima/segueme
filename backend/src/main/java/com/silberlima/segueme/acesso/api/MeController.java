package com.silberlima.segueme.acesso.api;

import com.silberlima.segueme.acesso.api.dto.MeResponse;
import com.silberlima.segueme.acesso.application.GetMeUseCase;
import com.silberlima.segueme.acesso.application.dto.UsuarioAutenticadoClaims;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/me")
@Tag(name = "Acesso", description = "Perfil autenticado e perfis de acesso permitidos")
public class MeController {

    private final GetMeUseCase getMeUseCase;

    public MeController(GetMeUseCase getMeUseCase) {
        this.getMeUseCase = getMeUseCase;
    }

    @GetMapping
    @Operation(summary = "Retorna o usuário autenticado e os perfis de acesso permitidos (RN-ACESSO-02)")
    public ResponseEntity<MeResponse> me(@AuthenticationPrincipal Jwt jwt, Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        String nome = jwt.getClaimAsString("name") != null
                ? jwt.getClaimAsString("name")
                : jwt.getClaimAsString("preferred_username");
        UsuarioAutenticadoClaims claims = new UsuarioAutenticadoClaims(
                jwt.getSubject(), jwt.getClaimAsString("email"), nome, authorities);
        return ResponseEntity.ok(MeResponse.from(getMeUseCase.execute(claims)));
    }
}
