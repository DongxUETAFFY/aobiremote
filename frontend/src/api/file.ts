import http from '@/api/http'
import type { FileUploadApiResponse, FileUploadResult } from '@/types/file'

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

export type { FileUploadResult }
