/** Calcula a idade a partir da data de nascimento (regra pura, RF-007 — coluna "idade" da lista). */
export function calcularIdade(dataNascimento: string): number {
  const nascimento = new Date(dataNascimento)
  const hoje = new Date()

  let idade = hoje.getFullYear() - nascimento.getFullYear()
  const aniversarioJaPassouEsteAno =
    hoje.getMonth() > nascimento.getMonth() ||
    (hoje.getMonth() === nascimento.getMonth() && hoje.getDate() >= nascimento.getDate())

  if (!aniversarioJaPassouEsteAno) {
    idade -= 1
  }

  return idade
}

/** Iniciais para o avatar da pessoa (ex.: "Maria Souza" → "MS"). */
export function iniciaisDoNome(nomeCompleto: string): string {
  const partes = nomeCompleto.trim().split(/\s+/)
  const primeira = partes[0]?.[0] ?? ''
  const ultima = partes.length > 1 ? (partes[partes.length - 1]?.[0] ?? '') : ''
  return (primeira + ultima).toUpperCase()
}
