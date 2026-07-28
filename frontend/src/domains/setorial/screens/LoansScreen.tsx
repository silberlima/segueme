import { useState } from 'react'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, Select, Skeleton } from '../../../shared/ui'
import { STATUS_EMPRESTIMO_LABEL } from '../domain/setorial.types'
import type { Emprestimo } from '../domain/setorial.types'
import { useCandidatosEmprestimo } from '../hooks/useCandidatosEmprestimo'
import { useCompetenciaOptions } from '../hooks/useCompetenciaOptions'
import { useEfetivarEmprestimo } from '../hooks/useEfetivarEmprestimo'
import { useEmprestimos } from '../hooks/useEmprestimos'
import { useEncontroOptions } from '../hooks/useEncontroOptions'
import { useEnviarCandidatos } from '../hooks/useEnviarCandidatos'
import { useEquipeOptions } from '../hooks/useEquipeOptions'
import { useParoquiaOptions } from '../hooks/useParoquiaOptions'
import { useRecusarEmprestimo } from '../hooks/useRecusarEmprestimo'
import { useSolicitarEmprestimo } from '../hooks/useSolicitarEmprestimo'

const TONE_POR_STATUS = {
  SOLICITADO: 'neutral',
  SUGESTOES_ENVIADAS: 'info',
  EFETIVADO: 'success',
  RECUSADO: 'danger',
} as const

/**
 * Empréstimos (RF-042 a 046, RN-18): uma única tela com duas visões por perfil — Montagem
 * solicita e efetiva, Setorial filtra/seleciona candidatos e envia (fluxo bidirecional
 * obrigatório, RN-MOVIMENTO-04).
 */
export function LoansScreen() {
  const { perfilAtivo } = useProfile()
  const isMontagem = perfilAtivo === 'EQUIPE_MONTAGEM'
  const isSetorial = perfilAtivo === 'SETORIAL'

  const { data: emprestimos, isLoading, isError, refetch } = useEmprestimos()

  return (
    <div className="p-6">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Empréstimos</h1>

      {isMontagem && <NovaSolicitacao />}

      <h2 className="mb-3 text-sm font-semibold text-slate-900">Solicitações</h2>

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar os empréstimos.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {emprestimos && emprestimos.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum empréstimo registrado.
        </div>
      )}

      <div className="flex flex-col gap-3">
        {emprestimos?.map((emprestimo) => (
          <EmprestimoCard key={emprestimo.id} emprestimo={emprestimo} isMontagem={isMontagem} isSetorial={isSetorial} />
        ))}
      </div>
    </div>
  )
}

function NovaSolicitacao() {
  const [encontroId, setEncontroId] = useState('')
  const [equipeId, setEquipeId] = useState('')
  const [competenciaRequeridaId, setCompetenciaRequeridaId] = useState('')

  const { data: encontros } = useEncontroOptions()
  const { data: equipes } = useEquipeOptions(encontroId || undefined)
  const { data: competencias } = useCompetenciaOptions()
  const solicitar = useSolicitarEmprestimo()

  const formularioValido = Boolean(encontroId) && Boolean(equipeId)

  function solicitarEmprestimo() {
    if (!formularioValido) return
    solicitar.mutate(
      { encontroId, equipeId, competenciaRequeridaId: competenciaRequeridaId || undefined },
      {
        onSuccess: () => {
          setEncontroId('')
          setEquipeId('')
          setCompetenciaRequeridaId('')
        },
      },
    )
  }

  return (
    <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
      <h2 className="mb-4 text-sm font-semibold text-slate-900">Nova solicitação</h2>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
        <Select
          label="Encontro"
          value={encontroId}
          placeholder="Selecione o encontro"
          options={encontros?.map((encontro) => ({ value: encontro.id, label: `#${encontro.numero} · ${encontro.padroeiro}` })) ?? []}
          onChange={(event) => {
            setEncontroId(event.target.value)
            setEquipeId('')
          }}
        />

        <Select
          label="Equipe"
          value={equipeId}
          placeholder="Selecione a equipe"
          disabled={!encontroId}
          options={equipes?.map((equipe) => ({ value: equipe.id, label: equipe.nome })) ?? []}
          onChange={(event) => setEquipeId(event.target.value)}
        />

        <Select
          label="Habilidade necessária (opcional)"
          value={competenciaRequeridaId}
          placeholder="Qualquer"
          options={competencias?.map((competencia) => ({ value: competencia.id, label: competencia.nome })) ?? []}
          onChange={(event) => setCompetenciaRequeridaId(event.target.value)}
        />
      </div>

      <div className="mt-4 flex justify-end">
        <Button disabled={!formularioValido || solicitar.isPending} onClick={solicitarEmprestimo}>
          Solicitar empréstimo
        </Button>
      </div>
    </div>
  )
}

interface EmprestimoCardProps {
  emprestimo: Emprestimo
  isMontagem: boolean
  isSetorial: boolean
}

function EmprestimoCard({ emprestimo, isMontagem, isSetorial }: EmprestimoCardProps) {
  const efetivar = useEfetivarEmprestimo()
  const [gerenciandoCandidatos, setGerenciandoCandidatos] = useState(false)

  return (
    <div className="rounded-lg border border-slate-200 bg-white p-4">
      <div className="flex items-center justify-between">
        <div>
          <p className="text-sm font-medium text-slate-900">
            {emprestimo.equipeNome} · Encontro #{emprestimo.encontroNumero}
          </p>
          <p className="text-xs text-slate-500">
            {emprestimo.paroquiaSolicitanteNome}
            {emprestimo.competenciaRequeridaNome && ` · Habilidade: ${emprestimo.competenciaRequeridaNome}`}
          </p>
        </div>
        <Badge tone={TONE_POR_STATUS[emprestimo.status]}>{STATUS_EMPRESTIMO_LABEL[emprestimo.status]}</Badge>
      </div>

      {emprestimo.candidatos.length > 0 && (
        <div className="mt-3 flex flex-wrap gap-1.5">
          {emprestimo.candidatos.map((candidato) => (
            <Badge key={candidato.id} tone={candidato.status === 'EFETIVADO' ? 'success' : 'neutral'}>
              {candidato.pessoaNome} ({candidato.paroquiaOrigemNome})
            </Badge>
          ))}
        </div>
      )}

      {isMontagem && emprestimo.status === 'SUGESTOES_ENVIADAS' && (
        <div className="mt-3 flex justify-end">
          <Button disabled={efetivar.isPending} onClick={() => efetivar.mutate(emprestimo.id)}>
            Adicionar às equipes
          </Button>
        </div>
      )}

      {isSetorial && emprestimo.status === 'SOLICITADO' && (
        <div className="mt-3">
          <Button variant="secondary" onClick={() => setGerenciandoCandidatos((atual) => !atual)}>
            {gerenciandoCandidatos ? 'Fechar' : 'Selecionar candidatos'}
          </Button>
          {gerenciandoCandidatos && <SelecaoCandidatos emprestimoId={emprestimo.id} onEnviado={() => setGerenciandoCandidatos(false)} />}
        </div>
      )}
    </div>
  )
}

function SelecaoCandidatos({ emprestimoId, onEnviado }: { emprestimoId: string; onEnviado: () => void }) {
  const [paroquiaId, setParoquiaId] = useState('')
  const [competenciaId, setCompetenciaId] = useState('')
  const [selecionados, setSelecionados] = useState<Set<string>>(new Set())
  const [justificativaRecusa, setJustificativaRecusa] = useState('')

  const { data: paroquias } = useParoquiaOptions()
  const { data: competencias } = useCompetenciaOptions()
  const { data: candidatos, isLoading } = useCandidatosEmprestimo(emprestimoId, {
    paroquiaId: paroquiaId || undefined,
    competenciaId: competenciaId || undefined,
  })
  const enviar = useEnviarCandidatos()
  const recusar = useRecusarEmprestimo()

  function alternarSelecao(pessoaId: string) {
    setSelecionados((atual) => {
      const novo = new Set(atual)
      if (novo.has(pessoaId)) {
        novo.delete(pessoaId)
      } else {
        novo.add(pessoaId)
      }
      return novo
    })
  }

  return (
    <div className="mt-3 rounded-lg border border-slate-100 bg-slate-50 p-4">
      <div className="mb-3 grid grid-cols-1 gap-3 sm:grid-cols-2">
        <Select
          label="Filtrar por paróquia"
          value={paroquiaId}
          placeholder="Todas"
          options={paroquias?.map((paroquia) => ({ value: paroquia.id, label: paroquia.nome })) ?? []}
          onChange={(event) => setParoquiaId(event.target.value)}
        />
        <Select
          label="Filtrar por habilidade"
          value={competenciaId}
          placeholder="Todas"
          options={competencias?.map((competencia) => ({ value: competencia.id, label: competencia.nome })) ?? []}
          onChange={(event) => setCompetenciaId(event.target.value)}
        />
      </div>

      {isLoading && <Skeleton variant="rect" height="60px" />}

      {candidatos && candidatos.length === 0 && (
        <div className="rounded-lg border border-slate-200 bg-white p-3 text-sm text-slate-500">
          Nenhum candidato encontrado para este filtro.
        </div>
      )}

      {candidatos && candidatos.length > 0 && (
        <div className="flex flex-col gap-2">
          {candidatos.map((candidato) => (
            <label
              key={candidato.pessoaId}
              className="flex cursor-pointer items-center gap-2 rounded-lg border border-slate-200 bg-white px-3 py-2 text-sm"
            >
              <input
                type="checkbox"
                checked={selecionados.has(candidato.pessoaId)}
                onChange={() => alternarSelecao(candidato.pessoaId)}
              />
              <span className="font-medium text-slate-900">{candidato.pessoaNome}</span>
              <span className="text-xs text-slate-500">
                {candidato.paroquiaNome}
                {candidato.competencias.length > 0 && ` · ${candidato.competencias.join(', ')}`}
              </span>
            </label>
          ))}
        </div>
      )}

      <div className="mt-4 flex flex-col gap-2 sm:flex-row sm:items-center sm:justify-between">
        <input
          type="text"
          value={justificativaRecusa}
          onChange={(event) => setJustificativaRecusa(event.target.value)}
          placeholder="Justificativa (obrigatória para recusar)"
          className="h-9 flex-1 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
        />
        <div className="flex gap-2">
          <Button
            variant="danger"
            disabled={!justificativaRecusa.trim() || recusar.isPending}
            onClick={() => recusar.mutate({ emprestimoId, justificativa: justificativaRecusa })}
          >
            Recusar solicitação
          </Button>
          <Button
            disabled={selecionados.size === 0 || enviar.isPending}
            onClick={() =>
              enviar.mutate(
                { emprestimoId, pessoaIds: Array.from(selecionados) },
                { onSuccess: onEnviado },
              )
            }
          >
            Enviar à Montagem
          </Button>
        </div>
      </div>
    </div>
  )
}
