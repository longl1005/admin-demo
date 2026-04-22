import http, { type ApiResult } from './http'
import type { PageResult } from './user'

export interface Article {
  id: number
  title: string
  summary?: string
  content?: string
  author?: string
  status: number
  createdAt?: string
  updatedAt?: string
}

export interface ArticleQuery {
  page?: number
  size?: number
  keyword?: string
  status?: number
}

export interface ArticleForm {
  title: string
  summary?: string
  content?: string
  status?: number
}

export const pageArticles = (params: ArticleQuery) =>
  http
    .get<unknown, ApiResult<PageResult<Article>>>('/content/articles', { params })
    .then((r) => r.data)

export const getArticle = (id: number) =>
  http.get<unknown, ApiResult<Article>>(`/content/articles/${id}`).then((r) => r.data)

export const createArticle = (data: ArticleForm) =>
  http.post<unknown, ApiResult<number>>('/content/articles', data).then((r) => r.data)

export const updateArticle = (id: number, data: ArticleForm) =>
  http.put<unknown, ApiResult<void>>(`/content/articles/${id}`, data)

export const deleteArticle = (id: number) =>
  http.delete<unknown, ApiResult<void>>(`/content/articles/${id}`)
