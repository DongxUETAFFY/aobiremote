import http from '@/api/http'
import type {
  AdminOverviewApiResponse,
  AdminPageQuery,
  AdminPublicPostPageApiResponse,
  AdminUserApiResponse,
  AdminUserPageApiResponse,
} from '@/types/admin'
import type { ApiResponse } from '@/types/auth'

export const getAdminOverview = async () => {
  const { data } = await http.get<AdminOverviewApiResponse>('/admin/overview')
  return data
}

export const getAdminUsers = async (query?: AdminPageQuery) => {
  const { data } = await http.get<AdminUserPageApiResponse>('/admin/users', { params: query })
  return data
}

export const updateAdminUserStatus = async (id: number, status: 'active' | 'disabled') => {
  const { data } = await http.patch<AdminUserApiResponse>(`/admin/users/${id}/status`, { status })
  return data
}

export const getAdminPublicPosts = async (query?: AdminPageQuery) => {
  const { data } = await http.get<AdminPublicPostPageApiResponse>('/admin/public-posts', { params: query })
  return data
}

export const deleteAdminPublicPost = async (id: number) => {
  const { data } = await http.delete<ApiResponse<null>>(`/admin/public-posts/${id}`)
  return data
}

