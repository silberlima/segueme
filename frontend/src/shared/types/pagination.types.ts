export interface Pagination<T> {
  content: T[]
  page: number
  size: number
  totalElements: number
  totalPages: number
}

export interface ApiError {
  status: number
  message: string
  details?: string[]
}
