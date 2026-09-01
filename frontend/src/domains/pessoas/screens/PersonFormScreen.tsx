import { useState } from 'react'
import { Badge, Button, Input, Select, Tabs } from '../../../shared/ui'
import type { TabItem } from '../../../shared/ui/Tabs/Tabs.types'
import { calcularIdade } from '../domain/pessoa.entity'
import { ESTADO_CIVIL_LABEL } from '../domain/pessoa.types'
import type { EstadoCivil } from '../domain/pessoa.types'
import { useCompetencias } from '../hooks/useCompetencias'
import { useParoquiaOptions } from '../hooks/useParoquiaOptions'
import { usePersonForm } from '../hooks/usePersonForm'

const SECOES: TabItem[] = [
  { id: 'dados', label: 'Dados pessoais' },
  { id: 'endereco', label: 'Endereço' },
  { id: 'saude', label: 'Saúde/restrições' },
  { id: 'emergencia', label: 'Contato de emergência' },
  { id: 'competencias', label: 'Competências' },
]

const ESTADO_CIVIL_OPTIONS = Object.entries(ESTADO_CIVIL_LABEL).map(([value, label]) => ({ value, label }))

export function PersonFormScreen() {
  const [secaoAtiva, setSecaoAtiva] = useState('dados')
  const { data: paroquias } = useParoquiaOptions()
  const { data: competencias } = useCompetencias()
  const { form, errors, setField, toggleCompetencia, salvar, cancelar, isSaving, saveError } = usePersonForm()

  return (
    <div className="p-6">
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Cadastro de Pessoa</h1>
      <p className="mb-6 text-sm text-slate-500">
        Registra uma nova pessoa no cadastro nacional único (RN-PESSOA-01).
      </p>

      <div className="grid grid-cols-1 gap-6 lg:grid-cols-[1fr_280px]">
        <div className="rounded-xl border border-slate-200 bg-white">
          <div className="px-4 pt-2">
            <Tabs tabs={SECOES} activeTabId={secaoAtiva} onChange={setSecaoAtiva} />
          </div>

          <div className="space-y-4 p-4">
            {secaoAtiva === 'dados' && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input
                  label="Nome completo"
                  value={form.nomeCompleto}
                  error={errors.nomeCompleto}
                  onChange={(event) => setField('nomeCompleto', event.target.value)}
                />
                <Input
                  label="CPF"
                  placeholder="000.000.000-00"
                  value={form.cpf}
                  error={errors.cpf}
                  onChange={(event) => setField('cpf', event.target.value)}
                />
                <Input
                  label="Data de nascimento"
                  type="date"
                  value={form.dataNascimento}
                  error={errors.dataNascimento}
                  onChange={(event) => setField('dataNascimento', event.target.value)}
                />
                <Input
                  label="Telefone"
                  type="tel"
                  placeholder="(11) 91234-5678"
                  value={form.telefone}
                  error={errors.telefone}
                  onChange={(event) => setField('telefone', event.target.value)}
                />
                <Input
                  label="Instagram (opcional)"
                  placeholder="@perfil"
                  value={form.instagram}
                  onChange={(event) => setField('instagram', event.target.value)}
                />
                <Select
                  label="Estado civil"
                  value={form.estadoCivil}
                  error={errors.estadoCivil}
                  options={ESTADO_CIVIL_OPTIONS}
                  placeholder="Selecione"
                  onChange={(event) => setField('estadoCivil', event.target.value as EstadoCivil)}
                />
                <Select
                  label="Paróquia"
                  value={form.paroquiaId}
                  error={errors.paroquiaId}
                  options={(paroquias ?? []).map((paroquia) => ({ value: paroquia.id, label: paroquia.nome }))}
                  placeholder="Selecione"
                  onChange={(event) => setField('paroquiaId', event.target.value)}
                />
              </div>
            )}

            {secaoAtiva === 'endereco' && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input
                  label="CEP"
                  placeholder="00000-000"
                  value={form.cep}
                  error={errors.cep}
                  onChange={(event) => setField('cep', event.target.value)}
                />
                <Input
                  label="Logradouro"
                  value={form.logradouro}
                  error={errors.logradouro}
                  onChange={(event) => setField('logradouro', event.target.value)}
                />
                <Input
                  label="Número"
                  value={form.numero}
                  error={errors.numero}
                  onChange={(event) => setField('numero', event.target.value)}
                />
                <Input
                  label="Complemento (opcional)"
                  value={form.complemento}
                  onChange={(event) => setField('complemento', event.target.value)}
                />
                <Input
                  label="Bairro"
                  value={form.bairro}
                  error={errors.bairro}
                  onChange={(event) => setField('bairro', event.target.value)}
                />
                <Input
                  label="Cidade"
                  value={form.cidade}
                  error={errors.cidade}
                  onChange={(event) => setField('cidade', event.target.value)}
                />
                <Input
                  label="UF"
                  placeholder="SP"
                  maxLength={2}
                  value={form.uf}
                  error={errors.uf}
                  onChange={(event) => setField('uf', event.target.value)}
                />
              </div>
            )}

            {secaoAtiva === 'saude' && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input
                  label="Alergias"
                  value={form.alergias}
                  onChange={(event) => setField('alergias', event.target.value)}
                />
                <Input
                  label="Restrição alimentar"
                  value={form.restricaoAlimentar}
                  onChange={(event) => setField('restricaoAlimentar', event.target.value)}
                />
                <Input
                  label="Medicação de uso contínuo"
                  value={form.medicacaoUsoContinuo}
                  onChange={(event) => setField('medicacaoUsoContinuo', event.target.value)}
                />
              </div>
            )}

            {secaoAtiva === 'emergencia' && (
              <div className="grid grid-cols-1 gap-4 sm:grid-cols-2">
                <Input
                  label="Nome do contato"
                  value={form.contatoEmergenciaNome}
                  onChange={(event) => setField('contatoEmergenciaNome', event.target.value)}
                />
                <Input
                  label="Telefone do contato"
                  type="tel"
                  value={form.contatoEmergenciaTelefone}
                  onChange={(event) => setField('contatoEmergenciaTelefone', event.target.value)}
                />
                <Input
                  label="Parentesco"
                  value={form.contatoEmergenciaParentesco}
                  onChange={(event) => setField('contatoEmergenciaParentesco', event.target.value)}
                />
              </div>
            )}

            {secaoAtiva === 'competencias' && (
              <div className="flex flex-wrap gap-2">
                {(competencias ?? []).length === 0 && (
                  <p className="text-sm text-slate-500">Nenhuma competência cadastrada no catálogo nacional.</p>
                )}
                {(competencias ?? []).map((competencia) => {
                  const selecionada = form.competenciaIds.includes(competencia.id)
                  return (
                    <button
                      key={competencia.id}
                      type="button"
                      onClick={() => toggleCompetencia(competencia.id)}
                      className={`rounded-full border px-3 py-1.5 text-sm font-medium transition-colors ${
                        selecionada
                          ? 'border-violet-600 bg-violet-50 text-violet-700'
                          : 'border-slate-200 bg-white text-slate-600 hover:border-slate-300'
                      }`}
                    >
                      {competencia.nome}
                    </button>
                  )
                })}
              </div>
            )}
          </div>

          <div className="flex items-center justify-between border-t border-slate-200 p-4">
            {saveError && <span className="text-sm text-rose-600">Não foi possível salvar. Tente novamente.</span>}
            <div className="ml-auto flex gap-2">
              <Button variant="secondary" onClick={cancelar} disabled={isSaving}>
                Cancelar
              </Button>
              <Button onClick={salvar} disabled={isSaving}>
                {isSaving ? 'Salvando…' : 'Salvar cadastro'}
              </Button>
            </div>
          </div>
        </div>

        <aside className="h-fit rounded-xl border border-slate-200 bg-white p-4">
          <p className="mb-3 text-sm font-medium text-slate-700">Resumo</p>
          <p className="text-sm font-semibold text-slate-900">{form.nomeCompleto || 'Nova pessoa'}</p>
          {form.dataNascimento && (
            <p className="mt-1 text-xs text-slate-500">{calcularIdade(form.dataNascimento)} anos</p>
          )}
          <div className="mt-3">
            <Badge tone="neutral">Pré-inscrito</Badge>
          </div>
        </aside>
      </div>
    </div>
  )
}
