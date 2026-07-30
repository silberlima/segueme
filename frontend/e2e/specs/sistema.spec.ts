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
import { obterIdDoUsuarioE2E } from '../support/keycloak'
import { limparDadosDeSistemaDoUsuarioE2E, limparEncontrosPosDaMassa } from '../support/seed'

const [ANA, BRUNO] = MASSA.jovens

/**
 * Módulo Sistema (Fase 13, RF-056/057, RN-SISTEMA-01/02, UC-22) contra o stack real: relatórios
 * gerados de ponta a ponta, notificações em tela cheia e preferências de conta persistidas.
 */
test.describe('Sistema — relatórios, notificações e configurações', () => {
  let usuarioId: string

  test.beforeAll(async () => {
    usuarioId = await obterIdDoUsuarioE2E()
  })

  test.beforeEach(() => {
    limparEncontrosPosDaMassa()
    limparDadosDeSistemaDoUsuarioE2E(usuarioId)
  })

  /** UC-22: escolher o tipo → gerar → o arquivo aparece em "Gerados recentemente" e é baixável. */
  test('gera um relatório de ponta a ponta a partir de dado real', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, dataDeTeste(21), [ANA.nome], [BRUNO.nome])

    await irParaTela(page, 'Relatórios')
    await gerar(page, 'Pós-encontro')

    const linha = linhaDoRelatorio(page, 'Pós-encontro')
    await expect(linha).toContainText('Pronto', { timeout: 30_000 })
    // Os dois jovens acompanhados da massa, uma linha cada.
    await expect(linha).toContainText('2')

    const download = page.waitForEvent('download')
    await linha.getByRole('button', { name: 'Baixar' }).click()
    expect((await download).suggestedFilename()).toBe('relatorio-pos-encontro.csv')
  })

  /**
   * RF-056: os cartões vêm do backend, já recortados pelos perfis concedidos ao usuário. O recorte
   * é pelas roles do token, não pelo perfil ativo na tela — como em toda autorização do sistema
   * (mesmo critério dos painéis da Fase 12). O usuário de E2E tem Pós-Encontro e Paróquia, então
   * enxerga os seis tipos; que um perfil sem o tipo é recusado está coberto no backend
   * (`RelatoriosControllerIT`).
   */
  test('os cartões de tipo vêm do backend, recortados pelos perfis do usuário', async ({
    paginaPosEncontro: page,
  }) => {
    await irParaTela(page, 'Relatórios')

    for (const titulo of ['Pessoas', 'Encontros', 'Inscrições', 'Equipes e convites', 'Financeiro', 'Pós-encontro']) {
      await expect(cartaoDeTipo(page, titulo)).toBeVisible()
    }
  })

  /** A falha da geração assíncrona é registrada e explicada, em vez de ficar "gerando" para sempre. */
  test('registra o motivo quando o relatório não pode ser gerado', async ({
    paginaPosEncontro: page,
  }) => {
    await ativarPerfil(page, 'Paróquia')
    await irParaTela(page, 'Relatórios')

    // A paróquia da massa não tem Encontro; o relatório financeiro é sobre o Encontro corrente.
    await gerar(page, 'Financeiro')

    const linha = linhaDoRelatorio(page, 'Financeiro')
    await expect(linha).toContainText('Falhou', { timeout: 30_000 })
    await expect(linha).toContainText('nenhum Encontro')
  })

  /** RN-SISTEMA-01/02: a geração notifica o solicitante e o badge do sino reflete a não lida. */
  test('a conclusão do relatório chega como notificação na tela cheia', async ({
    paginaPosEncontro: page,
  }) => {
    await irParaTela(page, 'Relatórios')
    await gerar(page, 'Pós-encontro')
    await expect(linhaDoRelatorio(page, 'Pós-encontro')).toContainText('Pronto', { timeout: 30_000 })

    await irParaTela(page, 'Notificações')

    const notificacao = page.getByRole('listitem').filter({ hasText: 'Relatório pronto' })
    await expect(notificacao).toBeVisible()
    await expect(notificacao.getByLabel('Não lida')).toBeVisible()

    await page.getByRole('button', { name: 'Marcar todas como lidas' }).click()

    await expect(page.getByRole('button', { name: 'Marcar todas como lidas' })).toHaveCount(0)
    await expect(notificacao.getByLabel('Não lida')).toHaveCount(0)
  })

  /** RF-057: o toggle desligado é persistido e passa a barrar a notificação daquele tipo. */
  test('desligar um tipo em Configurações impede a notificação correspondente', async ({
    paginaPosEncontro: page,
  }) => {
    await irParaTela(page, 'Configurações')

    await page.getByRole('switch', { name: 'Positiva' }).click()
    await page.getByRole('button', { name: 'Salvar' }).click()
    await expect(page.getByText('Preferências salvas.')).toBeVisible()

    await page.reload()
    await expect(page.getByRole('switch', { name: 'Positiva' })).toHaveAttribute('aria-checked', 'false')

    await irParaTela(page, 'Relatórios')
    await gerar(page, 'Pós-encontro')
    await expect(linhaDoRelatorio(page, 'Pós-encontro')).toContainText('Pronto', { timeout: 30_000 })

    await irParaTela(page, 'Notificações')
    await expect(page.getByText('Nenhuma notificação até agora.')).toBeVisible()
  })

  /** O item Sistema é visível a qualquer perfil autenticado; Relatórios, não (SEGUIDOR não gera). */
  test('o menu do Sistema leva às três telas da fase', async ({ paginaPosEncontro: page }) => {
    await expect(menuLateral(page).getByRole('link', { name: 'Notificações' })).toBeVisible()
    await expect(menuLateral(page).getByRole('link', { name: 'Relatórios' })).toBeVisible()
    await expect(menuLateral(page).getByRole('link', { name: 'Configurações' })).toBeVisible()
  })
})

/** Cartão de um tipo de relatório — é uma região nomeada pelo título, como no ReportTypeCard. */
function cartaoDeTipo(page: Page, titulo: string) {
  return page.getByRole('region', { name: titulo })
}

async function gerar(page: Page, titulo: string): Promise<void> {
  await cartaoDeTipo(page, titulo).getByRole('button', { name: 'Gerar relatório' }).click()
}

/** Linha de "Gerados recentemente" do tipo informado. */
function linhaDoRelatorio(page: Page, titulo: string) {
  return page.getByRole('row').filter({ hasText: titulo })
}
