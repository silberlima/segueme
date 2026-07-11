import type { DrawerProps } from './Drawer.types'
import { useDrawer } from './useDrawer'

const positionClasses: Record<'left' | 'right', string> = {
  left: 'left-0',
  right: 'right-0',
}

export function Drawer({ isOpen, onClose, position = 'right', title, children }: DrawerProps) {
  useDrawer({ isOpen, onClose })

  if (!isOpen) return null

  return (
    <div className="fixed inset-0 z-50">
      <div className="absolute inset-0 bg-slate-900/50" onClick={onClose} />
      <div
        className={`absolute top-0 h-full w-full max-w-sm bg-white p-6 shadow-xl ${positionClasses[position]}`}
      >
        {title && <h2 className="mb-4 text-lg font-semibold text-slate-900">{title}</h2>}
        {children}
      </div>
    </div>
  )
}
