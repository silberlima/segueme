import { Link } from 'react-router-dom'
import { Button, Input } from '../../../shared/ui'
import { useRecuperarSenhaForm } from '../hooks/useRecuperarSenhaForm'

export function RecuperarSenhaScreen() {
  const { email, setEmail, enviado, erro, enviar } = useRecuperarSenhaForm()

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <h1 className="text-xl font-semibold text-slate-900">Recuperar senha</h1>
          <p className="mt-2 text-sm text-slate-500">
            Informe o e-mail cadastrado no movimento para receber o link de recuperação.
          </p>
        </div>

        <div className="flex flex-col gap-4">
          <Input
            label="E-mail cadastrado"
            name="email"
            type="email"
            value={email}
            placeholder="voce@exemplo.org"
            onChange={(event) => setEmail(event.target.value)}
          />

          {erro && <p className="text-xs text-rose-600">{erro}</p>}
          {enviado && <p className="text-xs text-emerald-600">Redirecionando para a recuperação de senha...</p>}

          <Button variant="primary" onClick={enviar}>
            Enviar link de recuperação
          </Button>
        </div>

        <div className="mt-6 text-center text-sm">
          <Link to="/login" className="text-violet-600 hover:underline">
            Voltar ao login
          </Link>
        </div>
      </div>
    </div>
  )
}
