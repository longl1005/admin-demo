import http, { type ApiResult } from './http'

export interface SysUser {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  status: number
  createdAt?: string
  updatedAt?: string
  password?: string
}

export interface PageResult<T> {
  total: number
  records: T[]
}

export interface UserQuery {
  page?: number
  size?: number
  keyword?: string
}

export interface UserSaveDTO {
  id?: number
  username: string
  password?: string
  nickname?: string
  email?: string
  avatar?: string
  status?: number
  roleIds?: number[]
}

export const pageUsers = (params: UserQuery) =>
  http
    .get<unknown, ApiResult<PageResult<SysUser>>>('/system/users', { params })
    .then((r) => r.data)

export const getUserRoleIds = (id: number) =>
  http.get<unknown, ApiResult<number[]>>(`/system/users/${id}/roles`).then((r) => r.data)

export const createUser = (data: UserSaveDTO) =>
  http.post<unknown, ApiResult<number>>('/system/users', data).then((r) => r.data)

export const updateUser = (id: number, data: UserSaveDTO) =>
  http.put<unknown, ApiResult<void>>(`/system/users/${id}`, data)

export const deleteUser = (id: number) =>
  http.delete<unknown, ApiResult<void>>(`/system/users/${id}`)
