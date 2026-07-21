import type { BuscaTipo } from '../../domain/busca.types'
import { useGlobalSearchBox } from './useGlobalSearchBox'
import type { GlobalSearchProps } from './GlobalSearch.types'

const TITULO_GRUPO: Record<BuscaTipo, string> = {
  PESSOA: 'Pessoas',
  ENCONTRO: 'Encontros',
}

export function GlobalSearch({ className = '' }: GlobalSearchProps) {
  const { query, setQuery, isOpen, setIsOpen, inputRef, resultados, isFetching, selecionar } =
    useGlobalSearchBox()

  const grupos: BuscaTipo[] = ['PESSOA', 'ENCONTRO']
  const mostrarDropdown = isOpen && query.trim().length >= 2

  return (
    <div className={`relative ${className}`}>
      <input
        ref={inputRef}
        value={query}
        onChange={(event) => setQuery(event.target.value)}
        onFocus={() => setIsOpen(true)}
        onBlur={() => setTimeout(() => setIsOpen(false), 150)}
        placeholder="Buscar pessoas e encontros... (⌘K)"
        className="h-10 w-72 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none transition-colors focus:border-violet-500"
      />
      {mostrarDropdown && (
        <div className="absolute z-40 mt-1 w-full rounded-lg border border-slate-200 bg-white p-2 shadow-lg">
          {isFetching && <p className="px-2 py-1.5 text-xs text-slate-400">Buscando...</p>}
          {!isFetching && resultados.length === 0 && (
            <p className="px-2 py-1.5 text-xs text-slate-400">Nenhum resultado encontrado.</p>
          )}
          {grupos.map((tipo) => {
            const itens = resultados.filter((resultado) => resultado.tipo === tipo)
            if (itens.length === 0) return null
            return (
              <div key={tipo} className="mb-1 last:mb-0">
                <p className="px-2 py-1 text-xs font-semibold uppercase text-slate-400">{TITULO_GRUPO[tipo]}</p>
                {itens.map((resultado) => (
                  <button
                    key={resultado.id}
                    type="button"
                    onMouseDown={() => selecionar(resultado)}
                    className="block w-full rounded-md px-2 py-1.5 text-left text-sm text-slate-900 hover:bg-slate-50"
                  >
                    <span className="font-medium">{resultado.titulo}</span>
                    <span className="ml-2 text-xs text-slate-500">{resultado.subtitulo}</span>
                  </button>
                ))}
              </div>
            )
          })}
        </div>
      )}
    </div>
  )
}
