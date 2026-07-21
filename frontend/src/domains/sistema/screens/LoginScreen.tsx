import { Link, Navigate } from 'react-router-dom'
import { useAuth } from '../../../app/providers/AuthProvider'
import { Button, Input } from '../../../shared/ui'
import { useLoginForm } from '../hooks/useLoginForm'

export function LoginScreen() {
  const { isConfigured, isAuthenticated } = useAuth()
  const { email, setEmail, password, setPassword, erro, enviando, submeter } = useLoginForm()

  if (isAuthenticated) {
    return <Navigate to="/" replace />
  }

  return (
    <div className="flex min-h-screen items-center justify-center bg-slate-50 px-4">
      <div className="w-full max-w-md rounded-2xl bg-white p-8 shadow-sm">
        <div className="mb-8 text-center">
          <h1 className="text-2xl font-semibold text-slate-900">Segue-me</h1>
          <p className="mt-2 text-sm text-slate-500">
            Bem-vindo(a) de volta. Entre com sua credencial do movimento para continuar.
          </p>
        </div>

        <form className="space-y-4" onSubmit={submeter}>
          <Input
            type="email"
            label="E-mail"
            name="email"
            value={email}
            onChange={(event) => setEmail(event.target.value)}
            disabled={enviando}
          />
          <Input
            type="password"
            label="Senha"
            name="password"
            value={password}
            onChange={(event) => setPassword(event.target.value)}
            disabled={enviando}
          />

          {erro && <p className="text-xs text-rose-600">{erro}</p>}

          <Button type="submit" variant="primary" disabled={!isConfigured || enviando}>
            {enviando ? 'Entrando…' : 'Entrar'}
          </Button>
        </form>

        {!isConfigured && (
          <p className="mt-3 text-xs text-rose-600">
            Autenticação indisponível: configure VITE_KEYCLOAK_URL, VITE_KEYCLOAK_REALM e
            VITE_KEYCLOAK_CLIENT_ID.
          </p>
        )}

        <div className="mt-6 text-center text-sm">
          <Link to="/recuperar-senha" className="text-violet-600 hover:underline">
            Esqueci a senha
          </Link>
        </div>

        <p className="mt-8 text-center text-xs text-slate-400">
          Em caso de dificuldade de acesso, procure o coordenador da sua paróquia ou o setorial.
        </p>
      </div>
    </div>
  )
}
