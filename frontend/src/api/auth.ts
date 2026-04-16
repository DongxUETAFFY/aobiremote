import http from '@/api/http'
import type { ApiResponse, LoginResponse, RegisterResponse, UserProfile } from '@/types/auth'

export interface SendRegisterCodePayload {
  email: string
}

export interface RegisterPayload {
  email: string
  code: string
  password: string
}

export interface LoginPayload {
  email: string
  password: string
}

export interface ChangePasswordPayload {
  oldPassword: string
  newPassword: string
}

export const sendRegisterCode = async (payload: SendRegisterCodePayload) => {
  const { data } = await http.post<ApiResponse<null>>('/auth/send-register-code', payload)
  return data
}

export const register = async (payload: RegisterPayload) => {
  const { data } = await http.post<ApiResponse<RegisterResponse>>('/auth/register', payload)
  return data
}

export const login = async (payload: LoginPayload) => {
  const { data } = await http.post<ApiResponse<LoginResponse>>('/auth/login', payload)
  return data
}

export const logout = async () => {
  const { data } = await http.post<ApiResponse<null>>('/auth/logout')
  return data
}

export const fetchCurrentUser = async () => {
  const { data } = await http.get<ApiResponse<UserProfile>>('/auth/me')
  return data
}

export const changePassword = async (payload: ChangePasswordPayload) => {
  const { data } = await http.post<ApiResponse<null>>('/auth/change-password', payload)
  return data
}
