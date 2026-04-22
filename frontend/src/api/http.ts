import axios, { AxiosError, type AxiosInstance, type InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'

const http: AxiosInstance = axios.create({
  baseURL: '/api',
  timeout: 15000
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.set('Authorization', `Bearer ${token}`)
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data
    if (body && typeof body === 'object' && 'code' in body) {
      if (body.code === 0) {
        return body
      }
      ElMessage.error(body.message || '请求失败')
      return Promise.reject(new Error(body.message || 'Request failed'))
    }
    return response
  },
  (error: AxiosError<{ code?: number; message?: string }>) => {
    const status = error.response?.status
    const msg = error.response?.data?.message || error.message
    if (status === 401) {
      localStorage.removeItem('token')
      if (!location.pathname.endsWith('/login')) {
        location.href = '/login'
      }
    } else {
      ElMessage.error(msg || '网络错误')
    }
    return Promise.reject(error)
  }
)

export interface ApiResult<T> {
  code: number
  message: string
  data: T
}

export default http
