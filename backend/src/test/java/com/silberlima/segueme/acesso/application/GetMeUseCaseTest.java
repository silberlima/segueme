package com.silberlima.segueme.acesso.application;

import static org.assertj.core.api.Assertions.assertThat;

import com.silberlima.segueme.acesso.application.dto.MeResult;
import com.silberlima.segueme.acesso.application.dto.UsuarioAutenticadoClaims;
import com.silberlima.segueme.acesso.domain.Perfil;
import java.util.List;
import org.junit.jupiter.api.Test;

class GetMeUseCaseTest {

    private final GetMeUseCase useCase = new GetMeUseCase();

    @Test
    void shouldMapKnownAuthoritiesToPerfis() {
        UsuarioAutenticadoClaims claims = new UsuarioAutenticadoClaims(
                "user-1", "user@segueme.org", "Fulano de Tal",
                List.of("ROLE_PAROQUIA", "ROLE_EQUIPE_MONTAGEM"));

        MeResult result = useCase.execute(claims);

        assertThat(result.id()).isEqualTo("user-1");
        assertThat(result.email()).isEqualTo("user@segueme.org");
        assertThat(result.nome()).isEqualTo("Fulano de Tal");
        assertThat(result.perfis()).containsExactly(Perfil.PAROQUIA, Perfil.EQUIPE_MONTAGEM);
    }

    @Test
    void shouldIgnoreAuthoritiesThatAreNotPerfis() {
        UsuarioAutenticadoClaims claims = new UsuarioAutenticadoClaims(
                "user-2", "user2@segueme.org", "Ciclana",
                List.of("ROLE_PAROQUIA", "OFFLINE_ACCESS", "ROLE_DESCONHECIDO"));

        MeResult result = useCase.execute(claims);

        assertThat(result.perfis()).containsExactly(Perfil.PAROQUIA);
    }

    @Test
    void shouldReturnEmptyPerfisWhenNoAuthoritiesMatch() {
        UsuarioAutenticadoClaims claims = new UsuarioAutenticadoClaims(
                "user-3", "user3@segueme.org", "Beltrano", List.of());

        MeResult result = useCase.execute(claims);

        assertThat(result.perfis()).isEmpty();
    }
}
