import http, { type ApiResult } from './http'
import type { PageResult } from './user'

export interface KbCategory {
  id: number
  parentId: number
  name: string
  description?: string
  sort?: number
  createdAt?: string
  updatedAt?: string
}

export interface KbCategoryTree extends KbCategory {
  children: KbCategoryTree[]
}

export interface KbCategoryForm {
  name: string
  description?: string
  parentId?: number
  sort?: number
}

export interface KbArticle {
  id: number
  categoryId: number
  title: string
  summary?: string
  content?: string
  tags?: string
  author?: string
  views?: number
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface KbArticleQuery {
  page?: number
  size?: number
  keyword?: string
  categoryId?: number
  status?: number
}

export interface KbArticleForm {
  title: string
  summary?: string
  content?: string
  tags?: string
  categoryId?: number
  status?: number
}

export const listKbCategories = () =>
  http.get<unknown, ApiResult<KbCategory[]>>('/content/kb/categories').then((r) => r.data)

export const fetchKbCategoryTree = () =>
  http
    .get<unknown, ApiResult<KbCategoryTree[]>>('/content/kb/categories/tree')
    .then((r) => r.data)

export const createKbCategory = (data: KbCategoryForm) =>
  http
    .post<unknown, ApiResult<number>>('/content/kb/categories', data)
    .then((r) => r.data)

export const updateKbCategory = (id: number, data: KbCategoryForm) =>
  http.put<unknown, ApiResult<void>>(`/content/kb/categories/${id}`, data)

export const deleteKbCategory = (id: number) =>
  http.delete<unknown, ApiResult<void>>(`/content/kb/categories/${id}`)

export const pageKbArticles = (params: KbArticleQuery) =>
  http
    .get<unknown, ApiResult<PageResult<KbArticle>>>('/content/kb/articles', { params })
    .then((r) => r.data)

export const getKbArticle = (id: number) =>
  http.get<unknown, ApiResult<KbArticle>>(`/content/kb/articles/${id}`).then((r) => r.data)

export const createKbArticle = (data: KbArticleForm) =>
  http.post<unknown, ApiResult<number>>('/content/kb/articles', data).then((r) => r.data)

export const updateKbArticle = (id: number, data: KbArticleForm) =>
  http.put<unknown, ApiResult<void>>(`/content/kb/articles/${id}`, data)

export const deleteKbArticle = (id: number) =>
  http.delete<unknown, ApiResult<void>>(`/content/kb/articles/${id}`)
