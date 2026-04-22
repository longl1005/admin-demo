import http, { type ApiResult } from './http'
import type { MenuItem } from './auth'

export const fetchMenuTree = () =>
  http.get<unknown, ApiResult<MenuItem[]>>('/system/menus/tree').then((r) => r.data)
