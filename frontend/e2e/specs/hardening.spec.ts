import type { Page } from '@playwright/test'
import { MASSA } from '../support/config'
import { expect, irParaTela, menuLateral, test } from '../support/fixtures'
import { obterIdDoUsuarioSetorialE2E } from '../support/keycloak'
import { limparEncontrosPosDaMassa, limparTransferenciasDaMassa } from '../support/seed'

const [ANA] = MASSA.jovens

/**
 * Hardening da Fase 14 contra o stack real: os quatro estados de lista (RNF-02) e a confirmação com
 * justificativa das ações sensíveis (RNF-03/RNF-08), com a notificação automática que a decisão
 * dispara (RNF-07). Nada é mockado — a única interceptação é a que provoca o estado de erro, que
 * por definição não se produz com o backend saudável.
 */
test.describe('Hardening — estados de lista e confirmação de ação sensível', () => {
  let usuarioSetorialId: string

  test.beforeAll(async () => {
    usuarioSetorialId = await obterIdDoUsuarioSetorialE2E()
  })

  test.beforeEach(() => {
    limparEncontrosPosDaMassa()
    limparTransferenciasDaMassa(usuarioSetorialId)
  })

  test.describe('RNF-02: os quatro estados de uma lista', () => {
    /** Carregando: o skeleton do `ListState`, anunciado como região ocupada. */
    test('exibe skeleton enquanto a lista carrega', async ({ paginaPosEncontro: page }) => {
      await page.route('**/api/v1/pos-encontro/prioridades**', async (rota) => {
        await new Promise((resolve) => setTimeout(resolve, 1_500))
        await rota.continue()
      })

      await irParaTela(page, 'Prioridades')
      await selecionarParoquia(page)

      await expect(page.getByRole('status', { name: 'Carregando' })).toBeVisible()
      await expect(page.getByRole('status', { name: 'Carregando' })).toBeHidden({ timeout: 15_000 })
    })

    /** Com dados: a mesma tela, com o backend respondendo normalmente. */
    test('exibe os dados quando a consulta responde', async ({ paginaPosEncontro: page }) => {
      await irParaTela(page, 'Prioridades')
      await selecionarParoquia(page)

      await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toBeVisible()
    })

    /** Vazio: com orientação de próxima ação, nunca uma tela em branco. */
    test('exibe o estado vazio com orientação antes de escolher a paróquia', async ({
      paginaPosEncontro: page,
    }) => {
      await irParaTela(page, 'Prioridades')

      await expect(page.getByText('Selecione a paróquia para ver o ranking de prioridade.')).toBeVisible()
      await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toHaveCount(0)
    })

    /**
     * Erro: mensagem e ação de nova tentativa. O teste prova que o botão **funciona** — solta a
     * interceptação e clica nele, e a lista carrega sem recarregar a página.
     */
    test('exibe erro com nova tentativa que recupera a lista', async ({ paginaPosEncontro: page }) => {
      await page.route('**/api/v1/pos-encontro/prioridades**', (rota) => rota.abort('failed'))

      await irParaTela(page, 'Prioridades')
      await selecionarParoquia(page)

      const erro = page.getByRole('alert')
      await expect(erro).toContainText('Não foi possível carregar o ranking de prioridade.')

      await page.unroute('**/api/v1/pos-encontro/prioridades**')
      await erro.getByRole('button', { name: 'Tentar novamente' }).click()

      await expect(page.getByRole('alert')).toHaveCount(0)
      await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toBeVisible()
    })
  })

  test.describe('RNF-03: confirmação e justificativa de ação sensível', () => {
    /**
     * Recusa de transferência de ponta a ponta: a Paróquia solicita, o Setorial recusa pelo
     * diálogo — que não libera o confirmar sem justificativa (RNF-08) — e a justificativa fica
     * registrada na lista. O mesmo usuário tem os dois perfis; a troca é pelo seletor do menu, o
     * mesmo caminho do usuário real (RN-ACESSO-02).
     */
    test('recusar transferência exige justificativa no diálogo e registra a decisão', async ({
      paginaSetorial: page,
    }) => {
      await solicitarTransferencia(page, ANA.nome, MASSA.paroquiaDestinoNome)

      await expect(linhaDaTransferencia(page)).toContainText('Pendente')

      await page.getByLabel('Perfil de acesso').selectOption({ label: 'Setorial' })
      await irParaTela(page, 'Transferências')

      await linhaDaTransferencia(page).getByRole('button', { name: 'Recusar' }).click()

      const dialogo = page.getByRole('dialog', { name: 'Recusar transferência' })
      await expect(dialogo).toContainText(ANA.nome)
      const confirmar = dialogo.getByRole('button', { name: 'Recusar transferência' })
      await expect(confirmar).toBeDisabled()

      await dialogo.getByRole('textbox', { name: 'Justificativa da recusa' }).fill('Pendências na paróquia de origem.')
      await expect(confirmar).toBeEnabled()
      await confirmar.click()

      await expect(dialogo).toBeHidden()
      await expect(linhaDaTransferencia(page)).toContainText('Recusada')
      await expect(linhaDaTransferencia(page)).toContainText('Pendências na paróquia de origem.')
    })

    /** Cancelar não decide nada: a solicitação continua pendente. */
    test('cancelar o diálogo mantém a transferência pendente', async ({ paginaSetorial: page }) => {
      await solicitarTransferencia(page, ANA.nome, MASSA.paroquiaDestinoNome)

      await page.getByLabel('Perfil de acesso').selectOption({ label: 'Setorial' })
      await irParaTela(page, 'Transferências')

      await linhaDaTransferencia(page).getByRole('button', { name: 'Recusar' }).click()
      const dialogo = page.getByRole('dialog', { name: 'Recusar transferência' })
      await dialogo.getByRole('button', { name: 'Cancelar' }).click()

      await expect(dialogo).toHaveCount(0)
      await expect(linhaDaTransferencia(page)).toContainText('Pendente')
    })

    /**
     * RNF-07: a decisão do Setorial notifica automaticamente quem solicitou — aqui o mesmo usuário,
     * já que ele fez os dois passos. Sem isso, a paróquia de origem só saberia do desfecho
     * reabrindo a tela.
     */
    test('a recusa notifica automaticamente quem solicitou', async ({ paginaSetorial: page }) => {
      await solicitarTransferencia(page, ANA.nome, MASSA.paroquiaDestinoNome)

      await page.getByLabel('Perfil de acesso').selectOption({ label: 'Setorial' })
      await irParaTela(page, 'Transferências')
      await linhaDaTransferencia(page).getByRole('button', { name: 'Recusar' }).click()
      const dialogo = page.getByRole('dialog', { name: 'Recusar transferência' })
      await dialogo.getByRole('textbox', { name: 'Justificativa da recusa' }).fill('Sem vaga no destino.')
      await dialogo.getByRole('button', { name: 'Recusar transferência' }).click()
      await expect(linhaDaTransferencia(page)).toContainText('Recusada')

      await page.getByRole('button', { name: 'Notificações' }).click()

      const drawer = page.getByRole('dialog', { name: 'Notificações' })
      await expect(drawer.getByText(`Transferência recusada: ${ANA.nome}`)).toBeVisible({ timeout: 15_000 })
      await expect(drawer.getByText('Sem vaga no destino.')).toBeVisible()
    })
  })
})

async function selecionarParoquia(page: Page): Promise<void> {
  await page.getByRole('combobox', { name: /^Paróquia/ }).selectOption({ label: MASSA.paroquiaNome })
}

/**
 * O cartão da única transferência da massa — o seed limpa as anteriores a cada teste. Ancorado no
 * nome da pessoa, o menor elemento que contém tanto o status quanto os botões de decisão.
 */
function linhaDaTransferencia(page: Page) {
  return page.getByText(ANA.nome, { exact: true }).locator('xpath=ancestor::div[contains(@class,"rounded-lg")][1]')
}

async function solicitarTransferencia(page: Page, pessoa: string, destino: string): Promise<void> {
  await menuLateral(page).getByRole('link', { name: 'Transferências' }).click()
  await expect(page.getByRole('heading', { name: 'Transferências' })).toBeVisible()

  await page.getByPlaceholder('Buscar pessoa a transferir...').fill(pessoa.slice(0, 8))
  await page.getByRole('button', { name: pessoa }).click()
  await page.getByRole('combobox', { name: /^Paróquia de destino/ }).selectOption({ label: destino })
  await page.getByRole('button', { name: 'Solicitar transferência' }).click()
}
