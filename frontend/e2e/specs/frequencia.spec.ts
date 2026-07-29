import { MASSA } from '../support/config'
import {
  dataDeTeste,
  expect,
  irParaTela,
  registrarChamada,
  selecionarParoquiaE2E,
  test,
} from '../support/fixtures'
import { limparEncontrosPosDaMassa } from '../support/seed'

const [ANA, BRUNO] = MASSA.jovens

/** Tela Frequência (`screen_frequencia`, RF-059, RN-POS-03). */
test.describe('Pós-Encontro — Frequência', () => {
  test.beforeEach(() => limparEncontrosPosDaMassa())

  test('apura a frequência de todo o pool, inclusive quem só faltou', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, dataDeTeste(10), [ANA.nome], [BRUNO.nome])

    await irParaTela(page, 'Frequência')
    await selecionarParoquiaE2E(page)

    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('1 de 1')
    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('100%')
    // Bruno não tem presença: continua na lista com 0, em vez de desaparecer da apuração.
    await expect(page.getByRole('row', { name: new RegExp(BRUNO.nome) })).toContainText('0 de 1')
    await expect(page.getByRole('row', { name: new RegExp(BRUNO.nome) })).toContainText('0%')
  })

  test('recorta a apuração pelo período informado', async ({ paginaPosEncontro: page }) => {
    const antigo = dataDeTeste(200)
    const recente = dataDeTeste(11)

    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, antigo, [BRUNO.nome], [ANA.nome])
    await registrarChamada(page, recente, [ANA.nome], [BRUNO.nome])

    await irParaTela(page, 'Frequência')
    await selecionarParoquiaE2E(page)

    // Sem filtro: os dois encontros entram, cada um com uma presença.
    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('1 de 2')
    await expect(page.getByRole('row', { name: new RegExp(BRUNO.nome) })).toContainText('1 de 2')

    // Com o corte inferior depois do encontro antigo, só o recente é apurado.
    await page.getByLabel('De', { exact: true }).fill(dataDeTeste(30))
    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('1 de 1')
    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('100%')
    await expect(page.getByRole('row', { name: new RegExp(BRUNO.nome) })).toContainText('0 de 1')
  })
})
