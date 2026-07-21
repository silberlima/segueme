package com.silberlima.segueme.acesso.application;

import com.silberlima.segueme.acesso.application.dto.MeResult;
import com.silberlima.segueme.acesso.application.dto.UsuarioAutenticadoClaims;
import com.silberlima.segueme.acesso.domain.Perfil;
import java.util.List;
import org.springframework.stereotype.Service;

/**
 * Resolve o perfil autenticado e os perfis permitidos para o usuário (RN-ACESSO-02).
 * Um usuário real pode ter mais de um perfil atribuído simultaneamente (pendência #3 de
 * {@code docs/pendencias-gerais.md}, validada como 1:N) — a troca de perfil ativo em si
 * é responsabilidade do frontend (RN-ACESSO-02), este use case apenas informa o conjunto
 * de perfis que o usuário está autorizado a assumir.
 */
@Service
public class GetMeUseCase {

    public MeResult execute(UsuarioAutenticadoClaims claims) {
        List<Perfil> perfis = Perfil.fromAuthorities(claims.authorities());
        return new MeResult(claims.id(), claims.email(), claims.nome(), perfis);
    }
}
