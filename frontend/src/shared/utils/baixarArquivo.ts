/**
 * Entrega um arquivo já carregado ao usuário. Fica em `shared/utils` porque é manipulação de DOM
 * pura, sem regra de negócio: nem o serviço (que só fala com a API) nem a tela precisam saber que
 * o download passa por uma URL temporária de objeto.
 */
export function baixarArquivo(conteudo: Blob, nomeArquivo: string): void {
  const url = URL.createObjectURL(conteudo)
  const link = document.createElement('a')
  link.href = url
  link.download = nomeArquivo
  document.body.appendChild(link)
  link.click()
  link.remove()
  URL.revokeObjectURL(url)
}
