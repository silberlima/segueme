import type { ReactNode } from 'react'
import { PASTAS_DIRIGENTE, PASTA_DIRIGENTE_LABEL } from '../../domain/encontro.types'
import type { WizardState, WizardStepId } from './wizard.types'

interface StepRevisaoProps {
  form: WizardState
  onEditStep: (stepId: WizardStepId) => void
}

export function StepRevisao({ form, onEditStep }: StepRevisaoProps) {
  const pastasIncompletas = PASTAS_DIRIGENTE.filter((pasta) => {
    const membros = form.equipeDirigente.filter((membro) => membro.pasta === pasta)
    const jovens = membros.filter((membro) => membro.tipoMembro === 'JOVEM').length
    const casais = membros.filter((membro) => membro.tipoMembro === 'CASAL').length
    return jovens < 2 || casais < 1
  })

  return (
    <div className="flex flex-col gap-6">
      <Bloco titulo="Identidade" onEditar={() => onEditStep('identidade')}>
        <Campo label="Padroeiro" valor={form.padroeiro} />
        <Campo label="Diretor espiritual" valor={form.diretorEspiritualNome} />
      </Bloco>

      <Bloco titulo="Local & datas" onEditar={() => onEditStep('local-datas')}>
        <Campo label="Local" valor={form.local} />
        <Campo label="Data de início" valor={form.dataInicio} />
        <Campo label="Data de término" valor={form.dataTermino} />
        <Campo label="Vagas" valor={form.vagas} />
      </Bloco>

      <Bloco titulo="Equipe Dirigente" onEditar={() => onEditStep('equipe-dirigente')}>
        {pastasIncompletas.length > 0 && (
          <p className="mb-2 text-xs text-amber-700">
            Pastas incompletas: {pastasIncompletas.map((pasta) => PASTA_DIRIGENTE_LABEL[pasta]).join(', ')} — o
            encontro pode ser criado mesmo assim (RN-21).
          </p>
        )}
        {form.equipeDirigente.length === 0 && <p className="text-sm text-slate-400">Nenhum membro adicionado.</p>}
        <ul className="flex flex-col gap-1 text-sm text-slate-700">
          {form.equipeDirigente.map((membro, index) => (
            <li key={index}>
              {PASTA_DIRIGENTE_LABEL[membro.pasta]}:{' '}
              {membro.tipoMembro === 'JOVEM' ? membro.pessoaNome : membro.casalNome}
            </li>
          ))}
        </ul>
      </Bloco>

      <Bloco titulo="Equipe Comando" onEditar={() => onEditStep('equipe-comando')}>
        <Campo label="Coordenador geral" valor={form.coordenadorGeralNome} />
        <Campo label="Casal coordenador" valor={form.casalCoordenadorNome} />
      </Bloco>
    </div>
  )
}

function Bloco({ titulo, onEditar, children }: { titulo: string; onEditar: () => void; children: ReactNode }) {
  return (
    <div>
      <div className="mb-2 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-slate-900">{titulo}</h3>
        <button type="button" className="text-xs font-semibold text-violet-600 hover:underline" onClick={onEditar}>
          Editar
        </button>
      </div>
      {children}
    </div>
  )
}

function Campo({ label, valor }: { label: string; valor: string }) {
  return (
    <p className="text-sm text-slate-700">
      <span className="text-slate-400">{label}:</span> {valor || '—'}
    </p>
  )
}
