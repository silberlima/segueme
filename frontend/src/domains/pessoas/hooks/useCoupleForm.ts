import { useState } from 'react'
import type { NovoCasalPayload } from '../domain/pessoa.types'
import { useCreateCasal } from './useCreateCasal'

/** Estado e submissão do formulário "Vincular casal" (RF-012, RN-PESSOA-03/RN-03). */
export function useCoupleForm(onSuccess: () => void) {
  const createCasal = useCreateCasal()
  const [pessoa1Id, setPessoa1Id] = useState('')
  const [pessoa2Id, setPessoa2Id] = useState('')
  const [dataVinculo, setDataVinculo] = useState('')
  const [erro, setErro] = useState<string | undefined>(undefined)

  function reset() {
    setPessoa1Id('')
    setPessoa2Id('')
    setDataVinculo('')
    setErro(undefined)
  }

  async function salvar() {
    if (!pessoa1Id || !pessoa2Id || !dataVinculo) {
      setErro('Selecione as duas pessoas e a data do vínculo.')
      return
    }
    if (pessoa1Id === pessoa2Id) {
      setErro('Selecione duas pessoas diferentes.')
      return
    }
    const payload: NovoCasalPayload = { pessoa1Id, pessoa2Id, dataVinculo }
    try {
      await createCasal.mutateAsync(payload)
      reset()
      onSuccess()
    } catch {
      setErro('Não foi possível vincular o casal. Verifique se alguma das pessoas já possui vínculo.')
    }
  }

  return {
    pessoa1Id,
    pessoa2Id,
    dataVinculo,
    erro,
    setPessoa1Id,
    setPessoa2Id,
    setDataVinculo,
    salvar,
    reset,
    isSaving: createCasal.isPending,
  }
}
