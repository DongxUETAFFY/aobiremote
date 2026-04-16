import type { ApiResponse } from '@/types/auth'

export interface FileUploadResult {
  fileId: string
  previewUrl: string
  fileSize: number
  contentType: string
}

export type FileUploadApiResponse = ApiResponse<FileUploadResult>
