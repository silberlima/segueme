import { useState } from 'react'
import { Link, useParams } from 'react-router-dom'
import { ListState, Tabs } from '../../../shared/ui'
import { PASTAS_DIRIGENTE, PASTA_DIRIGENTE_LABEL } from '../domain/encontro.types'
import type { PastaDirigente } from '../domain/encontro.types'
import { useEncontro } from '../hooks/useEncontro'

export function DirigenteScreen() {
  const { id } = useParams<{ id: string }>()
  const { data: encontro, isLoading, isError, refetch } = useEncontro(id)
  const [pastaAtiva, setPastaAtiva] = useState<PastaDirigente>('FINANCAS')

  return (
    <div className="p-6">
      <Link to={`/encontros/${id}`} className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Detalhes do Encontro
      </Link>

      <ListState
        isLoading={isLoading}
        isError={isError}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="160px"
        errorMessage="Não foi possível carregar a Equipe Dirigente deste encontro."
      />

      {encontro && (
        <div className="rounded-xl border border-slate-200 bg-white p-6">
          <h1 className="mb-1 text-xl font-semibold text-slate-900">Equipe Dirigente</h1>
          <p className="mb-4 text-sm text-slate-500">
            #{encontro.numero} · {encontro.padroeiro} — acompanhada pelo diretor espiritual{' '}
            {encontro.diretorEspiritualNome}
          </p>

          <Tabs
            tabs={PASTAS_DIRIGENTE.map((pasta) => ({ id: pasta, label: PASTA_DIRIGENTE_LABEL[pasta] }))}
            activeTabId={pastaAtiva}
            onChange={(tabId) => setPastaAtiva(tabId as PastaDirigente)}
          />

          <div className="mt-4">
            {(() => {
              const membros = encontro.equipeDirigente.filter((membro) => membro.pasta === pastaAtiva)
              if (membros.length === 0) {
                return <p className="text-sm text-slate-400">Nenhum membro cadastrado nesta pasta.</p>
              }
              return (
                <ul className="flex flex-col gap-2">
                  {membros.map((membro, index) => (
                    <li
                      key={index}
                      className="flex items-center justify-between rounded-lg border border-slate-200 px-3 py-2 text-sm"
                    >
                      <span className="text-slate-900">
                        {membro.tipoMembro === 'JOVEM' ? membro.pessoaNome : membro.casalNome}
                      </span>
                      <span className="text-xs text-slate-500">{membro.tipoMembro === 'JOVEM' ? 'Jovem' : 'Casal'}</span>
                    </li>
                  ))}
                </ul>
              )
            })()}
          </div>
        </div>
      )}
    </div>
  )
}
