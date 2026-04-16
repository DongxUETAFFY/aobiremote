import http from '@/api/http'
import type {
  PublicPostPageApiResponse,
  PublicPostDetailApiResponse,
  PublicPostUpsertApiResponse,
  PublicPostCreateRequest,
  PublicPostUpdateRequest,
  PublicPostPageQuery,
} from '@/types/public-post'

export const getPublicPostPage = async (query?: PublicPostPageQuery) => {
  const { data } = await http.get<PublicPostPageApiResponse>('/public-post/page', { params: query })
  return data
}

export const getPublicPostDetail = async (id: number) => {
  const { data } = await http.get<PublicPostDetailApiResponse>(`/public-post/${id}`)
  return data
}

export const createPublicPost = async (payload: PublicPostCreateRequest) => {
  const { data } = await http.post<PublicPostUpsertApiResponse>('/public-post', payload)
  return data
}

export const updatePublicPost = async (id: number, payload: PublicPostUpdateRequest) => {
  const { data } = await http.put<PublicPostUpsertApiResponse>(`/public-post/${id}`, payload)
  return data
}

export const deletePublicPost = async (id: number, requestId: string) => {
  const { data } = await http.delete<PublicPostUpsertApiResponse>(`/public-post/${id}`, {
    params: { requestId },
  })
  return data
}

export const togglePublicPostUntrusted = async (id: number, requestId: string) => {
  const { data } = await http.post<PublicPostUpsertApiResponse>(`/public-post/${id}/toggle-untrusted`, null, {
    params: { requestId },
  })
  return data
}
