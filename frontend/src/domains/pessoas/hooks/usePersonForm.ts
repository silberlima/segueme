import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { EstadoCivil, NovaPessoaPayload } from '../domain/pessoa.types'
import { useCreatePessoa } from './useCreatePessoa'

export interface PersonFormState {
  nomeCompleto: string
  dataNascimento: string
  telefone: string
  estadoCivil: EstadoCivil | ''
  paroquiaId: string
  alergias: string
  restricaoAlimentar: string
  medicacaoUsoContinuo: string
  contatoEmergenciaNome: string
  contatoEmergenciaTelefone: string
  contatoEmergenciaParentesco: string
  competenciaIds: string[]
}

const ESTADO_INICIAL: PersonFormState = {
  nomeCompleto: '',
  dataNascimento: '',
  telefone: '',
  estadoCivil: '',
  paroquiaId: '',
  alergias: '',
  restricaoAlimentar: '',
  medicacaoUsoContinuo: '',
  contatoEmergenciaNome: '',
  contatoEmergenciaTelefone: '',
  contatoEmergenciaParentesco: '',
  competenciaIds: [],
}

type CampoObrigatorio = 'nomeCompleto' | 'dataNascimento' | 'telefone' | 'estadoCivil' | 'paroquiaId'

/** Estado, validação e submissão do formulário de Cadastro de Pessoa (RF-009). */
export function usePersonForm() {
  const navigate = useNavigate()
  const createPessoa = useCreatePessoa()
  const [form, setForm] = useState<PersonFormState>(ESTADO_INICIAL)
  const [errors, setErrors] = useState<Partial<Record<CampoObrigatorio, string>>>({})

  function setField<K extends keyof PersonFormState>(field: K, value: PersonFormState[K]) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function toggleCompetencia(competenciaId: string) {
    setForm((current) => ({
      ...current,
      competenciaIds: current.competenciaIds.includes(competenciaId)
        ? current.competenciaIds.filter((id) => id !== competenciaId)
        : [...current.competenciaIds, competenciaId],
    }))
  }

  function validar(): boolean {
    const proximosErros: Partial<Record<CampoObrigatorio, string>> = {}
    if (!form.nomeCompleto.trim()) proximosErros.nomeCompleto = 'Informe o nome completo.'
    if (!form.dataNascimento) proximosErros.dataNascimento = 'Informe a data de nascimento.'
    if (!form.telefone.trim()) proximosErros.telefone = 'Informe o telefone.'
    if (!form.estadoCivil) proximosErros.estadoCivil = 'Selecione o estado civil.'
    if (!form.paroquiaId) proximosErros.paroquiaId = 'Selecione a paróquia.'
    setErrors(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  async function salvar() {
    if (!validar()) return

    const payload: NovaPessoaPayload = {
      nomeCompleto: form.nomeCompleto.trim(),
      dataNascimento: form.dataNascimento,
      telefone: form.telefone.trim(),
      estadoCivil: form.estadoCivil as EstadoCivil,
      paroquiaId: form.paroquiaId,
      alergias: form.alergias.trim() || undefined,
      restricaoAlimentar: form.restricaoAlimentar.trim() || undefined,
      medicacaoUsoContinuo: form.medicacaoUsoContinuo.trim() || undefined,
      contatoEmergenciaNome: form.contatoEmergenciaNome.trim() || undefined,
      contatoEmergenciaTelefone: form.contatoEmergenciaTelefone.trim() || undefined,
      contatoEmergenciaParentesco: form.contatoEmergenciaParentesco.trim() || undefined,
      competenciaIds: form.competenciaIds.length > 0 ? form.competenciaIds : undefined,
    }

    const pessoa = await createPessoa.mutateAsync(payload)
    navigate(`/pessoas/${pessoa.id}`)
  }

  function cancelar() {
    navigate('/pessoas')
  }

  return {
    form,
    errors,
    setField,
    toggleCompetencia,
    salvar,
    cancelar,
    isSaving: createPessoa.isPending,
    saveError: createPessoa.isError,
  }
}
