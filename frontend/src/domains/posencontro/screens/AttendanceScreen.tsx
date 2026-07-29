import { useState } from 'react'
import { useProfile } from '../../../app/providers/ProfileProvider'
import { Badge, Button, Select, Skeleton, Table } from '../../../shared/ui'
import type { TableColumn } from '../../../shared/ui/Table/Table.types'
import { ParoquiaSelect } from '../components/ParoquiaSelect'
import type { Presenca } from '../domain/posencontro.types'
import { useAgendarEncontroPos } from '../hooks/useAgendarEncontroPos'
import { useEncontrosPos } from '../hooks/useEncontrosPos'
import { usePresencas } from '../hooks/usePresencas'
import { useRegistrarPresencas } from '../hooks/useRegistrarPresencas'

const formatadorData = new Intl.DateTimeFormat('pt-BR', { timeZone: 'UTC' })

/**
 * Presenças (`screen_presencas`, RF-058, UC-20): agenda os encontros pós da paróquia e faz a chamada
 * de cada um. Tela desenhada nesta fase — não existia no protótipo (pendência #20 de
 * `pendencias-gerais.md`), então o layout segue o padrão das demais telas de registro do sistema.
 *
 * <p>A chamada é salva em lote: marcar todo mundo e salvar de uma vez, em vez de uma requisição por
 * jovem. Quem nunca foi conferido aparece como "não conferido", distinto de "ausente".
 */
export function AttendanceScreen() {
  const { perfilAtivo } = useProfile()
  const podeRegistrar = perfilAtivo === 'POS_ENCONTRO' || perfilAtivo === 'PAROQUIA'

  const [paroquiaId, setParoquiaId] = useState('')
  const [encontroPosId, setEncontroPosId] = useState('')
  const [data, setData] = useState('')
  const [tema, setTema] = useState('')
  const [marcacoesLocais, setMarcacoesLocais] = useState<Record<string, boolean>>({})

  const { data: encontrosPos } = useEncontrosPos(paroquiaId)
  const { data: presencas, isLoading, isError, refetch } = usePresencas(encontroPosId)
  const agendar = useAgendarEncontroPos()
  const registrar = useRegistrarPresencas()

  /** O que já está salvo, sobreposto pelo que a equipe acabou de marcar nesta sessão de chamada. */
  function marcacaoDe(presenca: Presenca): boolean | undefined {
    return marcacoesLocais[presenca.pessoaId] ?? presenca.presente ?? undefined
  }

  function marcar(pessoaId: string, presente: boolean) {
    setMarcacoesLocais((atual) => ({ ...atual, [pessoaId]: presente }))
  }

  function trocarParoquia(novaParoquiaId: string) {
    setParoquiaId(novaParoquiaId)
    setEncontroPosId('')
    setMarcacoesLocais({})
  }

  function trocarEncontroPos(novoEncontroPosId: string) {
    setEncontroPosId(novoEncontroPosId)
    setMarcacoesLocais({})
  }

  function agendarEncontroPos() {
    if (!paroquiaId || !data) return
    agendar.mutate(
      { paroquiaId, data, tema: tema.trim() || undefined },
      {
        onSuccess: (encontroPos) => {
          trocarEncontroPos(encontroPos.id)
          setData('')
          setTema('')
        },
      },
    )
  }

  function salvarChamada() {
    if (!encontroPosId || !presencas) return
    registrar.mutate(
      {
        encontroPosId,
        presencas: presencas
          .filter((presenca) => marcacaoDe(presenca) !== undefined)
          .map((presenca) => ({ pessoaId: presenca.pessoaId, presente: marcacaoDe(presenca) as boolean })),
      },
      { onSuccess: () => setMarcacoesLocais({}) },
    )
  }

  const columns: TableColumn<Presenca>[] = [
    { key: 'pessoa', header: 'Jovem', render: (presenca) => presenca.pessoaNome },
    {
      key: 'situacao',
      header: 'Situação atual',
      render: (presenca) =>
        marcacaoDe(presenca) === undefined ? (
          <Badge>Não conferido</Badge>
        ) : (
          <Badge tone={marcacaoDe(presenca) ? 'success' : 'danger'}>
            {marcacaoDe(presenca) ? 'Presente' : 'Ausente'}
          </Badge>
        ),
    },
    {
      key: 'chamada',
      header: 'Chamada',
      render: (presenca) => (
        <div className="flex gap-2">
          <Button
            variant={marcacaoDe(presenca) === true ? 'primary' : 'secondary'}
            disabled={!podeRegistrar}
            onClick={() => marcar(presenca.pessoaId, true)}
          >
            Presente
          </Button>
          <Button
            variant={marcacaoDe(presenca) === false ? 'primary' : 'secondary'}
            disabled={!podeRegistrar}
            onClick={() => marcar(presenca.pessoaId, false)}
          >
            Ausente
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div className="p-6">
      <h1 className="mb-6 text-xl font-semibold text-slate-900">Presenças</h1>

      <div className="mb-8 grid grid-cols-1 gap-4 sm:grid-cols-2">
        <ParoquiaSelect value={paroquiaId} onChange={trocarParoquia} />
        <Select
          label="Encontro pós"
          value={encontroPosId}
          placeholder={paroquiaId ? 'Selecione o encontro pós' : 'Selecione a paróquia primeiro'}
          disabled={!paroquiaId}
          options={(encontrosPos ?? []).map((encontroPos) => ({
            value: encontroPos.id,
            label: `${formatadorData.format(new Date(encontroPos.data))}${encontroPos.tema ? ` — ${encontroPos.tema}` : ''}`,
          }))}
          onChange={(event) => trocarEncontroPos(event.target.value)}
        />
      </div>

      {podeRegistrar && paroquiaId && (
        <div className="mb-8 rounded-xl border border-slate-200 bg-white p-6">
          <h2 className="mb-4 text-sm font-semibold text-slate-900">Agendar encontro pós</h2>

          <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Data</span>
              <input
                type="date"
                value={data}
                onChange={(event) => setData(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>

            <label className="flex flex-col gap-1.5">
              <span className="text-sm font-medium text-slate-700">Tema (opcional)</span>
              <input
                type="text"
                value={tema}
                onChange={(event) => setTema(event.target.value)}
                className="h-10 rounded-lg border border-slate-200 px-3 text-sm text-slate-900 outline-none focus:border-violet-500"
              />
            </label>
          </div>

          {agendar.isError && (
            <p className="mt-3 text-xs text-rose-600">
              Não foi possível agendar. Verifique se já existe um encontro pós nesta paróquia nesta data.
            </p>
          )}

          <div className="mt-4 flex justify-end">
            <Button disabled={!data || agendar.isPending} onClick={agendarEncontroPos}>
              Agendar
            </Button>
          </div>
        </div>
      )}

      {isLoading && <Skeleton variant="rect" height="120px" />}

      {isError && (
        <div className="rounded-xl border border-rose-200 bg-rose-50 p-4 text-sm text-rose-700">
          <p className="mb-3">Não foi possível carregar a lista de chamada.</p>
          <Button variant="secondary" onClick={() => refetch()}>
            Tentar novamente
          </Button>
        </div>
      )}

      {!encontroPosId && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Selecione a paróquia e o encontro pós para fazer a chamada.
        </div>
      )}

      {encontroPosId && presencas && presencas.length === 0 && (
        <div className="rounded-xl border border-slate-200 bg-white p-4 text-sm text-slate-500">
          Nenhum jovem acompanhado nesta paróquia ainda.
        </div>
      )}

      {presencas && presencas.length > 0 && (
        <>
          <div className="overflow-x-auto rounded-xl border border-slate-200 bg-white">
            <Table columns={columns} rows={presencas} getRowKey={(presenca) => presenca.pessoaId} />
          </div>

          {podeRegistrar && (
            <div className="mt-4 flex items-center justify-end gap-4">
              {registrar.isError && (
                <p className="text-xs text-rose-600">Não foi possível salvar a chamada. Tente novamente.</p>
              )}
              {/* Sem esta confirmação a tela fica idêntica antes e depois de salvar — a marcação já
                  aparece só por ter sido clicada, e a equipe não tem como saber se a chamada foi. */}
              {registrar.isSuccess && !registrar.isPending && (
                <p className="text-xs text-emerald-600">Chamada salva.</p>
              )}
              <Button disabled={registrar.isPending} onClick={salvarChamada}>
                Salvar chamada
              </Button>
            </div>
          )}
        </>
      )}
    </div>
  )
}
