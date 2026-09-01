import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import type { EstadoCivil, NovaPessoaPayload } from '../domain/pessoa.types'
import { useCreatePessoa } from './useCreatePessoa'
import { cpfValido } from './useMeusDadosForm'

const CEP_VALIDO = /^\d{5}-?\d{3}$/
const UF_VALIDA = /^[A-Za-z]{2}$/

export interface PersonFormState {
  nomeCompleto: string
  cpf: string
  dataNascimento: string
  telefone: string
  instagram: string
  estadoCivil: EstadoCivil | ''
  paroquiaId: string
  cep: string
  logradouro: string
  numero: string
  complemento: string
  bairro: string
  cidade: string
  uf: string
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
  cpf: '',
  dataNascimento: '',
  telefone: '',
  instagram: '',
  estadoCivil: '',
  paroquiaId: '',
  cep: '',
  logradouro: '',
  numero: '',
  complemento: '',
  bairro: '',
  cidade: '',
  uf: '',
  alergias: '',
  restricaoAlimentar: '',
  medicacaoUsoContinuo: '',
  contatoEmergenciaNome: '',
  contatoEmergenciaTelefone: '',
  contatoEmergenciaParentesco: '',
  competenciaIds: [],
}

type CampoObrigatorio =
  | 'nomeCompleto'
  | 'cpf'
  | 'dataNascimento'
  | 'telefone'
  | 'estadoCivil'
  | 'paroquiaId'
  | 'cep'
  | 'logradouro'
  | 'numero'
  | 'bairro'
  | 'cidade'
  | 'uf'

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
    if (!form.cpf.trim()) proximosErros.cpf = 'Informe o CPF.'
    else if (!cpfValido(form.cpf)) proximosErros.cpf = 'CPF inválido.'
    if (!form.dataNascimento) proximosErros.dataNascimento = 'Informe a data de nascimento.'
    if (!form.telefone.trim()) proximosErros.telefone = 'Informe o telefone.'
    if (!form.estadoCivil) proximosErros.estadoCivil = 'Selecione o estado civil.'
    if (!form.paroquiaId) proximosErros.paroquiaId = 'Selecione a paróquia.'
    if (!CEP_VALIDO.test(form.cep.trim())) proximosErros.cep = 'Informe um CEP válido.'
    if (!form.logradouro.trim()) proximosErros.logradouro = 'Informe o logradouro.'
    if (!form.numero.trim()) proximosErros.numero = 'Informe o número.'
    if (!form.bairro.trim()) proximosErros.bairro = 'Informe o bairro.'
    if (!form.cidade.trim()) proximosErros.cidade = 'Informe a cidade.'
    if (!UF_VALIDA.test(form.uf.trim())) proximosErros.uf = 'Informe a UF.'
    setErrors(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  async function salvar() {
    if (!validar()) return

    const payload: NovaPessoaPayload = {
      nomeCompleto: form.nomeCompleto.trim(),
      cpf: form.cpf.trim(),
      dataNascimento: form.dataNascimento,
      telefone: form.telefone.trim(),
      instagram: form.instagram.trim() || undefined,
      estadoCivil: form.estadoCivil as EstadoCivil,
      paroquiaId: form.paroquiaId,
      endereco: {
        cep: form.cep.trim(),
        logradouro: form.logradouro.trim(),
        numero: form.numero.trim(),
        complemento: form.complemento.trim() || null,
        bairro: form.bairro.trim(),
        cidade: form.cidade.trim(),
        uf: form.uf.trim().toUpperCase(),
      },
      alergias: form.alergias.trim() || undefined,
      restricaoAlimentar: form.restricaoAlimentar.trim() || undefined,
      medicacaoUsoContinuo: form.medicacaoUsoContinuo.trim() || undefined,
      contatoEmergenciaNome: form.contatoEmergenciaNome.trim() || undefined,
      contatoEmergenciaTelefone: form.contatoEmergenciaTelefone.trim() || undefined,
      contatoEmergenciaParentesco: form.contatoEmergenciaParentesco.trim() || undefined,
      competenciaIds: form.competenciaIds.length > 0 ? form.competenciaIds : undefined,
    }

    try {
      const pessoa = await createPessoa.mutateAsync(payload)
      navigate(`/pessoas/${pessoa.id}`)
    } catch (falha) {
      // 409 é CPF já cadastrado (RN-PESSOA-01): a pessoa já está no cadastro nacional, e o erro
      // pertence ao campo, não à faixa genérica de "não foi possível salvar". As demais falhas
      // seguem aparecendo por `saveError`, que a mutation já expõe.
      if ((falha as { response?: { status?: number } }).response?.status === 409) {
        setErrors({ cpf: 'Já existe uma pessoa cadastrada com este CPF.' })
      }
    }
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
    // O CPF duplicado já se explica no campo; repetir o aviso genérico só faria ruído.
    saveError: createPessoa.isError && !errors.cpf,
  }
}
