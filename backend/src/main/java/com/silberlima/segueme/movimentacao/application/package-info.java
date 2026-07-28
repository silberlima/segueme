/**
 * Camada Application do domínio Movimentação: um Use Case por ação (RF-040 a RF-046) e os DTOs de
 * orquestração que os isolam do formato de request/response da camada API — incluindo
 * {@code ListarSolicitacoesUseCase}, que agrega Transferências e Empréstimos pendentes num inbox
 * único do Setorial sem introduzir uma tabela própria de "Solicitação" (`plano-implementacao.md`).
 */
package com.silberlima.segueme.movimentacao.application;
