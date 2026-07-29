import { useState } from 'react'
import { usePessoaSearch } from '../../hooks/usePessoaSearch'
import type { PessoaSearchSelectProps } from './PessoaSearchSelect.types'

/** Campo de busca de Pessoa por nome com debounce, usado no cadastro de palestrante Seguidor. */
export function PessoaSearchSelect({
  label,
  selectedId,
  selectedLabel,
  onSelect,
  onClear,
  error,
  placeholder,
}: PessoaSearchSelectProps) {
  const [termo, setTermo] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const { data: resultados, isFetching } = usePessoaSearch(termo)

  if (selectedId) {
    return (
      <div className="flex flex-col gap-1.5">
        {label && <span className="text-sm font-medium text-slate-700">{label}</span>}
        <div className="flex h-10 items-center justify-between rounded-lg border border-slate-200 bg-slate-50 px-3 text-sm text-slate-900">
          <span>{selectedLabel}</span>
          <button
            type="button"
            className="text-xs font-semibold text-violet-600 hover:underline"
            onClick={() => onClear?.()}
          >
            Trocar
          </button>
        </div>
      </div>
    )
  }

  return (
    <div className="relative flex flex-col gap-1.5">
      {label && <span className="text-sm font-medium text-slate-700">{label}</span>}
      <input
        type="text"
        value={termo}
        placeholder={placeholder ?? 'Buscar pessoa por nome...'}
        onChange={(event) => {
          setTermo(event.target.value)
          setIsOpen(true)
        }}
        onFocus={() => setIsOpen(true)}
        onBlur={() => setTimeout(() => setIsOpen(false), 150)}
        className={`h-10 rounded-lg border px-3 text-sm text-slate-900 outline-none transition-colors focus:border-violet-500 ${
          error ? 'border-rose-400' : 'border-slate-200'
        }`}
      />
      {isOpen && termo.trim().length >= 2 && (
        <div className="absolute top-full z-10 mt-1 max-h-56 w-full overflow-auto rounded-lg border border-slate-200 bg-white shadow-lg">
          {isFetching && <div className="px-3 py-2 text-sm text-slate-400">Buscando...</div>}
          {!isFetching && resultados?.length === 0 && (
            <div className="px-3 py-2 text-sm text-slate-400">Nenhuma pessoa encontrada.</div>
          )}
          {resultados?.map((pessoa) => (
            <button
              key={pessoa.id}
              type="button"
              onMouseDown={(event) => event.preventDefault()}
              onClick={() => {
                onSelect(pessoa.id, pessoa.nomeCompleto)
                setTermo('')
                setIsOpen(false)
              }}
              className="block w-full px-3 py-2 text-left text-sm text-slate-900 hover:bg-slate-50"
            >
              {pessoa.nomeCompleto}
            </button>
          ))}
        </div>
      )}
      {error && <span className="text-xs text-rose-600">{error}</span>}
    </div>
  )
}
