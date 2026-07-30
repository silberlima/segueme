package com.silberlima.segueme.infrastructure.security;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import com.silberlima.segueme.encontros.domain.EncontroRepository;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorial;
import com.silberlima.segueme.hierarquia.domain.EscopoTerritorialResolver;
import com.silberlima.segueme.hierarquia.domain.ParoquiaRepository;
import com.silberlima.segueme.hierarquia.domain.Territorio;
import com.silberlima.segueme.posencontro.domain.EncontroPosRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Escopo territorial (RN-04, RNF-06) aplicado a endpoints que recebem o id do registro pela
 * requisição, e não uma consulta filtrada pelo escopo: sem isto, ter a role certa bastaria para
 * ler os convites, as equipes, as inscrições ou o financeiro de um Encontro de qualquer paróquia
 * do país, bastando trocar o {@code encontroId} da URL.
 *
 * <p>É chamado do próprio {@code @PreAuthorize} — {@code @escopoTerritorial.podeVerEncontro(#id)}
 * — para que a checagem continue declarativa no endpoint e nenhum Controller ou Use Case tenha
 * regra de permissão escrita à mão.
 *
 * <p>Id nulo devolve {@code true}: o parâmetro é opcional naquele endpoint e a consulta por trás
 * já é filtrada pelo escopo do usuário.
 */
@Component("escopoTerritorial")
public class AutorizacaoTerritorial {

    private final EscopoTerritorialResolver escopoTerritorialResolver;
    private final EncontroRepository encontroRepository;
    private final ParoquiaRepository paroquiaRepository;
    private final EncontroPosRepository encontroPosRepository;

    public AutorizacaoTerritorial(
            EscopoTerritorialResolver escopoTerritorialResolver,
            EncontroRepository encontroRepository,
            ParoquiaRepository paroquiaRepository,
            EncontroPosRepository encontroPosRepository) {
        this.escopoTerritorialResolver = escopoTerritorialResolver;
        this.encontroRepository = encontroRepository;
        this.paroquiaRepository = paroquiaRepository;
        this.encontroPosRepository = encontroPosRepository;
    }

    @Transactional(readOnly = true)
    public boolean podeVerEncontro(UUID encontroId) {
        return encontroId == null || permite(encontroRepository.buscarTerritorio(encontroId));
    }

    @Transactional(readOnly = true)
    public boolean podeVerParoquia(UUID paroquiaId) {
        return paroquiaId == null || permite(paroquiaRepository.buscarTerritorio(paroquiaId));
    }

    @Transactional(readOnly = true)
    public boolean podeVerEncontroPos(UUID encontroPosId) {
        return encontroPosId == null || permite(encontroPosRepository.buscarTerritorio(encontroPosId));
    }

    /**
     * Registro inexistente passa pela autorização: quem responde 404 é o Use Case, não o
     * {@code @PreAuthorize} — negar aqui transformaria "não existe" em 403 e ainda revelaria, pela
     * diferença de resposta, quais ids existem fora do escopo de quem perguntou.
     */
    private boolean permite(Optional<Territorio> territorio) {
        if (territorio.isEmpty()) {
            return true;
        }
        return escopoAtual()
                .map(escopo -> escopo.contem(territorio.get()))
                .orElse(false);
    }

    /**
     * Token sem a claim territorial do seu perfil é usuário mal configurado no Keycloak: aqui isso
     * nega o acesso (403), em vez de propagar o erro de regra de negócio do resolver e virar um 409
     * — a decisão em curso é de autorização, não de negócio.
     */
    private Optional<EscopoTerritorial> escopoAtual() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        List<String> authorities =
                authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).toList();
        Jwt jwt = (Jwt) authentication.getPrincipal();
        try {
            return Optional.of(escopoTerritorialResolver.resolve(
                    Perfil.fromAuthorities(authorities),
                    jwt.getClaimAsString("diocese_id"),
                    jwt.getClaimAsString("setor_id"),
                    jwt.getClaimAsString("paroquia_id")));
        } catch (BusinessRuleViolationException e) {
            return Optional.empty();
        }
    }
}
