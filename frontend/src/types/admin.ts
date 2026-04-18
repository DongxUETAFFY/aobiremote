import type { ApiResponse } from '@/types/auth'
import type { PublicPostCategory, PublicPostChannel, PublicPostDirection } from '@/types/public-post'

export interface AdminOverview {
  totalUsers: number
  activeUsers: number
  disabledUsers: number
  todayNewUsers: number
  totalInventoryItems: number
  totalPublicPosts: number
  todayPublicPosts: number
  totalFiles: number
  totalImageCount: number
  totalImageBytes: number
  totalStorageBytes: number
  usableStorageBytes: number
  totalUntrustedCount: number
  qps: number
}

export interface AdminPageResponse<T> {
  items: T[]
  pageNo: number
  pageSize: number
  totalCount: number
  totalPages: number
}

export interface AdminUser {
  id: number
  email: string
  nickname: string
  status: 'active' | 'disabled'
  admin: boolean
  lastLoginAt: string | null
  createdAt: string
}

export interface AdminPublicPost {
  id: number
  userId: number
  userEmail: string
  itemName: string
  price: number
  tradeTime: string
  direction: PublicPostDirection
  channel: PublicPostChannel
  category: PublicPostCategory
  imageFileId: string | null
  untrustedCount: number
  createdAt: string
}

export interface AdminPageQuery {
  pageNo?: number
  pageSize?: number
  keyword?: string
  status?: string
}

export type AdminOverviewApiResponse = ApiResponse<AdminOverview>
export type AdminUserPageApiResponse = ApiResponse<AdminPageResponse<AdminUser>>
export type AdminUserApiResponse = ApiResponse<AdminUser>
export type AdminPublicPostPageApiResponse = ApiResponse<AdminPageResponse<AdminPublicPost>>
