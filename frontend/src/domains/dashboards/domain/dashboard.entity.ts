import { REGIAO_LABEL } from './dashboard.types'
import type { ContagemMensal, ContagemRegiao, EquipeResumoDoPainel } from './dashboard.types'

/** Funções puras de apresentação dos painéis — nenhuma delas depende de React ou de API. */

const moeda = new Intl.NumberFormat('pt-BR', { style: 'currency', currency: 'BRL' })
const mesCurto = new Intl.DateTimeFormat('pt-BR', { month: 'short' })

export function formatarMoeda(valor: number): string {
  return moeda.format(valor)
}

export function formatarData(iso: string): string {
  return new Date(`${iso}T00:00:00`).toLocaleDateString('pt-BR')
}

export function formatarDataHora(iso: string): string {
  return new Date(iso).toLocaleDateString('pt-BR')
}

/** `2026-07` → `jul/26`, o rótulo curto que cabe na barra do gráfico mensal. */
export function formatarMes(mes: string): string {
  const [ano, mesDoAno] = mes.split('-')
  const data = new Date(Number(ano), Number(mesDoAno) - 1, 1)
  return `${mesCurto.format(data).replace('.', '')}/${ano.slice(2)}`
}

export function rotuloRegiao(regiao: ContagemRegiao['regiao']): string {
  return regiao === null ? 'Não informada' : REGIAO_LABEL[regiao]
}

export function rotuloMesDoGrafico(contagem: ContagemMensal): string {
  return formatarMes(contagem.mes)
}

/** Progresso de "Equipes montadas" do Painel da Paróquia: quantas equipes atingiram o mínimo (RN-06). */
export function equipesCompletas(equipes: EquipeResumoDoPainel[]): number {
  return equipes.filter((equipe) => equipe.completa).length
}

/** Percentual de ocupação de uma equipe, limitado a 100 para a barra nunca transbordar. */
export function percentualOcupacao(equipe: EquipeResumoDoPainel): number {
  if (equipe.maximo === 0) {
    return 0
  }
  return Math.min(100, Math.round((equipe.preenchidos / equipe.maximo) * 100))
}

/** Percentual de progresso de uma meta; 0 quando a meta ainda não foi definida. */
export function percentualDaMeta(valorArrecadado: number, meta: number): number {
  if (meta <= 0) {
    return 0
  }
  return Math.min(100, Math.round((valorArrecadado / meta) * 100))
}
