import http, { type ApiResult } from './http'
import type { PageResult } from './user'

export interface SysRole {
  id: number
  name: string
  code: string
  remark?: string
  createdAt?: string
  updatedAt?: string
}

export interface RoleForm {
  name: string
  code: string
  remark?: string
}

export const pageRoles = (params: { page?: number; size?: number; keyword?: string }) =>
  http
    .get<unknown, ApiResult<PageResult<SysRole>>>('/system/roles', { params })
    .then((r) => r.data)

export const listAllRoles = () =>
  http.get<unknown, ApiResult<SysRole[]>>('/system/roles/all').then((r) => r.data)

export const createRole = (data: RoleForm) =>
  http.post<unknown, ApiResult<number>>('/system/roles', data).then((r) => r.data)

export const updateRole = (id: number, data: RoleForm) =>
  http.put<unknown, ApiResult<void>>(`/system/roles/${id}`, data)

export const deleteRole = (id: number) =>
  http.delete<unknown, ApiResult<void>>(`/system/roles/${id}`)
