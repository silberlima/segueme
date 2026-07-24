import { useState } from 'react'
import { Badge, Button, Select } from '../../../../shared/ui'
import { CasalSearchSelect } from '../CasalSearchSelect'
import { PersonSearchSelect } from '../PersonSearchSelect'
import { PASTAS_DIRIGENTE, PASTA_DIRIGENTE_LABEL } from '../../domain/encontro.types'
import type { PastaDirigente, TipoMembroDirigente } from '../../domain/encontro.types'
import type { WizardMembroDirigente } from './wizard.types'

interface StepEquipeDirigenteProps {
  equipeDirigente: WizardMembroDirigente[]
  onAdd: (membro: WizardMembroDirigente) => string | null
  onRemove: (index: number) => void
}

export function StepEquipeDirigente({ equipeDirigente, onAdd, onRemove }: StepEquipeDirigenteProps) {
  return (
    <div className="flex flex-col gap-4">
      <p className="text-sm text-slate-500">
        Cada pasta tem até 2 jovens e 1 casal. É permitido deixar uma pasta incompleta — o encontro pode ser criado
        mesmo assim (RN-21).
      </p>
      {PASTAS_DIRIGENTE.map((pasta) => (
        <PastaDirigenteCard
          key={pasta}
          pasta={pasta}
          membros={equipeDirigente
            .map((membro, index) => ({ membro, index }))
            .filter(({ membro }) => membro.pasta === pasta)}
          onAdd={onAdd}
          onRemove={onRemove}
        />
      ))}
    </div>
  )
}

interface PastaDirigenteCardProps {
  pasta: PastaDirigente
  membros: { membro: WizardMembroDirigente; index: number }[]
  onAdd: (membro: WizardMembroDirigente) => string | null
  onRemove: (index: number) => void
}

function PastaDirigenteCard({ pasta, membros, onAdd, onRemove }: PastaDirigenteCardProps) {
  const [tipoMembro, setTipoMembro] = useState<TipoMembroDirigente>('JOVEM')
  const [pessoaId, setPessoaId] = useState('')
  const [pessoaNome, setPessoaNome] = useState('')
  const [casalId, setCasalId] = useState('')
  const [casalNome, setCasalNome] = useState('')
  const [erro, setErro] = useState<string | null>(null)

  function limpar() {
    setPessoaId('')
    setPessoaNome('')
    setCasalId('')
    setCasalNome('')
  }

  function adicionar() {
    if (tipoMembro === 'JOVEM' && !pessoaId) {
      setErro('Selecione a pessoa.')
      return
    }
    if (tipoMembro === 'CASAL' && !casalId) {
      setErro('Selecione o casal.')
      return
    }

    const membro: WizardMembroDirigente =
      tipoMembro === 'JOVEM' ? { pasta, tipoMembro, pessoaId, pessoaNome } : { pasta, tipoMembro, casalId, casalNome }

    const resultado = onAdd(membro)
    if (resultado) {
      setErro(resultado)
      return
    }
    setErro(null)
    limpar()
  }

  return (
    <div className="rounded-xl border border-slate-200 p-4">
      <div className="mb-3 flex items-center justify-between">
        <h3 className="text-sm font-semibold text-slate-900">{PASTA_DIRIGENTE_LABEL[pasta]}</h3>
        <Badge tone={membros.length >= 3 ? 'success' : 'neutral'}>{membros.length}/3</Badge>
      </div>

      {membros.length > 0 && (
        <ul className="mb-3 flex flex-col gap-2">
          {membros.map(({ membro, index }) => (
            <li
              key={index}
              className="flex items-center justify-between rounded-lg bg-slate-50 px-3 py-2 text-sm text-slate-900"
            >
              <span>
                {membro.tipoMembro === 'JOVEM' ? membro.pessoaNome : membro.casalNome}
                <span className="ml-2 text-xs text-slate-400">{membro.tipoMembro === 'JOVEM' ? 'Jovem' : 'Casal'}</span>
              </span>
              <button
                type="button"
                className="text-xs font-semibold text-rose-600 hover:underline"
                onClick={() => onRemove(index)}
              >
                Remover
              </button>
            </li>
          ))}
        </ul>
      )}

      <div className="flex flex-wrap items-end gap-2">
        <div className="w-32">
          <Select
            label="Tipo"
            value={tipoMembro}
            options={[
              { value: 'JOVEM', label: 'Jovem' },
              { value: 'CASAL', label: 'Casal' },
            ]}
            onChange={(event) => {
              setTipoMembro(event.target.value as TipoMembroDirigente)
              limpar()
              setErro(null)
            }}
          />
        </div>
        <div className="min-w-[220px] flex-1">
          {tipoMembro === 'JOVEM' ? (
            <PersonSearchSelect
              label="Pessoa"
              selectedId={pessoaId}
              selectedLabel={pessoaNome}
              onSelect={(id, nome) => {
                setPessoaId(id)
                setPessoaNome(nome)
              }}
              onClear={() => {
                setPessoaId('')
                setPessoaNome('')
              }}
            />
          ) : (
            <CasalSearchSelect
              label="Casal"
              selectedId={casalId}
              selectedLabel={casalNome}
              onSelect={(id, label) => {
                setCasalId(id)
                setCasalNome(label)
              }}
              onClear={() => {
                setCasalId('')
                setCasalNome('')
              }}
            />
          )}
        </div>
        <Button variant="secondary" onClick={adicionar}>
          Adicionar
        </Button>
      </div>
      {erro && <p className="mt-2 text-xs text-rose-600">{erro}</p>}
    </div>
  )
}
