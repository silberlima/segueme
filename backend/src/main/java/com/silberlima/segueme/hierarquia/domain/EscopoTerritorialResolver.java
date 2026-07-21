package com.silberlima.segueme.hierarquia.domain;

import com.silberlima.segueme.acesso.domain.Perfil;
import com.silberlima.segueme.domain.exception.BusinessRuleViolationException;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;

/**
 * Resolve o {@link EscopoTerritorial} de um usuário autenticado (RN-04) a partir dos perfis
 * concedidos (roles do Keycloak) e das claims territoriais customizadas do JWT
 * ({@code diocese_id}/{@code setor_id}/{@code paroquia_id} — atributos de usuário no Keycloak,
 * análogos ao mapeamento de roles já usado para {@link Perfil}). Prioriza o perfil de maior
 * alcance territorial quando o usuário possui mais de um.
 */
@Service
public class EscopoTerritorialResolver {

    public EscopoTerritorial resolve(
            List<Perfil> perfis, String dioceseIdClaim, String setorIdClaim, String paroquiaIdClaim) {
        if (perfis.contains(Perfil.NACIONAL)) {
            return new EscopoTerritorial.Nacional();
        }
        if (perfis.contains(Perfil.DIOCESE)) {
            return new EscopoTerritorial.DeDiocese(exigirClaim(dioceseIdClaim, "diocese_id"));
        }
        if (perfis.contains(Perfil.SETORIAL)) {
            return new EscopoTerritorial.DeSetor(exigirClaim(setorIdClaim, "setor_id"));
        }
        if (perfis.contains(Perfil.PAROQUIA)) {
            return new EscopoTerritorial.DeParoquia(exigirClaim(paroquiaIdClaim, "paroquia_id"));
        }
        throw new BusinessRuleViolationException(
                "Usuário não possui um perfil com escopo territorial atribuído.");
    }

    private UUID exigirClaim(String claim, String nomeClaim) {
        if (claim == null || claim.isBlank()) {
            throw new BusinessRuleViolationException(
                    "Usuário não possui o atributo territorial '" + nomeClaim + "' configurado no Keycloak.");
        }
        try {
            return UUID.fromString(claim);
        } catch (IllegalArgumentException e) {
            throw new BusinessRuleViolationException(
                    "Atributo territorial '" + nomeClaim + "' inválido: " + claim);
        }
    }
}
