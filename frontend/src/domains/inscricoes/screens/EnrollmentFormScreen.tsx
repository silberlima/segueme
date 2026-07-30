import { useNavigate, useParams } from 'react-router-dom'
import { Badge, Button, ListState, Stepper } from '../../../shared/ui'
import type { StepperStep } from '../../../shared/ui/Stepper/Stepper.types'
import { usePessoaCompleta } from '../hooks/usePessoaCompleta'
import { usePreencherFicha } from '../hooks/usePreencherFicha'
import { usePreInscricao } from '../hooks/usePreInscricao'

const STEPS: readonly StepperStep[] = [
  { id: 'pre-inscricao', label: 'Pré-inscrição concluída' },
  { id: 'ficha', label: 'Preenchimento da ficha' },
  { id: 'confirmacao', label: 'Confirmação' },
  { id: 'participacao', label: 'Participação' },
]

function Campo({ label, valor }: { label: string; valor: string }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="text-sm text-slate-900">{valor || '—'}</p>
    </div>
  )
}

export function EnrollmentFormScreen() {
  const { id: encontroId, preInscricaoId } = useParams<{ id: string; preInscricaoId: string }>()
  const navigate = useNavigate()

  const { data: preInscricao, isLoading: isLoadingPreInscricao } = usePreInscricao(preInscricaoId)
  const { data: pessoa, isLoading: isLoadingPessoa } = usePessoaCompleta(preInscricao?.pessoaId)
  const preencherFicha = usePreencherFicha()

  const isLoading = isLoadingPreInscricao || isLoadingPessoa
  const isConfirmada = preInscricao?.status === 'CONFIRMADA'
  const activeStepId = isConfirmada ? 'confirmacao' : 'ficha'

  async function confirmar() {
    if (!preInscricaoId) {
      return
    }
    await preencherFicha.mutateAsync(preInscricaoId)
    navigate(`/encontros/${encontroId}/inscricoes`)
  }

  return (
    <div className="p-6">
      <button
        type="button"
        onClick={() => navigate(`/encontros/${encontroId}/inscricoes`)}
        className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline"
      >
        ← Pré-inscrições
      </button>

      <h1 className="mb-1 text-xl font-semibold text-slate-900">Ficha de Inscrição</h1>
      <p className="mb-6 text-sm text-slate-500">
        Os dados desta ficha integram o cadastro nacional e nunca são apagados.
      </p>

      <ListState isLoading={isLoading} skeletonRows={1} skeletonHeight="200px" />

      {pessoa && preInscricao && (
        <div className="grid grid-cols-1 gap-6 lg:grid-cols-[2fr_1fr]">
          <div className="flex flex-col gap-6">
            <section className="rounded-xl border border-slate-200 bg-white p-5">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">Dados pessoais</h2>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                <Campo label="Nome completo" valor={pessoa.nomeCompleto} />
                <Campo label="Data de nascimento" valor={pessoa.dataNascimento} />
                <Campo label="Telefone" valor={pessoa.telefone} />
                <Campo label="Estado civil" valor={pessoa.estadoCivil} />
                <Campo label="Paróquia" valor={pessoa.paroquiaNome} />
              </div>
            </section>

            <section className="rounded-xl border border-slate-200 bg-white p-5">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">Saúde &amp; restrições</h2>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                <Campo label="Alergias" valor={pessoa.alergias ?? 'Nenhuma'} />
                <Campo label="Restrição alimentar" valor={pessoa.restricaoAlimentar ?? 'Nenhuma'} />
                <Campo label="Medicação de uso contínuo" valor={pessoa.medicacaoUsoContinuo ?? 'Nenhuma'} />
              </div>
            </section>

            <section className="rounded-xl border border-slate-200 bg-white p-5">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">Contato de emergência</h2>
              <div className="grid grid-cols-2 gap-4 sm:grid-cols-3">
                <Campo label="Nome" valor={pessoa.contatoEmergenciaNome ?? ''} />
                <Campo label="Telefone" valor={pessoa.contatoEmergenciaTelefone ?? ''} />
                <Campo label="Parentesco" valor={pessoa.contatoEmergenciaParentesco ?? ''} />
              </div>
            </section>

            <section className="rounded-xl border border-slate-200 bg-white p-5">
              <h2 className="mb-3 text-sm font-semibold text-slate-900">Competências</h2>
              {pessoa.competencias.length === 0 && <p className="text-sm text-slate-400">Nenhuma competência informada.</p>}
              <div className="flex flex-wrap gap-2">
                {pessoa.competencias.map((competencia) => (
                  <Badge key={competencia} tone="info">
                    {competencia}
                  </Badge>
                ))}
              </div>
            </section>

            <div className="flex justify-end gap-2">
              <Button variant="secondary" onClick={() => navigate(`/encontros/${encontroId}/inscricoes`)}>
                Cancelar
              </Button>
              <Button onClick={confirmar} disabled={isConfirmada || preencherFicha.isPending}>
                {isConfirmada ? 'Inscrição já confirmada' : 'Confirmar inscrição'}
              </Button>
            </div>
          </div>

          <aside className="rounded-xl border border-slate-200 bg-white p-5">
            <h2 className="mb-4 text-sm font-semibold text-slate-900">Fluxo da inscrição</h2>
            <Stepper steps={STEPS} activeStepId={activeStepId} furthestStepId={activeStepId} onChange={() => {}} />
            {pessoa.indicadoPorNome && (
              <p className="mt-4 text-xs text-slate-500">Indicado por {pessoa.indicadoPorNome}</p>
            )}
          </aside>
        </div>
      )}
    </div>
  )
}
