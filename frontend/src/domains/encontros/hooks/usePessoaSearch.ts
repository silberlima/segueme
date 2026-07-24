import { useEffect, useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { searchPessoas } from '../services/encontro.service'

const DEBOUNCE_MS = 300

/** Busca de Pessoa com debounce, usada pelos seletores do wizard "Criar Encontro". */
export function usePessoaSearch(termo: string, paroquiaId?: string) {
  const [termoComDebounce, setTermoComDebounce] = useState(termo)

  useEffect(() => {
    const timeout = setTimeout(() => setTermoComDebounce(termo), DEBOUNCE_MS)
    return () => clearTimeout(timeout)
  }, [termo])

  return useQuery({
    queryKey: ['pessoa-search', termoComDebounce, paroquiaId],
    queryFn: () => searchPessoas(termoComDebounce, paroquiaId),
    enabled: termoComDebounce.trim().length >= 2,
  })
}
