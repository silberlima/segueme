import { useQuery } from '@tanstack/react-query'
import { fetchCadastroReserva } from '../services/inscricao.service'

export function useCadastroReserva(encontroId: string | undefined) {
  return useQuery({
    queryKey: ['cadastro-reserva', encontroId],
    queryFn: () => fetchCadastroReserva(encontroId as string),
    enabled: Boolean(encontroId),
  })
}
