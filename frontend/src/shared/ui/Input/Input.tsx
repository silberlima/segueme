import type { InputProps } from './Input.types'

export function Input({
  label,
  name,
  type = 'text',
  value,
  placeholder,
  maxLength,
  error,
  disabled,
  onChange,
}: InputProps) {
  return (
    <label className="flex flex-col gap-1.5">
      {label && <span className="text-sm font-medium text-slate-700">{label}</span>}
      <input
        name={name}
        type={type}
        value={value}
        placeholder={placeholder}
        maxLength={maxLength}
        disabled={disabled}
        onChange={onChange}
        className={`h-10 rounded-lg border px-3 text-sm text-slate-900 outline-none transition-colors focus:border-violet-500 disabled:opacity-50 ${
          error ? 'border-rose-400' : 'border-slate-200'
        }`}
      />
      {error && <span className="text-xs text-rose-600">{error}</span>}
    </label>
  )
}
