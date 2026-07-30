import { MASSA, STACK, USUARIO_E2E } from './config'
import { provisionarUsuarioE2E, provisionarUsuarioSetorialE2E } from './keycloak'
import { semearMassaPosEncontro } from './seed'

/**
 * Prepara o stack antes da suíte: espera Keycloak e backend responderem, semeia a massa da paróquia
 * e provisiona o usuário de teste com a claim territorial da paróquia semeada.
 */
export default async function globalSetup(): Promise<void> {
  await esperar('Keycloak', `${STACK.keycloakUrl}/realms/${STACK.realm}/.well-known/openid-configuration`)
  await esperar('Backend', `${STACK.apiUrl.replace(/\/api\/v1$/, '')}/actuator/health`)

  semearMassaPosEncontro()
  await provisionarUsuarioE2E(MASSA.paroquiaId)
  await provisionarUsuarioSetorialE2E(MASSA.setorId, MASSA.paroquiaId)
  await validarLogin()
}

async function esperar(nome: string, url: string, tentativas = 60): Promise<void> {
  for (let tentativa = 1; tentativa <= tentativas; tentativa += 1) {
    try {
      const resposta = await fetch(url)
      if (resposta.ok) return
    } catch {
      // serviço ainda subindo
    }
    await new Promise((resolve) => setTimeout(resolve, 2_000))
  }
  throw new Error(
    `${nome} não respondeu em ${url}. Suba a infraestrutura (docker compose -f docker-compose.yml ` +
      '-f docker-compose.e2e.yml up -d postgres keycloak) e o backend (./mvnw spring-boot:run) antes do E2E.',
  )
}

/**
 * Falha cedo e com mensagem clara se o usuário provisionado não conseguir obter token — sem isso, o
 * erro apareceria como um timeout obscuro na tela de login do primeiro teste.
 */
async function validarLogin(): Promise<void> {
  const resposta = await fetch(`${STACK.keycloakUrl}/realms/${STACK.realm}/protocol/openid-connect/token`, {
    method: 'POST',
    headers: { 'Content-Type': 'application/x-www-form-urlencoded' },
    body: new URLSearchParams({
      grant_type: 'password',
      client_id: STACK.clientId,
      username: USUARIO_E2E.username,
      password: USUARIO_E2E.password,
    }),
  })
  if (!resposta.ok) {
    throw new Error(
      `O usuário de E2E não conseguiu autenticar no realm ${STACK.realm} (${resposta.status}): ${await resposta.text()}`,
    )
  }
}
