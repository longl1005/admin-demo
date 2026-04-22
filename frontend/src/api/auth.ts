import http, { type ApiResult } from './http'

export interface LoginRequest {
  username: string
  password: string
}

export interface LoginResponse {
  token: string
  tokenType: string
  expireMinutes: number
}

export interface UserInfo {
  id: number
  username: string
  nickname?: string
  email?: string
  avatar?: string
  roles: string[]
  permissions: string[]
}

export interface MenuItem {
  id: number
  parentId: number
  name: string
  path?: string
  component?: string
  icon?: string
  sort: number
  type: number
  permission?: string
  children: MenuItem[]
}

export const login = (data: LoginRequest) =>
  http.post<unknown, ApiResult<LoginResponse>>('/auth/login', data).then((r) => r.data)

export const logout = () => http.post<unknown, ApiResult<void>>('/auth/logout')

export const fetchMe = () =>
  http.get<unknown, ApiResult<UserInfo>>('/auth/me').then((r) => r.data)

export const fetchMenus = () =>
  http.get<unknown, ApiResult<MenuItem[]>>('/auth/menus').then((r) => r.data)
