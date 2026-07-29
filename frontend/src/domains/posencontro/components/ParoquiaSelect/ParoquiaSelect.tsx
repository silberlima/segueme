import { Select } from '../../../../shared/ui'
import { useParoquiaOptions } from '../../hooks/useParoquiaOptions'
import type { ParoquiaSelectProps } from './ParoquiaSelect.types'

/**
 * Seletor da paróquia acompanhada, comum às 3 telas do módulo: o acompanhamento pós-encontro é
 * sempre recortado por paróquia (`pos-encontro.md` 1), e o perfil Pós-Encontro pode responder por
 * mais de uma.
 */
export function ParoquiaSelect({ value, onChange }: ParoquiaSelectProps) {
  const { data: paroquias } = useParoquiaOptions()

  return (
    <Select
      label="Paróquia"
      value={value}
      placeholder="Selecione a paróquia"
      options={(paroquias ?? []).map((paroquia) => ({ value: paroquia.id, label: paroquia.nome }))}
      onChange={(event) => onChange(event.target.value)}
    />
  )
}
