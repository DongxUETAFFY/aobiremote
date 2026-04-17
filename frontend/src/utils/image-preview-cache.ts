import { fetchImagePreviewBlob } from '@/api/file'

const objectUrlCache = new Map<string, string>()
const pendingRequestCache = new Map<string, Promise<string>>()

const revokeAllObjectUrls = () => {
  objectUrlCache.forEach((objectUrl) => {
    URL.revokeObjectURL(objectUrl)
  })
  objectUrlCache.clear()
  pendingRequestCache.clear()
}

if (typeof window !== 'undefined') {
  window.addEventListener('beforeunload', revokeAllObjectUrls, { once: true })
}

export const peekImagePreviewObjectUrl = (fileId: string) => objectUrlCache.get(fileId) || ''

export const getImagePreviewObjectUrl = async (fileId: string) => {
  const cachedObjectUrl = objectUrlCache.get(fileId)
  if (cachedObjectUrl) {
    return cachedObjectUrl
  }

  const pendingRequest = pendingRequestCache.get(fileId)
  if (pendingRequest) {
    return pendingRequest
  }

  const request = (async () => {
    const blob = await fetchImagePreviewBlob(fileId)
    const objectUrl = URL.createObjectURL(blob)
    objectUrlCache.set(fileId, objectUrl)
    return objectUrl
  })().finally(() => {
    pendingRequestCache.delete(fileId)
  })

  pendingRequestCache.set(fileId, request)
  return request
}
