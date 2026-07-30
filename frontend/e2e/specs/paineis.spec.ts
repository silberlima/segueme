import type { Page } from '@playwright/test'
import { MASSA } from '../support/config'
import {
  ativarPerfil,
  dataDeTeste,
  expect,
  irParaTela,
  menuLateral,
  registrarChamada,
  selecionarParoquiaE2E,
  test,
} from '../support/fixtures'
import { limparEncontrosPosDaMassa } from '../support/seed'

const [ANA, BRUNO] = MASSA.jovens

/**
 * Painéis por perfil (Fase 12, RF-053, RN-DASH-01/02) contra o stack real. O usuário de E2E tem os
 * perfis Pós-Encontro e Paróquia, então a suíte cobre esses dois painéis de ponta a ponta e a regra
 * de que um perfil não alcança o painel do outro.
 */
test.describe('Dashboards por perfil', () => {
  test.beforeEach(() => limparEncontrosPosDaMassa())

  test('entrar no sistema leva ao painel do perfil ativo', async ({ paginaPosEncontro: page }) => {
    await page.goto('/')

    await expect(page.getByRole('heading', { name: 'Painel Pós-Encontro' })).toBeVisible()
    await expect(page).toHaveURL(/\/painel\/pos-encontro$/)
  })

  /** RN-DASH-01: cada perfil só enxerga o painel mapeado para ele; a troca é pela troca de perfil. */
  test('o menu mostra apenas o painel do perfil ativo', async ({ paginaPosEncontro: page }) => {
    await expect(menuLateral(page).getByRole('link', { name: 'Painel Pós-Encontro' })).toBeVisible()
    await expect(menuLateral(page).getByRole('link', { name: 'Painel da Paróquia' })).toHaveCount(0)

    await ativarPerfil(page, 'Paróquia')

    await expect(menuLateral(page).getByRole('link', { name: 'Painel da Paróquia' })).toBeVisible()
    await expect(menuLateral(page).getByRole('link', { name: 'Painel Pós-Encontro' })).toHaveCount(0)
  })

  test('o Painel Pós-Encontro apura frequência real da chamada registrada', async ({
    paginaPosEncontro: page,
  }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, dataDeTeste(21), [ANA.nome], [BRUNO.nome])
    await registrarChamada(page, dataDeTeste(14), [ANA.nome], [BRUNO.nome])

    await irParaTela(page, 'Painel Pós-Encontro')

    // Os dois jovens da massa são acompanhados; Ana 2/2 (Alta) e Bruno 0/2 (Baixa) → média 50%.
    await expect(kpi(page, 'Jovens acompanhados')).toContainText('2')
    await expect(kpi(page, 'Presença média')).toContainText('50%')
    await expect(kpi(page, 'Prioridade alta')).toContainText('1')

    const ranking = page.getByRole('row')
    await expect(ranking.nth(1)).toContainText(ANA.nome)
    await expect(ranking.nth(1)).toContainText('Alta')
    await expect(ranking.nth(2)).toContainText(BRUNO.nome)
    await expect(ranking.nth(2)).toContainText('Baixa')
  })

  test('o Painel Pós-Encontro parte de zero quando não há chamada', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Painel Pós-Encontro')

    await expect(kpi(page, 'Jovens acompanhados')).toContainText('2')
    await expect(kpi(page, 'Presença média')).toContainText('0%')
    await expect(kpi(page, 'Encontros pós no mês')).toContainText('0')
    await expect(page.getByText('Nenhum encontro pós agendado.')).toBeVisible()
  })

  test('o Painel da Paróquia mostra o estado vazio sem Encontro criado', async ({
    paginaPosEncontro: page,
  }) => {
    await ativarPerfil(page, 'Paróquia')
    await irParaTela(page, 'Painel da Paróquia')

    // A massa de E2E não tem Encontro: o painel carrega zerado e oferece "Novo encontro".
    await expect(kpi(page, 'Encontros realizados')).toContainText('0')
    await expect(kpi(page, 'Pendências')).toContainText('0')
    await expect(page.getByText('Esta paróquia ainda não criou nenhum Encontro.')).toBeVisible()
    await expect(page.getByRole('button', { name: 'Novo encontro' })).toBeVisible()
  })
})

/** Cartão de KPI da faixa de indicadores — cada um é uma região nomeada pelo próprio rótulo. */
function kpi(page: Page, rotulo: string) {
  return page.getByRole('region', { name: rotulo })
}
