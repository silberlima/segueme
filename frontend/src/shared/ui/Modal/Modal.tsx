import type { ModalProps } from './Modal.types'
import { useModal } from './useModal'

export function Modal({ isOpen, onClose, title, children }: ModalProps) {
  useModal({ isOpen, onClose })

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center">
      <div className="absolute inset-0 bg-slate-900/50" onClick={onClose} />
      <div
        role="dialog"
        aria-modal="true"
        aria-label={title}
        className="relative z-10 w-full max-w-lg rounded-xl bg-white p-6 shadow-xl"
      >
        {title && <h2 className="mb-4 text-lg font-semibold text-slate-900">{title}</h2>}
        {children}
      </div>
    </div>
  )
}
