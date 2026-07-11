import type { TableProps } from './Table.types'

export function Table<T>({ columns, rows, getRowKey }: TableProps<T>) {
  return (
    <table className="w-full border-collapse text-sm">
      <thead>
        <tr className="border-b border-slate-200 text-left text-slate-500">
          {columns.map((column) => (
            <th key={column.key} className="px-4 py-2.5 font-medium">
              {column.header}
            </th>
          ))}
        </tr>
      </thead>
      <tbody>
        {rows.map((row) => (
          <tr key={getRowKey(row)} className="border-b border-slate-100 text-slate-900">
            {columns.map((column) => (
              <td key={column.key} className="px-4 py-2.5">
                {column.render(row)}
              </td>
            ))}
          </tr>
        ))}
      </tbody>
    </table>
  )
}
