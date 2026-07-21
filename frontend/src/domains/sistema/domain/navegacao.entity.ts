import { CATALOGO_NAVEGACAO } from './navegacao.catalogo'
import type { ItemNavegacao } from './navegacao.types'
import type { Perfil } from './perfil.types'

function visivelParaPerfil(item: ItemNavegacao, perfil: Perfil): boolean {
  return item.perfis === 'todos' || item.perfis.includes(perfil)
}

/** Itens navegáveis (RN-ACESSO-02: menu lateral determinado pelo perfil ativo). */
export function getMenuParaPerfil(perfil: Perfil | null): ItemNavegacao[] {
  if (!perfil) return []
  return CATALOGO_NAVEGACAO.filter((item) => item.status === 'pronto' && visivelParaPerfil(item, perfil))
}

/** Catálogo completo agrupado por módulo, para o Mapa de Navegação (RN-ACESSO-03). */
export function getCatalogoAgrupado(): Map<string, ItemNavegacao[]> {
  const grupos = new Map<string, ItemNavegacao[]>()
  for (const item of CATALOGO_NAVEGACAO) {
    const lista = grupos.get(item.modulo) ?? []
    lista.push(item)
    grupos.set(item.modulo, lista)
  }
  return grupos
}
