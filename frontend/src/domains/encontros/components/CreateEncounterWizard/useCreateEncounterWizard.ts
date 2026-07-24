import { useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { useAuth } from '../../../../app/providers/AuthProvider'
import { useCreateEncontro } from '../../hooks/useCreateEncontro'
import type { NovoEncontroPayload, PastaDirigente, TipoMembroDirigente } from '../../domain/encontro.types'
import { ESTADO_INICIAL_WIZARD, WIZARD_STEPS } from './wizard.types'
import type { WizardMembroDirigente, WizardState, WizardStepId } from './wizard.types'

const MAX_JOVENS_POR_PASTA = 2
const MAX_CASAIS_POR_PASTA = 1

/** Estado, navegação e validação por passo do wizard "Criar Encontro" (RF-015 a RF-020, RNF-04). */
export function useCreateEncounterWizard() {
  const navigate = useNavigate()
  const { user } = useAuth()
  const paroquiaId = user?.paroquiaId
  const createEncontro = useCreateEncontro()

  const [activeStepId, setActiveStepId] = useState<WizardStepId>('identidade')
  const [furthestStepId, setFurthestStepId] = useState<WizardStepId>('identidade')
  const [form, setForm] = useState<WizardState>(ESTADO_INICIAL_WIZARD)
  const [errors, setErrors] = useState<Record<string, string>>({})

  function setField<K extends keyof WizardState>(field: K, value: WizardState[K]) {
    setForm((current) => ({ ...current, [field]: value }))
  }

  function indiceDoPasso(stepId: WizardStepId): number {
    return WIZARD_STEPS.findIndex((step) => step.id === stepId)
  }

  function validarPasso(stepId: WizardStepId): boolean {
    const proximosErros: Record<string, string> = {}

    if (stepId === 'identidade') {
      if (!form.padroeiro.trim()) proximosErros.padroeiro = 'Informe o padroeiro do encontro.'
      if (!form.diretorEspiritualId) proximosErros.diretorEspiritualId = 'Selecione o diretor espiritual.'
    }

    if (stepId === 'local-datas') {
      if (!form.local.trim()) proximosErros.local = 'Informe o local.'
      if (!form.dataInicio) proximosErros.dataInicio = 'Informe a data de início.'
      if (!form.dataTermino) proximosErros.dataTermino = 'Informe a data de término.'
      if (form.dataInicio && form.dataTermino && form.dataTermino < form.dataInicio) {
        proximosErros.dataTermino = 'A data de término não pode ser anterior à data de início.'
      }
      const vagas = Number(form.vagas)
      if (!form.vagas || !Number.isInteger(vagas) || vagas <= 0) {
        proximosErros.vagas = 'Informe um número de vagas maior que zero.'
      }
    }

    if (stepId === 'equipe-comando') {
      if (!form.coordenadorGeralId) proximosErros.coordenadorGeralId = 'Selecione o coordenador geral.'
      if (!form.casalCoordenadorId) proximosErros.casalCoordenadorId = 'Selecione o casal coordenador.'
    }

    setErrors(proximosErros)
    return Object.keys(proximosErros).length === 0
  }

  function goNext() {
    if (!validarPasso(activeStepId)) return
    const proximoIndice = indiceDoPasso(activeStepId) + 1
    if (proximoIndice >= WIZARD_STEPS.length) return
    const proximoStepId = WIZARD_STEPS[proximoIndice].id
    setActiveStepId(proximoStepId)
    if (indiceDoPasso(proximoStepId) > indiceDoPasso(furthestStepId)) {
      setFurthestStepId(proximoStepId)
    }
  }

  function goBack() {
    const indiceAnterior = indiceDoPasso(activeStepId) - 1
    if (indiceAnterior < 0) return
    setActiveStepId(WIZARD_STEPS[indiceAnterior].id)
  }

  function goToStep(stepId: WizardStepId) {
    if (indiceDoPasso(stepId) <= indiceDoPasso(furthestStepId)) {
      setActiveStepId(stepId)
    }
  }

  /** Composição máxima de 2 jovens + 1 casal por pasta (RN-20) — pasta incompleta é permitida (RN-21). */
  function podeAdicionarMembro(pasta: PastaDirigente, tipoMembro: TipoMembroDirigente): string | null {
    const membrosDaPasta = form.equipeDirigente.filter((membro) => membro.pasta === pasta);
    if (tipoMembro === 'JOVEM') {
      const totalJovens = membrosDaPasta.filter((membro) => membro.tipoMembro === 'JOVEM').length
      if (totalJovens >= MAX_JOVENS_POR_PASTA) return 'Esta pasta já tem 2 jovens (RN-20).'
    } else {
      const totalCasais = membrosDaPasta.filter((membro) => membro.tipoMembro === 'CASAL').length
      if (totalCasais >= MAX_CASAIS_POR_PASTA) return 'Esta pasta já tem 1 casal (RN-20).'
    }
    return null
  }

  function addMembroDirigente(membro: WizardMembroDirigente): string | null {
    const erro = podeAdicionarMembro(membro.pasta, membro.tipoMembro)
    if (erro) return erro
    setForm((current) => ({ ...current, equipeDirigente: [...current.equipeDirigente, membro] }))
    return null
  }

  function removeMembroDirigente(index: number) {
    setForm((current) => ({
      ...current,
      equipeDirigente: current.equipeDirigente.filter((_, itemIndex) => itemIndex !== index),
    }))
  }

  async function submit() {
    if (!paroquiaId) return
    if (!validarPasso('identidade') || !validarPasso('local-datas') || !validarPasso('equipe-comando')) {
      return
    }

    const payload: NovoEncontroPayload = {
      padroeiro: form.padroeiro.trim(),
      diretorEspiritualId: form.diretorEspiritualId,
      local: form.local.trim(),
      dataInicio: form.dataInicio,
      dataTermino: form.dataTermino,
      vagas: Number(form.vagas),
      paroquiaId,
      equipeDirigente: form.equipeDirigente.map((membro) => ({
        pasta: membro.pasta,
        tipoMembro: membro.tipoMembro,
        pessoaId: membro.pessoaId,
        casalId: membro.casalId,
      })),
      equipeComando: {
        coordenadorGeralId: form.coordenadorGeralId,
        casalCoordenadorId: form.casalCoordenadorId,
      },
    }

    const encontro = await createEncontro.mutateAsync(payload)
    navigate(`/encontros/${encontro.id}`)
  }

  function cancelar() {
    navigate('/encontros')
  }

  return {
    paroquiaId,
    steps: WIZARD_STEPS,
    activeStepId,
    furthestStepId,
    form,
    errors,
    setField,
    goNext,
    goBack,
    goToStep,
    addMembroDirigente,
    removeMembroDirigente,
    submit,
    cancelar,
    isSubmitting: createEncontro.isPending,
    submitError: createEncontro.isError,
  }
}
