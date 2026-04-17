import http from '@/api/http'
import type { FileUploadApiResponse, FileUploadResult } from '@/types/file'

const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || '/api').replace(/\/$/, '')

export const uploadImage = async (file: File, scene: 'private' | 'public' = 'private') => {
  const formData = new FormData()
  formData.append('file', file)
  formData.append('scene', scene)
  const { data } = await http.post<FileUploadApiResponse>('/files/images', formData)
  return data
}

export const fetchImagePreviewBlob = async (fileId: string) => {
  const { data } = await http.get<Blob>(`/files/${fileId}/preview`, {
    responseType: 'blob',
  })
  return data
}

export const buildImagePreviewUrl = (fileId?: string | null) => {
  const normalizedFileId = fileId?.trim()
  if (!normalizedFileId) {
    return ''
  }
  return `${apiBaseUrl}/files/${encodeURIComponent(normalizedFileId)}/preview`
}

export type { FileUploadResult }
