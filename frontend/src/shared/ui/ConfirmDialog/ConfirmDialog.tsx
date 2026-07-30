import { useState } from 'react'
import { Button } from '../Button'
import { Modal } from '../Modal'
import type { ConfirmDialogProps } from './ConfirmDialog.types'

/**
 * Confirmação de ação destrutiva ou sensível (RNF-03, Fase 14): nada de recusar convite, recusar
 * transferência/empréstimo ou registrar substituição direto no clique da lista. Quando a ação exige
 * rastro de decisão (RNF-08), `justification` torna o texto obrigatório para liberar o confirmar.
 */
export function ConfirmDialog({ isOpen, title, onCancel, ...resto }: ConfirmDialogProps) {
  return (
    <Modal isOpen={isOpen} onClose={onCancel} title={title}>
      {/* O corpo só monta com o diálogo aberto, então a justificativa de uma recusa nunca sobra
          para a recusa seguinte — sem efeito nenhum para limpar o campo. */}
      <ConteudoDoDialogo onCancel={onCancel} {...resto} />
    </Modal>
  )
}

type ConteudoDoDialogoProps = Omit<ConfirmDialogProps, 'isOpen' | 'title'>

function ConteudoDoDialogo({
  description,
  confirmLabel = 'Confirmar',
  cancelLabel = 'Cancelar',
  tone = 'default',
  justification,
  isPending = false,
  onCancel,
  onConfirm,
}: ConteudoDoDialogoProps) {
  const [justificativa, setJustificativa] = useState('')
  const justificativaPendente = Boolean(justification) && justificativa.trim().length === 0

  return (
    <>
      {description && <div className="mb-4 text-sm text-slate-600">{description}</div>}

      {justification && (
        <label className="mb-4 block">
          <span className="mb-1 block text-sm font-medium text-slate-700">{justification.label}</span>
          <textarea
            value={justificativa}
            onChange={(event) => setJustificativa(event.target.value)}
            placeholder={justification.placeholder}
            rows={3}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 text-sm text-slate-900 outline-none focus:border-violet-500"
          />
        </label>
      )}

      <div className="flex justify-end gap-2">
        <Button variant="secondary" disabled={isPending} onClick={onCancel}>
          {cancelLabel}
        </Button>
        <Button
          variant={tone === 'danger' ? 'danger' : 'primary'}
          disabled={isPending || justificativaPendente}
          onClick={() => onConfirm(justificativa.trim())}
        >
          {confirmLabel}
        </Button>
      </div>
    </>
  )
}
