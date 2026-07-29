import { STACK, USUARIO_E2E } from './config'

/**
 * Provisiona no Keycloak o usuário que os testes usam para logar de verdade pela tela de login. O
 * realm importado pelo compose (`segueme-realm.json`) traz as 10 roles mas nenhum usuário, então
 * criar o usuário é responsabilidade do setup — e não do realm compartilhado, que é o ambiente de
 * desenvolvimento de todo mundo.
 */
export async function provisionarUsuarioE2E(paroquiaId: string): Promise<void> {
  const adminToken = await obterTokenAdmin()
  const userId = await criarOuAtualizarUsuario(adminToken, paroquiaId)
  await definirSenha(adminToken, userId)
  await atribuirRoles(adminToken, userId)
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
  const usuarios = (await admin(
    adminToken,
    `users?username=${encodeURIComponent(USUARIO_E2E.username)}&exact=true`,
    'GET',
  )) as { id: string }[]
  return usuarios.length > 0 ? usuarios[0].id : null
}

async function definirSenha(adminToken: string, userId: string): Promise<void> {
  await admin(adminToken, `users/${userId}/reset-password`, 'PUT', {
    type: 'password',
    value: USUARIO_E2E.password,
    temporary: false,
  })
}

async function atribuirRoles(adminToken: string, userId: string): Promise<void> {
  const roles = []
  for (const nome of USUARIO_E2E.roles) {
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
