import { Link, useParams } from 'react-router-dom'
import { Badge, ListState } from '../../../shared/ui'
import { calcularIdade } from '../domain/pessoa.entity'
import { ESTADO_CIVIL_LABEL, SITUACAO_LABEL } from '../domain/pessoa.types'
import type { SituacaoPessoa } from '../domain/pessoa.types'
import { PersonAvatar } from '../components/PersonAvatar'
import { usePessoa } from '../hooks/usePessoa'

const SITUACAO_TONE: Record<SituacaoPessoa, 'neutral' | 'info' | 'success'> = {
  PRE_INSCRITO: 'neutral',
  INSCRITO: 'info',
  SEGUIMISTA: 'info',
  SEGUIDOR: 'success',
}

function Campo({ label, valor }: { label: string; valor: string | null }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="text-sm text-slate-900">{valor && valor.trim() ? valor : '—'}</p>
    </div>
  )
}

export function PersonProfileScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: pessoa, isLoading, isError, refetch } = usePessoa(id)

  return (
    <div className="p-6">
      <Link to="/pessoas" className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Lista de Pessoas
      </Link>

      <ListState
        isLoading={isLoading}
        isError={isError}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="120px"
        errorMessage="Não foi possível carregar o perfil desta pessoa."
      />

      {pessoa && (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
          <div className="mb-6 flex items-center gap-4">
            <PersonAvatar nomeCompleto={pessoa.nomeCompleto} size="md" />
            <div>
              <h1 className="text-xl font-semibold text-slate-900">{pessoa.nomeCompleto}</h1>
              <p className="text-sm text-slate-500">
                {calcularIdade(pessoa.dataNascimento)} anos · {ESTADO_CIVIL_LABEL[pessoa.estadoCivil]} ·{' '}
                {pessoa.paroquiaNome}
              </p>
            </div>
            <div className="ml-auto">
              <Badge tone={SITUACAO_TONE[pessoa.situacao]}>{SITUACAO_LABEL[pessoa.situacao]}</Badge>
            </div>
          </div>

          <div className="mb-6 grid grid-cols-1 gap-4 sm:grid-cols-3">
            <Campo label="Telefone" valor={pessoa.telefone} />
            <Campo label="Alergias" valor={pessoa.alergias} />
            <Campo label="Restrição alimentar" valor={pessoa.restricaoAlimentar} />
            <Campo label="Medicação de uso contínuo" valor={pessoa.medicacaoUsoContinuo} />
            <Campo label="Contato de emergência" valor={pessoa.contatoEmergenciaNome} />
            <Campo
              label="Telefone de emergência"
              valor={pessoa.contatoEmergenciaTelefone}
            />
            <Campo label="Parentesco" valor={pessoa.contatoEmergenciaParentesco} />
            <Campo label="Indicado por" valor={pessoa.indicadoPorNome} />
          </div>

          <div className="mb-6">
            <p className="mb-2 text-xs font-medium text-slate-500">Competências</p>
            <div className="flex flex-wrap gap-1">
              {pessoa.competencias.length === 0 && <span className="text-sm text-slate-400">—</span>}
              {pessoa.competencias.map((competencia) => (
                <Badge key={competencia} tone="neutral">
                  {competencia}
                </Badge>
              ))}
            </div>
          </div>

          <Link
            to={`/pessoas/${pessoa.id}/historico`}
            className="text-sm font-medium text-violet-600 hover:underline"
          >
            Ver histórico →
          </Link>
        </div>
      )}
    </div>
  )
}
