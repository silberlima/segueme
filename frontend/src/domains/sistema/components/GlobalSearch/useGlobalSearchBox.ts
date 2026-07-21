import { useEffect, useRef, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useGlobalSearch } from '../../hooks/useGlobalSearch'
import type { BuscaResultado } from '../../domain/busca.types'

export function useGlobalSearchBox() {
  const [query, setQuery] = useState('')
  const [isOpen, setIsOpen] = useState(false)
  const inputRef = useRef<HTMLInputElement>(null)
  const navigate = useNavigate()
  const { data: resultados = [], isFetching } = useGlobalSearch(query)

  useEffect(() => {
    function handleKeyDown(event: KeyboardEvent) {
      if ((event.metaKey || event.ctrlKey) && event.key.toLowerCase() === 'k') {
        event.preventDefault()
        inputRef.current?.focus()
        setIsOpen(true)
      }
      if (event.key === 'Escape') {
        setIsOpen(false)
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [])

  const selecionar = (resultado: BuscaResultado) => {
    setIsOpen(false)
    setQuery('')
    const rota = resultado.tipo === 'PESSOA' ? `/pessoas/${resultado.id}` : `/encontros/${resultado.id}`
    navigate(rota)
  }

  return { query, setQuery, isOpen, setIsOpen, inputRef, resultados, isFetching, selecionar }
}
