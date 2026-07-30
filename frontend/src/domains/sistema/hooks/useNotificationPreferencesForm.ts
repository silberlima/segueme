import { useState } from 'react'
import { aplicarAjustes, temAjusteEfetivo } from '../domain/notificacao.entity'
import type { AjustesDePreferencia, TipoNotificacao } from '../domain/notificacao.types'
import { useNotificationPreferences } from './useNotificationPreferences'

/**
 * Estado dos toggles da seção Notificações de Configurações (RF-057).
 *
 * <p>A escolha é gravada em uma ação só, ao salvar: o usuário mexe nos três interruptores e
 * confirma uma vez, em vez de disparar uma requisição por clique. O estado local guarda apenas o
 * que foi mexido e é derivado sobre o que veio do servidor — não uma cópia da lista, que ficaria
 * desatualizada em relação à consulta.
 */
export function useNotificationPreferencesForm() {
  const { data, isLoading, isError, refetch, salvar, isSalvando, salvou } = useNotificationPreferences()
  const [ajustes, setAjustes] = useState<AjustesDePreferencia>({})

  const persistidas = data ?? []
  const preferencias = aplicarAjustes(persistidas, ajustes)
  const alterado = temAjusteEfetivo(persistidas, ajustes)

  function alternar(tipo: TipoNotificacao, ativo: boolean) {
    setAjustes((atual) => ({ ...atual, [tipo]: ativo }))
  }

  return {
    preferencias,
    isLoading,
    isError,
    recarregar: refetch,
    alternar,
    salvar: () => salvar(preferencias, { onSuccess: () => setAjustes({}) }),
    isSalvando,
    /** Só confirma "salvo" enquanto nada foi mexido depois da gravação. */
    mostrarConfirmacao: salvou && !alterado,
    podeSalvar: alterado && !isSalvando,
  }
}
