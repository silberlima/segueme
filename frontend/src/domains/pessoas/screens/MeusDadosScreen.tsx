import { Link } from 'react-router-dom'
import { Badge, Button, Input, ListState, PageHeader, PanelCard } from '../../../shared/ui'
import { calcularIdade } from '../domain/pessoa.entity'
import { ESTADO_CIVIL_LABEL, SITUACAO_LABEL } from '../domain/pessoa.types'
import { PersonAvatar } from '../components/PersonAvatar'
import { useMeusDados } from '../hooks/useMeusDados'
import { useMeusDadosForm } from '../hooks/useMeusDadosForm'

function Campo({ label, valor }: { label: string; valor: string | null }) {
  return (
    <div>
      <p className="text-xs font-medium text-slate-500">{label}</p>
      <p className="text-sm text-slate-900">{valor && valor.trim() ? valor : '—'}</p>
    </div>
  )
}

/**
 * Meus Dados: a tela do perfil Seguidor. Ele não enxerga o cadastro dos outros — só o próprio, e
 * altera apenas contato e saúde. Nome, data de nascimento, paróquia e situação aparecem como
 * leitura: mudam por fluxos com mediação (correção de cadastro, Transferência, Inscrição), nunca
 * por autoatendimento.
 */
export function MeusDadosScreen() {
  const { data: pessoa, isLoading, isError, refetch } = useMeusDados()
  const form = useMeusDadosForm(pessoa)

  return (
    <div className="p-6">
      <PageHeader
        titulo="Meus Dados"
        subtitulo="Seu cadastro no movimento"
        acoes={
          pessoa &&
          !form.editando && (
            <div className="flex items-center gap-3">
              {form.salvo && <span className="text-xs text-emerald-600">Dados salvos.</span>}
              <Button onClick={form.editar}>Editar</Button>
            </div>
          )
        }
      />

      <ListState
        isLoading={isLoading}
        isError={isError}
        onRetry={() => refetch()}
        skeletonRows={1}
        skeletonHeight="200px"
        errorMessage="Não foi possível carregar os seus dados. Tente novamente."
      >
        {pessoa && (
          <div className="flex max-w-3xl flex-col gap-6">
            <PanelCard titulo="Identificação">
              <div className="mb-4 flex items-center gap-4">
                <PersonAvatar nomeCompleto={pessoa.nomeCompleto} size="md" />
                <div>
                  <p className="text-lg font-semibold text-slate-900">{pessoa.nomeCompleto}</p>
                  <p className="text-sm text-slate-500">
                    {calcularIdade(pessoa.dataNascimento)} anos · {ESTADO_CIVIL_LABEL[pessoa.estadoCivil]} ·{' '}
                    {pessoa.paroquiaNome}
                  </p>
                </div>
                <div className="ml-auto">
                  <Badge tone="info">{SITUACAO_LABEL[pessoa.situacao]}</Badge>
                </div>
              </div>
              <p className="text-xs text-slate-500">
                Nome, data de nascimento, paróquia e situação são mantidos pela equipe do movimento. Para corrigir
                algum deles, fale com a sua paróquia.
              </p>
            </PanelCard>

            <PanelCard titulo="Contato e saúde">
              {form.editando ? (
                <div className="flex flex-col gap-4">
                  <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                    <Input
                      label="Telefone"
                      value={form.campos.telefone}
                      placeholder="(11) 90000-0000"
                      onChange={(event) => form.alterar('telefone', event.target.value)}
                    />
                    <Input
                      label="Alergias"
                      value={form.campos.alergias ?? ''}
                      onChange={(event) => form.alterar('alergias', event.target.value)}
                    />
                    <Input
                      label="Restrição alimentar"
                      value={form.campos.restricaoAlimentar ?? ''}
                      onChange={(event) => form.alterar('restricaoAlimentar', event.target.value)}
                    />
                    <Input
                      label="Medicação de uso contínuo"
                      value={form.campos.medicacaoUsoContinuo ?? ''}
                      onChange={(event) => form.alterar('medicacaoUsoContinuo', event.target.value)}
                    />
                    <Input
                      label="Contato de emergência"
                      value={form.campos.contatoEmergenciaNome ?? ''}
                      onChange={(event) => form.alterar('contatoEmergenciaNome', event.target.value)}
                    />
                    <Input
                      label="Telefone de emergência"
                      value={form.campos.contatoEmergenciaTelefone ?? ''}
                      onChange={(event) => form.alterar('contatoEmergenciaTelefone', event.target.value)}
                    />
                    <Input
                      label="Parentesco"
                      value={form.campos.contatoEmergenciaParentesco ?? ''}
                      onChange={(event) => form.alterar('contatoEmergenciaParentesco', event.target.value)}
                    />
                  </div>

                  {form.erro && <p className="text-sm text-rose-600">{form.erro}</p>}

                  <div className="flex justify-end gap-2">
                    <Button variant="secondary" disabled={form.isSalvando} onClick={form.cancelar}>
                      Cancelar
                    </Button>
                    <Button disabled={form.isSalvando} onClick={form.salvar}>
                      {form.isSalvando ? 'Salvando…' : 'Salvar'}
                    </Button>
                  </div>
                </div>
              ) : (
                <div className="grid grid-cols-1 gap-4 sm:grid-cols-3">
                  <Campo label="Telefone" valor={pessoa.telefone} />
                  <Campo label="Alergias" valor={pessoa.alergias} />
                  <Campo label="Restrição alimentar" valor={pessoa.restricaoAlimentar} />
                  <Campo label="Medicação de uso contínuo" valor={pessoa.medicacaoUsoContinuo} />
                  <Campo label="Contato de emergência" valor={pessoa.contatoEmergenciaNome} />
                  <Campo label="Telefone de emergência" valor={pessoa.contatoEmergenciaTelefone} />
                  <Campo label="Parentesco" valor={pessoa.contatoEmergenciaParentesco} />
                </div>
              )}
            </PanelCard>

            <PanelCard titulo="Competências">
              <div className="flex flex-wrap gap-1">
                {pessoa.competencias.length === 0 && <span className="text-sm text-slate-400">—</span>}
                {pessoa.competencias.map((competencia) => (
                  <Badge key={competencia} tone="neutral">
                    {competencia}
                  </Badge>
                ))}
              </div>
              <p className="mt-3 text-xs text-slate-500">
                As competências alimentam a montagem das equipes e são atribuídas pela equipe do movimento.
              </p>
            </PanelCard>

            <Link to="/meus-dados/historico" className="text-sm font-medium text-violet-600 hover:underline">
              Ver meu histórico →
            </Link>
          </div>
        )}
      </ListState>
    </div>
  )
}
