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

/** Tela Prioridades (`screen_prioridades`, RF-059, RN-POS-02/03). */
test.describe('Pós-Encontro — Prioridades', () => {
  test.beforeEach(() => limparEncontrosPosDaMassa())

  test('ranqueia por frequência e classifica nas faixas da régua', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, dataDeTeste(21), [ANA.nome], [BRUNO.nome])
    await registrarChamada(page, dataDeTeste(14), [ANA.nome], [BRUNO.nome])

    await irParaTela(page, 'Prioridades')
    await selecionarParoquiaE2E(page)

    // Ana com 2/2 = 100% (Alta, régua default ≥75%) vem antes de Bruno com 0/2 (Baixa).
    const linhas = page.getByRole('row')
    await expect(linhas.nth(1)).toContainText(ANA.nome)
    await expect(linhas.nth(1)).toContainText('2 de 2')
    await expect(linhas.nth(1)).toContainText('100%')
    await expect(linhas.nth(1)).toContainText('Alta')
    await expect(linhas.nth(2)).toContainText(BRUNO.nome)
    await expect(linhas.nth(2)).toContainText('0 de 2')
    await expect(linhas.nth(2)).toContainText('Baixa')
  })

  /** RN-POS-02: a tela informa por que a frequência importa. */
  test('exibe a regra de prioridade para as equipes', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Prioridades')

    await expect(
      page.getByText('Quem participa com frequência tem prioridade para trabalhar nas equipes.'),
    ).toBeVisible()
  })

  /** A faixa Média existe entre os cortes: 1 de 2 = 50% cai entre 40% e 75% na régua default. */
  test('classifica frequência intermediária como Média', async ({ paginaPosEncontro: page }) => {
    await irParaTela(page, 'Presenças')
    await selecionarParoquiaE2E(page)
    await registrarChamada(page, dataDeTeste(22), [ANA.nome], [BRUNO.nome])
    await registrarChamada(page, dataDeTeste(15), [BRUNO.nome], [ANA.nome])

    await irParaTela(page, 'Prioridades')
    await selecionarParoquiaE2E(page)

    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('50%')
    await expect(page.getByRole('row', { name: new RegExp(ANA.nome) })).toContainText('Média')
    await expect(page.getByRole('row', { name: new RegExp(BRUNO.nome) })).toContainText('Média')
  })
})
