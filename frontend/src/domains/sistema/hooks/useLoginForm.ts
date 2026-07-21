import { useState, type FormEvent } from 'react'
import { useAuth } from '../../../app/providers/AuthProvider'

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/

export function useLoginForm() {
  const { login } = useAuth()
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [erro, setErro] = useState<string | null>(null)
  const [enviando, setEnviando] = useState(false)

  const submeter = async (event: FormEvent) => {
    event.preventDefault()
    if (!EMAIL_PATTERN.test(email)) {
      setErro('Informe um e-mail válido.')
      return
    }
    setErro(null)
    setEnviando(true)
    try {
      await login(email, password)
    } catch (error) {
      setErro(error instanceof Error ? error.message : 'Não foi possível entrar.')
    } finally {
      setEnviando(false)
    }
  }

  return { email, setEmail, password, setPassword, erro, enviando, submeter }
}
