import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { searchPessoas } from '../services/setorial.service'

const DEBOUNCE_MS = 300

/** Busca de Pessoa com debounce, usada pelo seletor de "quem transferir". */
export function usePessoaSearch(termo: string) {
  const [termoComDebounce, setTermoComDebounce] = useState(termo)

  useEffect(() => {
    const timeout = setTimeout(() => setTermoComDebounce(termo), DEBOUNCE_MS)
    return () => clearTimeout(timeout)
  }, [termo])

  return useQuery({
    queryKey: ['pessoa-search-setorial', termoComDebounce],
    queryFn: () => searchPessoas(termoComDebounce),
    enabled: termoComDebounce.trim().length >= 2,
  })
}
