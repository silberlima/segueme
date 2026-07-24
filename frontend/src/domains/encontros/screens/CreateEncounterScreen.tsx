import { Link } from 'react-router-dom'
import { CreateEncounterWizard } from '../components/CreateEncounterWizard'

export function CreateEncounterScreen() {
  return (
    <div className="p-6">
      <Link to="/encontros" className="mb-4 inline-block text-sm font-medium text-violet-600 hover:underline">
        ← Lista de Encontros
      </Link>
      <h1 className="mb-1 text-xl font-semibold text-slate-900">Novo encontro</h1>
      <p className="mb-6 text-sm text-slate-500">
        Preencha os 5 passos para criar o encontro, montar a Equipe Dirigente e entregá-lo à Equipe Comando.
      </p>
      <CreateEncounterWizard />
    </div>
  )
}
