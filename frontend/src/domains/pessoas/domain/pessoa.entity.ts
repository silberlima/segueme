import type { Endereco } from './pessoa.types'

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

/**
 * CPF vem da API só com os 11 dígitos — é assim que o cadastro garante a unicidade (RN-PESSOA-01).
 * A pontuação é assunto de exibição.
 */
export function formatarCpf(cpf: string): string {
  const digitos = cpf.replace(/\D/g, '')
  if (digitos.length !== 11) return cpf
  return `${digitos.slice(0, 3)}.${digitos.slice(3, 6)}.${digitos.slice(6, 9)}-${digitos.slice(9)}`
}

/** Endereço em uma linha, para o perfil e a leitura de "Meus Dados". */
export function formatarEndereco(endereco: Endereco): string {
  const rua = [endereco.logradouro, endereco.numero].filter(Boolean).join(', ')
  const comComplemento = endereco.complemento?.trim() ? `${rua} — ${endereco.complemento.trim()}` : rua
  return `${comComplemento} · ${endereco.bairro} · ${endereco.cidade}/${endereco.uf} · CEP ${endereco.cep}`
}

/** Iniciais para o avatar da pessoa (ex.: "Maria Souza" → "MS"). */
export function iniciaisDoNome(nomeCompleto: string): string {
  const partes = nomeCompleto.trim().split(/\s+/)
  const primeira = partes[0]?.[0] ?? ''
  const ultima = partes.length > 1 ? (partes[partes.length - 1]?.[0] ?? '') : ''
  return (primeira + ultima).toUpperCase()
}
