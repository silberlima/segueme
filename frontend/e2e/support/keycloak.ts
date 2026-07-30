import { STACK, USUARIO_E2E, USUARIO_SETORIAL_E2E } from './config'

/**
 * Provisiona no Keycloak o usuário que os testes usam para logar de verdade pela tela de login. O
 * realm importado pelo compose (`segueme-realm.json`) traz as 10 roles mas nenhum usuário, então
 * criar o usuário é responsabilidade do setup — e não do realm compartilhado, que é o ambiente de
 * desenvolvimento de todo mundo.
 */
export async function provisionarUsuarioE2E(paroquiaId: string): Promise<void> {
  const adminToken = await obterTokenAdmin()
  const userId = await criarOuAtualizarUsuario(adminToken, paroquiaId)
  await definirSenha(adminToken, userId, USUARIO_E2E.password)
  await atribuirRoles(adminToken, userId, USUARIO_E2E.roles)
}

/**
 * Provisiona o usuário Setorial da Fase 14, com a claim `setor_id` do setor semeado — é ela que o
 * backend usa para decidir o que este perfil enxerga e pode decidir (RN-04/RNF-06).
 */
export async function provisionarUsuarioSetorialE2E(setorId: string, paroquiaId: string): Promise<void> {
  const adminToken = await obterTokenAdmin()
  const representacao = {
    username: USUARIO_SETORIAL_E2E.username,
    email: USUARIO_SETORIAL_E2E.email,
    firstName: USUARIO_SETORIAL_E2E.primeiroNome,
    lastName: USUARIO_SETORIAL_E2E.sobrenome,
    enabled: true,
    emailVerified: true,
    attributes: { setor_id: [setorId], paroquia_id: [paroquiaId] },
  }

  const existente = await buscarPorUsername(adminToken, USUARIO_SETORIAL_E2E.username)
  if (existente) {
    await admin(adminToken, `users/${existente}`, 'PUT', representacao)
  } else {
    await admin(adminToken, 'users', 'POST', representacao)
  }

  const userId = await exigirUsuario(adminToken, USUARIO_SETORIAL_E2E.username)
  await definirSenha(adminToken, userId, USUARIO_SETORIAL_E2E.password)
  await atribuirRoles(adminToken, userId, USUARIO_SETORIAL_E2E.roles)
}

/**
 * Id do usuário de E2E no Keycloak — o mesmo valor que chega ao backend como `sub` do token e que
 * identifica o dono das notificações, preferências e relatórios. As specs da Fase 13 precisam dele
 * para limpar só os próprios dados, sem varrer as tabelas inteiras do banco de desenvolvimento.
 */
export async function obterIdDoUsuarioE2E(): Promise<string> {
  const id = await buscarUsuario(await obterTokenAdmin())
  if (!id) {
    throw new Error('Usuário de E2E não encontrado no Keycloak. O global setup rodou?')
  }
  return id
}

/** Id do usuário Setorial de E2E — usado pela suíte da Fase 14 para limpar só as notificações dele. */
export async function obterIdDoUsuarioSetorialE2E(): Promise<string> {
  return exigirUsuario(await obterTokenAdmin(), USUARIO_SETORIAL_E2E.username)
}

async function obterTokenAdmin(): Promise<string> {
  const resposta = await fetch(`${STACK.keycloakUrl}/realms/master/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'password',
      client_id: 'admin-cli',
      username: STACK.admin.username,
      password: STACK.admin.password,
    }),
  })
  if (!resposta.ok) {
    throw new Error(
      `Não foi possível autenticar no Keycloak como admin (${resposta.status}). Confira KEYCLOAK_ADMIN/KEYCLOAK_ADMIN_PASSWORD.`,
    )
  }
  return ((await resposta.json()) as { access_token: string }).access_token
}

async function criarOuAtualizarUsuario(adminToken: string, paroquiaId: string): Promise<string> {
  const representacao = {
    username: USUARIO_E2E.username,
    email: USUARIO_E2E.email,
    firstName: 'Equipe',
    lastName: 'Pós-Encontro E2E',
    enabled: true,
    emailVerified: true,
    attributes: { paroquia_id: [paroquiaId] },
  }

  const existente = await buscarUsuario(adminToken)
  if (existente) {
    await admin(adminToken, `users/${existente}`, 'PUT', representacao)
    return existente
  }

  await admin(adminToken, 'users', 'POST', representacao)
  const criado = await buscarUsuario(adminToken)
  if (!criado) {
    throw new Error('Usuário de E2E não encontrado no Keycloak logo após ser criado.')
  }
  return criado
}

async function buscarUsuario(adminToken: string): Promise<string | null> {
  return buscarPorUsername(adminToken, USUARIO_E2E.username)
}

async function buscarPorUsername(adminToken: string, username: string): Promise<string | null> {
  const usuarios = (await admin(
    adminToken,
    `users?username=${encodeURIComponent(username)}&exact=true`,
    'GET',
  )) as { id: string }[]
  return usuarios.length > 0 ? usuarios[0].id : null
}

async function exigirUsuario(adminToken: string, username: string): Promise<string> {
  const id = await buscarPorUsername(adminToken, username)
  if (!id) {
    throw new Error(`Usuário ${username} não encontrado no Keycloak logo após ser criado.`)
  }
  return id
}

async function definirSenha(adminToken: string, userId: string, senha: string): Promise<void> {
  await admin(adminToken, `users/${userId}/reset-password`, 'PUT', {
    type: 'password',
    value: senha,
    temporary: false,
  })
}

async function atribuirRoles(adminToken: string, userId: string, nomes: string[]): Promise<void> {
  const roles = []
  for (const nome of nomes) {
    roles.push(await admin(adminToken, `roles/${encodeURIComponent(nome)}`, 'GET'))
  }
  await admin(adminToken, `users/${userId}/role-mappings/realm`, 'POST', roles)
}

async function admin(token: string, caminho: string, metodo: string, corpo?: unknown): Promise<unknown> {
  const resposta = await fetch(`${STACK.keycloakUrl}/admin/realms/${STACK.realm}/${caminho}`, {
    method: metodo,
    headers: {
      Authorization: `Bearer ${token}`,
      ...(corpo === undefined ? {} : { 'Content-Type': 'application/json' }),
    },
    body: corpo === undefined ? undefined : JSON.stringify(corpo),
  })
  if (!resposta.ok) {
    throw new Error(`Keycloak admin ${metodo} ${caminho} falhou: ${resposta.status} ${await resposta.text()}`)
  }
  return resposta.status === 204 || resposta.headers.get('content-length') === '0'
    ? null
    : await resposta.json().catch(() => null)
}
