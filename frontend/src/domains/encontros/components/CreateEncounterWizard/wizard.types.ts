import type { PastaDirigente, TipoMembroDirigente } from '../../domain/encontro.types'

export const WIZARD_STEPS = [
  { id: 'identidade', label: 'Identidade' },
  { id: 'local-datas', label: 'Local & datas' },
  { id: 'equipe-dirigente', label: 'Equipe Dirigente' },
  { id: 'equipe-comando', label: 'Equipe Comando' },
  { id: 'revisao', label: 'Revisão' },
] as const

export type WizardStepId = (typeof WIZARD_STEPS)[number]['id']

export interface WizardMembroDirigente {
  pasta: PastaDirigente
  tipoMembro: TipoMembroDirigente
  pessoaId?: string
  pessoaNome?: string
  casalId?: string
  casalNome?: string
}

export interface WizardState {
  padroeiro: string
  diretorEspiritualId: string
  diretorEspiritualNome: string
  local: string
  dataInicio: string
  dataTermino: string
  vagas: string
  equipeDirigente: WizardMembroDirigente[]
  coordenadorGeralId: string
  coordenadorGeralNome: string
  casalCoordenadorId: string
  casalCoordenadorNome: string
}

export const ESTADO_INICIAL_WIZARD: WizardState = {
  padroeiro: '',
  diretorEspiritualId: '',
  diretorEspiritualNome: '',
  local: '',
  dataInicio: '',
  dataTermino: '',
  vagas: '',
  equipeDirigente: [],
  coordenadorGeralId: '',
  coordenadorGeralNome: '',
  casalCoordenadorId: '',
  casalCoordenadorNome: '',
}
