import { useState } from 'react'

function buildResetCredentialsUrl(): string | null {
  const authorityUrl = import.meta.env.VITE_KEYCLOAK_URL
  const realm = import.meta.env.VITE_KEYCLOAK_REALM
  const clientId = import.meta.env.VITE_KEYCLOAK_CLIENT_ID

  if (!authorityUrl || !realm || !clientId) return null

  const base = authorityUrl.replace(/\/$/, '')
  const redirectUri = encodeURIComponent(window.location.origin)
  return `${base}/realms/${realm}/login-actions/reset-credentials?client_id=${clientId}&redirect_uri=${redirectUri}`
}

export function useRecuperarSenhaForm() {
  const [email, setEmail] = useState('')
  const [enviado, setEnviado] = useState(false)
  const [erro, setErro] = useState<string | null>(null)

  const enviar = () => {
    const url = buildResetCredentialsUrl()
    if (!url) {
      setErro('Recuperação de senha indisponível: Keycloak não configurado.')
      return
    }
    setErro(null)
    setEnviado(true)
    // A recuperação de senha é delegada ao Keycloak (skills/04): o e-mail digitado aqui
    // é só para paridade visual com o protótipo, quem coleta o e-mail de fato é a
    // página hospedada do Keycloak.
    window.location.assign(url)
  }

  return { email, setEmail, enviado, erro, enviar }
}
