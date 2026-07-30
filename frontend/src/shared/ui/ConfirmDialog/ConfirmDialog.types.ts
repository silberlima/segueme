import type { ReactNode } from 'react'

export interface ConfirmDialogProps {
  isOpen: boolean
  title: string
  /** O que exatamente vai acontecer, em contexto, antes da decisão (RNF-05). */
  description?: ReactNode
  confirmLabel?: string
  cancelLabel?: string
  /** `danger` para o que destrói ou nega (recusas, substituição); `default` para o resto. */
  tone?: 'default' | 'danger'
  /**
   * Quando presente, a confirmação exige justificativa textual não vazia (RNF-03/RNF-08) e o texto
   * digitado chega em `onConfirm`. Ausente, `onConfirm` recebe string vazia.
   */
  justification?: {
    label: string
    placeholder?: string
  }
  /** Trava os botões enquanto a mutação está em voo. */
  isPending?: boolean
  onCancel: () => void
  onConfirm: (justificativa: string) => void
}
