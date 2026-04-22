import { defineStore } from 'pinia'
import { computed, ref } from 'vue'
import { fetchMe, fetchMenus, login as apiLogin, type MenuItem, type UserInfo } from '@/api/auth'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>(localStorage.getItem('token') ?? '')
  const userInfo = ref<UserInfo | null>(null)
  const menus = ref<MenuItem[]>([])

  const isLoggedIn = computed(() => !!token.value)
  const roles = computed(() => userInfo.value?.roles ?? [])
  const permissions = computed(() => userInfo.value?.permissions ?? [])

  const setToken = (value: string) => {
    token.value = value
    if (value) localStorage.setItem('token', value)
    else localStorage.removeItem('token')
  }

  const login = async (username: string, password: string) => {
    const resp = await apiLogin({ username, password })
    setToken(resp.token)
  }

  const loadUserInfo = async () => {
    userInfo.value = await fetchMe()
    menus.value = await fetchMenus()
  }

  const logout = () => {
    setToken('')
    userInfo.value = null
    menus.value = []
  }

  const hasPermission = (code: string) =>
    permissions.value.includes(code) || roles.value.includes('ROLE_ADMIN')

  return {
    token,
    userInfo,
    menus,
    isLoggedIn,
    roles,
    permissions,
    setToken,
    login,
    loadUserInfo,
    logout,
    hasPermission
  }
})
