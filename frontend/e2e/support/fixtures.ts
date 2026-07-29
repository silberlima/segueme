import { test as base, expect, type Page } from '@playwright/test'
import { MASSA, USUARIO_E2E } from './config'

interface PosEncontroFixtures {
  /** Página já autenticada de verdade no Keycloak e com o perfil Pós-Encontro ativo. */
  paginaPosEncontro: Page
}

export const test = base.extend<PosEncontroFixtures>({
  paginaPosEncontro: async ({ page }, use) => {
    await autenticar(page)
    await ativarPerfilPosEncontro(page)
    await use(page)
  },
})

export { expect }

/**
 * Login pela própria tela, com senha real trocada por token no Keycloak. A sessão vive em
 * `sessionStorage` (ver `AuthProvider`), que o `storageState` do Playwright não persiste — por isso
 * cada teste loga de novo em vez de reaproveitar estado gravado.
 */
async function autenticar(page: Page): Promise<void> {
  await page.goto('/login')
  await page.getByLabel('E-mail').fill(USUARIO_E2E.email)
  await page.getByLabel('Senha').fill(USUARIO_E2E.password)
  await page.getByRole('button', { name: 'Entrar' }).click()

  await expect(page.getByLabel('Perfil de acesso')).toBeVisible()
}

async function ativarPerfilPosEncontro(page: Page): Promise<void> {
  await page.getByLabel('Perfil de acesso').selectOption({ label: 'Pós-Encontro' })
  await expect(menuLateral(page).getByRole('link', { name: 'Presenças' })).toBeVisible()
}

/**
 * O menu lateral. Os locators de navegação são presos a ele porque o Mapa de Navegação exibe cards
 * com links de mesmo nome — sem o escopo, "Presenças" casaria com dois elementos.
 */
function menuLateral(page: Page) {
  return page.getByRole('complementary')
}

/** Navega pelo menu lateral do perfil — o mesmo caminho que a equipe usaria. */
export async function irParaTela(page: Page, titulo: string): Promise<void> {
  await menuLateral(page).getByRole('link', { name: titulo }).click()
  await expect(page.getByRole('heading', { name: titulo })).toBeVisible()
}

/**
 * Os `<select>` do design system são envolvidos pelo próprio `<label>`, então o nome acessível
 * concatena o rótulo com o texto das opções ("ParóquiaSelecione a paróquia"). Os locators abaixo
 * ancoram no início do rótulo para não casar com o seletor de perfil (que tem a opção "Paróquia")
 * nem com o de encontro pós (cujo placeholder cita "paróquia").
 */
export function campoParoquia(page: Page) {
  return page.getByRole('combobox', { name: /^Paróquia/ })
}

export function campoEncontroPos(page: Page) {
  return page.getByRole('combobox', { name: /^Encontro pós/ })
}

export async function selecionarParoquiaE2E(page: Page): Promise<void> {
  await campoParoquia(page).selectOption({ label: MASSA.paroquiaNome })
}

/** Data única por execução, para não colidir com a constraint de um encontro pós por paróquia/dia. */
export function dataDeTeste(diasAtras: number): string {
  const data = new Date()
  data.setUTCDate(data.getUTCDate() - diasAtras)
  return data.toISOString().slice(0, 10)
}

export function dataFormatada(iso: string): string {
  const [ano, mes, dia] = iso.split('-')
  return `${dia}/${mes}/${ano}`
}

/** Rótulo da opção do seletor de encontro pós, como a tela o monta. */
export function rotuloEncontroPos(iso: string, tema?: string): string {
  return tema ? `${dataFormatada(iso)} — ${tema}` : dataFormatada(iso)
}

/** Célula "Situação atual" da linha do jovem — evita confundir o badge com o botão de mesmo texto. */
export function situacaoDe(page: Page, nome: string) {
  return page.getByRole('row', { name: new RegExp(nome) }).locator('td').nth(1)
}

export async function agendarEncontroPos(page: Page, iso: string, tema?: string): Promise<void> {
  await page.getByLabel('Data', { exact: true }).fill(iso)
  if (tema) {
    await page.getByLabel('Tema (opcional)').fill(tema)
  }
  await page.getByRole('button', { name: 'Agendar' }).click()
  await expect(campoEncontroPos(page)).toHaveValue(/.+/)
}

export async function abrirChamada(page: Page, iso: string, tema?: string): Promise<void> {
  await campoEncontroPos(page).selectOption({ label: rotuloEncontroPos(iso, tema) })
}

/**
 * Agenda um encontro pós e salva a chamada — o caminho que a equipe percorre na tela, usado pelas
 * specs de Frequência e Prioridades para produzir dado real em vez de depender de outra spec.
 */
export async function registrarChamada(
  page: Page,
  iso: string,
  presentes: string[],
  ausentes: string[],
): Promise<void> {
  await agendarEncontroPos(page, iso)

  for (const nome of presentes) {
    await page.getByRole('row', { name: new RegExp(nome) }).getByRole('button', { name: 'Presente' }).click()
  }
  for (const nome of ausentes) {
    await page.getByRole('row', { name: new RegExp(nome) }).getByRole('button', { name: 'Ausente' }).click()
  }

  await salvarChamada(page)

  for (const nome of presentes) {
    await expect(situacaoDe(page, nome)).toHaveText('Presente')
  }
  for (const nome of ausentes) {
    await expect(situacaoDe(page, nome)).toHaveText('Ausente')
  }
}

/**
 * Salva e espera a confirmação da tela. Só olhar o badge não basta: a marcação aparece assim que é
 * clicada (estado local), então seguir para a tela de Frequência sem esperar o PUT concluir lia a
 * apuração antes de a presença existir no banco.
 */
export async function salvarChamada(page: Page): Promise<void> {
  const resposta = page.waitForResponse(
    (retorno) => retorno.request().method() === 'PUT' && retorno.url().includes('/presencas'),
  )
  await page.getByRole('button', { name: 'Salvar chamada' }).click()
  expect((await resposta).ok()).toBeTruthy()
  await expect(page.getByText('Chamada salva.')).toBeVisible()
}
