import { CATALOGO_NAVEGACAO } from './navegacao.catalogo'
import type { Perfil } from './perfil.types'

const MODULO_DASHBOARDS = 'Dashboards'

/**
 * Painel mapeado para um perfil (RN-DASH-01: cada perfil só enxerga o seu, e a troca de painel
 * acontece pela troca de perfil). Deriva do próprio catálogo de navegação para não existir uma
 * segunda lista de painéis fora de sincronia com o menu.
 *
 * <p>O Seguidor cai em "Meus Dados": o painel dele é o próprio cadastro, como o protótipo já
 * indicava ("é também o painel inicial do perfil Seguidor" — `cadastro-de-pessoas.md` 2.3). Como
 * ele tem duas entradas no módulo, vale a primeira do catálogo; por isso "Meus Dados" vem antes de
 * "Meu Histórico" lá.
 */
export function getPainelDoPerfil(perfil: Perfil | null): string | null {
  if (!perfil) {
    return null
  }
  const painel = CATALOGO_NAVEGACAO.find(
    (item) =>
      item.modulo === MODULO_DASHBOARDS &&
      item.status === 'pronto' &&
      item.path !== null &&
      item.perfis !== 'todos' &&
      item.perfis.includes(perfil),
  )
  return painel?.path ?? null
}
