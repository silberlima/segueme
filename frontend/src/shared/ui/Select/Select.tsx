import type { SelectProps } from './Select.types'

export function Select({
  label,
  name,
  value,
  options,
  placeholder,
  error,
  disabled,
  onChange,
}: SelectProps) {
  return (
    <label className="flex flex-col gap-1.5">
      {label && <span className="text-sm font-medium text-slate-700">{label}</span>}
      <select
        name={name}
        value={value}
        disabled={disabled}
        onChange={onChange}
        className={`h-10 rounded-lg border bg-white px-3 text-sm text-slate-900 outline-none transition-colors focus:border-violet-500 disabled:opacity-50 ${
          error ? 'border-rose-400' : 'border-slate-200'
        }`}
      >
        {placeholder && (
          <option value="" disabled>
            {placeholder}
          </option>
        )}
        {options.map((option) => (
          <option key={option.value} value={option.value}>
            {option.label}
          </option>
        ))}
      </select>
      {error && <span className="text-xs text-rose-600">{error}</span>}
    </label>
  )
}
