export type SituacaoPessoa = 'PRE_INSCRITO' | 'INSCRITO' | 'SEGUIMISTA' | 'SEGUIDOR'

export const SITUACAO_LABEL: Record<SituacaoPessoa, string> = {
  PRE_INSCRITO: 'Pré-inscrito',
  INSCRITO: 'Inscrito',
  SEGUIMISTA: 'Seguimista',
  SEGUIDOR: 'Seguidor',
}

export type EstadoCivil = 'SOLTEIRO' | 'CASADO' | 'DIVORCIADO' | 'VIUVO' | 'OUTRO'

export const ESTADO_CIVIL_LABEL: Record<EstadoCivil, string> = {
  SOLTEIRO: 'Solteiro(a)',
  CASADO: 'Casado(a)',
  DIVORCIADO: 'Divorciado(a)',
  VIUVO: 'Viúvo(a)',
  OUTRO: 'Outro',
}

export const TIPO_EVENTO_LABEL: Record<string, string> = {
  INSCRICAO_CADASTRO_NACIONAL: 'Inscrição no cadastro nacional',
  PRIMEIRA_PARTICIPACAO_SEGUIDOR: 'Primeira participação como seguidor',
  PRIMEIRA_EQUIPE_EQUIPISTA: 'Primeira equipe como equipista',
  TORNOU_SE_SEGUIMISTA: 'Tornou-se seguimista',
  PARTICIPACAO_EQUIPE: 'Participação em equipe',
  VINCULO_CASAL_REGISTRADO: 'Vínculo de casal registrado',
  EMPRESTIMO_OUTRA_PAROQUIA: 'Empréstimo para outra paróquia',
  PAPEL_COORDENADOR: 'Papel de coordenador',
}

export interface Pessoa {
  id: string
  nomeCompleto: string
  dataNascimento: string
  telefone: string
  estadoCivil: EstadoCivil
  situacao: SituacaoPessoa
  paroquiaId: string
  paroquiaNome: string
  alergias: string | null
  restricaoAlimentar: string | null
  medicacaoUsoContinuo: string | null
  contatoEmergenciaNome: string | null
  contatoEmergenciaTelefone: string | null
  contatoEmergenciaParentesco: string | null
  competencias: string[]
  indicadoPorId: string | null
  indicadoPorNome: string | null
  createdAt: string
}

/** O que a própria pessoa altera sobre si (perfil Seguidor). O resto do cadastro é da equipe. */
export interface MeusDadosPayload {
  telefone: string
  alergias: string | null
  restricaoAlimentar: string | null
  medicacaoUsoContinuo: string | null
  contatoEmergenciaNome: string | null
  contatoEmergenciaTelefone: string | null
  contatoEmergenciaParentesco: string | null
}

export interface PessoaResumo {
  id: string
  nomeCompleto: string
  dataNascimento: string
  estadoCivil: EstadoCivil
  situacao: SituacaoPessoa
  paroquiaId: string
  paroquiaNome: string
  competencias: string[]
  numEncontros: number
}

export interface Casal {
  id: string
  pessoa1: PessoaResumo
  pessoa2: PessoaResumo
  dataVinculo: string
}

export interface Competencia {
  id: string
  nome: string
  numPessoas: number
}

export interface HistoricoEvento {
  id: string
  tipo: string
  dataEvento: string
  descricao: string | null
}

export interface NovaPessoaPayload {
  nomeCompleto: string
  dataNascimento: string
  telefone: string
  estadoCivil: EstadoCivil
  paroquiaId: string
  alergias?: string
  restricaoAlimentar?: string
  medicacaoUsoContinuo?: string
  contatoEmergenciaNome?: string
  contatoEmergenciaTelefone?: string
  contatoEmergenciaParentesco?: string
  indicadoPorId?: string
  competenciaIds?: string[]
}

export interface NovoCasalPayload {
  pessoa1Id: string
  pessoa2Id: string
  dataVinculo: string
}
