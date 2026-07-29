import { MASSA } from '../support/config'
import {
  abrirChamada,
  agendarEncontroPos,
  campoEncontroPos,
  dataDeTeste,
  dataFormatada,
  expect,
  irParaTela,
  salvarChamada,
  selecionarParoquiaE2E,
  situacaoDe,
  test,
} from '../support/fixtures'
import { limparEncontrosPosDaMassa } from '../support/seed'

const [ANA, BRUNO] = MASSA.jovens

/** Tela Presenças (`screen_presencas`, RF-058, UC-20, RN-POS-01). */
test.describe('Pós-Encontro — Presenças', () => {
  test.beforeEach(() => limparEncontrosPosDaMassa())

  test('agenda um encontro pós, faz a chamada e a marcação sobrevive ao recarregamento', async ({
    paginaPosEncontro: page,
  }) => {
    const data = dataDeTeste(3)
    const tema = 'Perseverança E2E'

    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await agendarEncontroPos(page, data, tema)

    await expect(situacaoDe(page, ANA.nome)).toHaveText('Não conferido')
    await expect(situacaoDe(page, BRUNO.nome)).toHaveText('Não conferido')

    await page.getByRole('row', { name: new RegExp(ANA.nome) }).getByRole('button', { name: 'Presente' }).click()
    await page.getByRole('row', { name: new RegExp(BRUNO.nome) }).getByRole('button', { name: 'Ausente' }).click()
    await salvarChamada(page)

    await expect(situacaoDe(page, ANA.nome)).toHaveText('Presente')
    await expect(situacaoDe(page, BRUNO.nome)).toHaveText('Ausente')

    // Recarrega e reabre a mesma chamada: prova que a marcação foi para o banco pela API, e não
    // ficou apenas no estado local da tela.
    await page.reload()
    await selecionarParoquiaE2E(page)
    await abrirChamada(page, data, tema)

    await expect(situacaoDe(page, ANA.nome)).toHaveText('Presente')
    await expect(situacaoDe(page, BRUNO.nome)).toHaveText('Ausente')
  })

  /** RN-POS-01: a mesma paróquia não tem dois encontros pós na mesma data. */
  test('recusa dois encontros pós na mesma data', async ({ paginaPosEncontro: page }) => {
    const data = dataDeTeste(4)

    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await agendarEncontroPos(page, data)

    await page.getByLabel('Data', { exact: true }).fill(data)
    await page.getByRole('button', { name: 'Agendar' }).click()

    await expect(page.getByText('Não foi possível agendar. Verifique se já existe um encontro pós')).toBeVisible()
    const opcoes = await campoEncontroPos(page).locator('option').allInnerTexts()
    expect(opcoes.filter((opcao) => opcao.includes(dataFormatada(data)))).toHaveLength(1)
  })

  /** Só o pool de acompanhados entra na chamada — quem ainda não fez o Encontro não aparece. */
  test('lista apenas os jovens acompanhados da paróquia', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await agendarEncontroPos(page, dataDeTeste(5))

    await expect(page.getByRole('row')).toHaveCount(MASSA.jovens.length + 1)
    await expect(page.getByRole('cell', { name: ANA.nome })).toBeVisible()
    await expect(page.getByRole('cell', { name: BRUNO.nome })).toBeVisible()
  })
})
