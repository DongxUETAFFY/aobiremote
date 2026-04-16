import { defineStore } from 'pinia'
import {
  changePassword,
  fetchCurrentUser,
  login,
  logout,
  register,
  sendRegisterCode,
  type ChangePasswordPayload,
  type LoginPayload,
  type RegisterPayload,
  type SendRegisterCodePayload,
} from '@/api/auth'
import type { UserProfile } from '@/types/auth'

const TOKEN_STORAGE_KEY = 'aobi-access-token'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    accessToken: localStorage.getItem(TOKEN_STORAGE_KEY) || '',
    user: null as UserProfile | null,
    initialized: false,
  }),
  getters: {
    isAuthenticated: (state) => Boolean(state.accessToken && state.user),
  },
  actions: {
    setAccessToken(token: string) {
      this.accessToken = token
      if (token) {
        localStorage.setItem(TOKEN_STORAGE_KEY, token)
      } else {
        localStorage.removeItem(TOKEN_STORAGE_KEY)
      }
    },
    async initialize() {
      if (this.initialized) {
        return
      }
      if (!this.accessToken) {
        this.initialized = true
        return
      }
      try {
        const response = await fetchCurrentUser()
        this.user = response.data
      } catch {
        this.clearSession()
      } finally {
        this.initialized = true
      }
    },
    async sendRegisterCode(payload: SendRegisterCodePayload) {
      return sendRegisterCode(payload)
    },
    async register(payload: RegisterPayload) {
      return register(payload)
    },
    async login(payload: LoginPayload) {
      const response = await login(payload)
      this.setAccessToken(response.data.token)
      this.user = response.data.user
      this.initialized = true
      return response
    },
    async refreshCurrentUser() {
      const response = await fetchCurrentUser()
      this.user = response.data
      return response
    },
    async logout() {
      try {
        if (this.accessToken) {
          await logout()
        }
      } finally {
        this.clearSession()
      }
    },
    async changePassword(payload: ChangePasswordPayload) {
      await changePassword(payload)
      this.clearSession()
    },
    clearSession() {
      this.setAccessToken('')
      this.user = null
      this.initialized = true
    },
  },
})
