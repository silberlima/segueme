package com.silberlima.segueme.hierarquia.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EscopoTerritorialResolverTest {

    private final EscopoTerritorialResolver resolver = new EscopoTerritorialResolver();

    @Test
    void shouldResolveNacionalRegardlessOfClaims() {
        EscopoTerritorial escopo = resolver.resolve(List.of(Perfil.NACIONAL), null, null, null);

        assertThat(escopo).isInstanceOf(EscopoTerritorial.Nacional.class);
    }

    @Test
    void shouldResolveDeDioceseFromClaim() {
        UUID dioceseId = UUID.randomUUID();

        EscopoTerritorial escopo = resolver.resolve(List.of(Perfil.DIOCESE), dioceseId.toString(), null, null);

        assertThat(escopo).isEqualTo(new EscopoTerritorial.DeDiocese(dioceseId));
    }

    @Test
    void shouldThrowWhenDioceseClaimMissing() {
        assertThatThrownBy(() -> resolver.resolve(List.of(Perfil.DIOCESE), null, null, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldResolveDeSetorFromClaim() {
        UUID setorId = UUID.randomUUID();

        EscopoTerritorial escopo = resolver.resolve(List.of(Perfil.SETORIAL), null, setorId.toString(), null);

        assertThat(escopo).isEqualTo(new EscopoTerritorial.DeSetor(setorId));
    }

    @Test
    void shouldResolveDeParoquiaFromClaim() {
        UUID paroquiaId = UUID.randomUUID();

        EscopoTerritorial escopo = resolver.resolve(List.of(Perfil.PAROQUIA), null, null, paroquiaId.toString());

        assertThat(escopo).isEqualTo(new EscopoTerritorial.DeParoquia(paroquiaId));
    }

    @Test
    void shouldThrowWhenNoRelevantPerfilPresent() {
        assertThatThrownBy(() -> resolver.resolve(List.of(Perfil.EQUIPE_FICHAS), null, null, null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldThrowWhenClaimIsNotAValidUuid() {
        assertThatThrownBy(() -> resolver.resolve(List.of(Perfil.SETORIAL), null, "not-a-uuid", null))
                .isInstanceOf(BusinessRuleViolationException.class);
    }

    @Test
    void shouldPrioritizeNacionalOverOtherPerfis() {
        EscopoTerritorial escopo = resolver.resolve(
                List.of(Perfil.SETORIAL, Perfil.NACIONAL), null, UUID.randomUUID().toString(), null);

        assertThat(escopo).isInstanceOf(EscopoTerritorial.Nacional.class);
    }
}
