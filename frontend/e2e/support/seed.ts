import { execFileSync } from 'node:child_process'
import { MASSA, STACK } from './config'

/**
 * Semeia a massa mínima do módulo direto no PostgreSQL: uma paróquia e dois jovens já acompanhados
 * (situação SEGUIMISTA).
 *
 * <p>A situação SEGUIMISTA só é alcançável pela API percorrendo a Fase 5 inteira (pré-inscrição →
 * ficha, que exige antes um Encontro com equipe dirigente e equipe comando montadas). Fazer isso a
 * cada execução transformaria o E2E da Fase 11 num teste da Fase 5 e o deixaria refém dela — a massa
 * entra como fixture, e a asserção fica sobre o fluxo desta fase, exercitado pela UI de ponta a ponta.
 *
 * <p>Idempotente: apaga a própria massa (inclusive encontros pós e presenças criados por execuções
 * anteriores) antes de recriar, para não sujar o banco de desenvolvimento nem depender de estado.
 */
export function semearMassaPosEncontro(): void {
  psql(`
    begin;

    delete from frequencia_pos_encontro
     where encontro_pos_id in (select id from encontro_pos where paroquia_id = '${MASSA.paroquiaId}')
        or pessoa_id in (${MASSA.jovens.map((jovem) => `'${jovem.id}'`).join(', ')});
    delete from encontro_pos where paroquia_id = '${MASSA.paroquiaId}';
    delete from pessoa where paroquia_id = '${MASSA.paroquiaId}';
    delete from paroquia where id = '${MASSA.paroquiaId}';
    delete from setor where id = '${MASSA.setorId}';
    delete from diocese where id = '${MASSA.dioceseId}';

    insert into diocese (id, nome, created_at, updated_at, deleted)
      values ('${MASSA.dioceseId}', 'Diocese E2E Pós-Encontro', now(), now(), false);
    insert into setor (id, nome, diocese_id, created_at, updated_at, deleted)
      values ('${MASSA.setorId}', 'Setor E2E Pós-Encontro', '${MASSA.dioceseId}', now(), now(), false);
    insert into paroquia (id, nome, setor_id, created_at, updated_at, deleted)
      values ('${MASSA.paroquiaId}', '${MASSA.paroquiaNome}', '${MASSA.setorId}', now(), now(), false);

    ${MASSA.jovens
      .map(
        (jovem) => `insert into pessoa (
        id, nome_completo, data_nascimento, telefone, estado_civil, paroquia_id, situacao,
        created_at, updated_at, deleted
      ) values (
        '${jovem.id}', '${jovem.nome}', '2005-01-01', '(11) 90000-0000', 'SOLTEIRO',
        '${MASSA.paroquiaId}', 'SEGUIMISTA', now(), now(), false
      );`,
      )
      .join('\n    ')}

    commit;
  `)
}

/**
 * Zera só os encontros pós e as presenças da paróquia de teste, preservando a massa de pessoas.
 * Cada teste chama isso no início para não herdar os encontros criados pelo anterior — a apuração de
 * frequência conta todos os encontros do período, então estado residual mudaria os percentuais
 * esperados (`skills/09-testes.md`: testes independentes, nunca dependentes da ordem).
 */
export function limparEncontrosPosDaMassa(): void {
  psql(`
    begin;
    delete from frequencia_pos_encontro
     where encontro_pos_id in (select id from encontro_pos where paroquia_id = '${MASSA.paroquiaId}');
    delete from encontro_pos where paroquia_id = '${MASSA.paroquiaId}';
    commit;
  `)
}

/**
 * Zera relatórios, preferências e notificações do usuário de E2E (Fase 13). Filtra pelo `sub` dele
 * em vez de esvaziar as tabelas: o banco é o de desenvolvimento, e apagar dado de outro usuário
 * seria efeito colateral do teste.
 */
export function limparDadosDeSistemaDoUsuarioE2E(usuarioId: string): void {
  psql(`
    begin;
    delete from relatorio where solicitante_id = '${usuarioId}';
    delete from preferencia_notificacao where usuario_id = '${usuarioId}';
    delete from notificacao where destinatario_id = '${usuarioId}';
    commit;
  `)
}

function psql(sql: string): void {
  try {
    execFileSync(
      'docker',
      [
        'exec',
        '-i',
        STACK.postgres.container,
        'psql',
        '-v',
        'ON_ERROR_STOP=1',
        '-q',
        '-U',
        STACK.postgres.username,
        '-d',
        STACK.postgres.database,
      ],
      { input: sql, stdio: ['pipe', 'pipe', 'pipe'] },
    )
  } catch (erro) {
    const detalhe = erro instanceof Error && 'stderr' in erro ? String((erro as { stderr: Buffer }).stderr) : ''
    throw new Error(
      `Falha ao semear a massa de E2E no PostgreSQL (container "${STACK.postgres.container}").\n` +
        'Suba a infraestrutura com:\n' +
        '  docker compose -f docker-compose.yml -f docker-compose.e2e.yml up -d postgres keycloak\n' +
        `Detalhe: ${detalhe}`,
      { cause: erro },
    )
  }
}
