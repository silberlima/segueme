package com.silberlima.segueme.dashboards.domain;

/**
 * As pendências que o Painel da Paróquia lista e que também alimentam os badges do menu lateral
 * (RN-DASH-02). São exatamente as três citadas no protótipo (`dashboards-por-perfil.md` 3.4):
 * equipes incompletas, candidatos represados no cadastro de reserva e fichas de inscrição ainda
 * não preenchidas. O rótulo exibido é responsabilidade do frontend.
 */
public enum TipoPendencia {

    EQUIPES_INCOMPLETAS,
    CADASTRO_RESERVA,
    FICHAS_A_PREENCHER
}
