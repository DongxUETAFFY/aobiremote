export interface UserProfile {
  id: number
  email: string
  nickname: string
  avatarUrl: string
  admin: boolean
}

export interface LoginResponse {
  token: string
  tokenName: string
  user: UserProfile
}

export interface RegisterResponse {
  userId: number
}

export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp?: string
}
