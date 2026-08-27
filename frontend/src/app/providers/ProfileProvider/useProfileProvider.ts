import { useCallback, useMemo, useState } from 'react'
import { useMe } from '../../../domains/sistema/hooks/useMe'
import type { Perfil } from '../../../domains/sistema/domain/perfil.types'
import type { ProfileContextValue } from './ProfileProvider.types'

const STORAGE_KEY = 'segueme.perfilAtivo'

export function useProfileProvider(): ProfileContextValue {
  const { data: me, isLoading } = useMe()
  const [override, setOverride] = useState<Perfil | null>(null)

  const perfis = useMemo(() => me?.perfis ?? [], [me])

  const perfilAtivo = useMemo(() => {
    if (perfis.length === 0) return null
    if (override && perfis.includes(override)) return override
    const armazenado = window.localStorage.getItem(STORAGE_KEY) as Perfil | null
    if (armazenado && perfis.includes(armazenado)) return armazenado
    // Todo usuário é também Seguidor, então ele quase nunca é o perfil com que se entra: quem tem
    // um perfil de trabalho cai nele, e o Seguidor só vira padrão para quem não tem outro.
    return perfis.find((perfil) => perfil !== 'SEGUIDOR') ?? perfis[0]
  }, [perfis, override])

  const setPerfilAtivo = useCallback((perfil: Perfil) => {
    setOverride(perfil)
    window.localStorage.setItem(STORAGE_KEY, perfil)
  }, [])

  return { isLoading, perfis, perfilAtivo, setPerfilAtivo }
}
