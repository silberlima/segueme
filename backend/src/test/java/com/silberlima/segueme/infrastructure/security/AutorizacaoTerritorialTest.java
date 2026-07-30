package com.silberlima.segueme.infrastructure.security;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Territorio;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

@ExtendWith(MockitoExtension.class)
class AutorizacaoTerritorialTest {

    private static final UUID DIOCESE = UUID.randomUUID();
    private static final UUID SETOR = UUID.randomUUID();
    private static final UUID PAROQUIA = UUID.randomUUID();
    private static final UUID ENCONTRO = UUID.randomUUID();

    @Mock
    private EncontroRepository encontroRepository;

    @Mock
    private ParoquiaRepository paroquiaRepository;

    @Mock
    private EncontroPosRepository encontroPosRepository;

    private AutorizacaoTerritorial autorizacao() {
        return new AutorizacaoTerritorial(
                new EscopoTerritorialResolver(), encontroRepository, paroquiaRepository, encontroPosRepository);
    }

    @AfterEach
    void limparContexto() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAllowEncontroWhenItIsInTheUsersParoquia() {
        autenticar("PAROQUIA", Map.of("paroquia_id", PAROQUIA.toString()));
        when(encontroRepository.buscarTerritorio(ENCONTRO))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerEncontro(ENCONTRO)).isTrue();
    }

    @Test
    void shouldDenyEncontroOfAnotherParoquia() {
        autenticar("PAROQUIA", Map.of("paroquia_id", UUID.randomUUID().toString()));
        when(encontroRepository.buscarTerritorio(ENCONTRO))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerEncontro(ENCONTRO)).isFalse();
    }

    @Test
    void shouldAllowEncontroOfAnotherParoquiaWhenUserIsSetorialOfTheSameSetor() {
        autenticar("SETORIAL", Map.of("setor_id", SETOR.toString()));
        when(encontroRepository.buscarTerritorio(ENCONTRO))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerEncontro(ENCONTRO)).isTrue();
    }

    @Test
    void shouldAllowAnyEncontroWhenUserIsNacional() {
        autenticar("NACIONAL", Map.of());
        when(encontroRepository.buscarTerritorio(ENCONTRO))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerEncontro(ENCONTRO)).isTrue();
    }

    /** Parâmetro opcional ausente: a consulta por trás já é filtrada pelo escopo. */
    @Test
    void shouldAllowWhenIdIsNull() {
        assertThat(autorizacao().podeVerEncontro(null)).isTrue();
        assertThat(autorizacao().podeVerParoquia(null)).isTrue();
        assertThat(autorizacao().podeVerEncontroPos(null)).isTrue();
    }

    /** Quem responde 404 é o Use Case: negar aqui revelaria quais ids existem fora do escopo. */
    @Test
    void shouldAllowWhenRecordDoesNotExist() {
        autenticar("PAROQUIA", Map.of("paroquia_id", PAROQUIA.toString()));
        when(encontroRepository.buscarTerritorio(any())).thenReturn(Optional.empty());

        assertThat(autorizacao().podeVerEncontro(ENCONTRO)).isTrue();
    }

    @Test
    void shouldDenyParoquiaOutsideTheUsersDiocese() {
        autenticar("DIOCESE", Map.of("diocese_id", UUID.randomUUID().toString()));
        when(paroquiaRepository.buscarTerritorio(PAROQUIA))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerParoquia(PAROQUIA)).isFalse();
    }

    @Test
    void shouldDenyEncontroPosOfAnotherParoquia() {
        UUID encontroPosId = UUID.randomUUID();
        autenticar("POS_ENCONTRO", Map.of("paroquia_id", UUID.randomUUID().toString()));
        when(encontroPosRepository.buscarTerritorio(encontroPosId))
                .thenReturn(Optional.of(new Territorio(PAROQUIA, SETOR, DIOCESE)));

        assertThat(autorizacao().podeVerEncontroPos(encontroPosId)).isFalse();
    }

    private void autenticar(String perfil, Map<String, Object> claims) {
        Jwt.Builder builder = Jwt.withTokenValue("token").header("alg", "none").subject("e2e");
        claims.forEach(builder::claim);
        Jwt jwt = builder.claim("sub", "e2e").build();
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new JwtAuthenticationToken(jwt, List.of(new SimpleGrantedAuthority("ROLE_" + perfil))));
    }
}
