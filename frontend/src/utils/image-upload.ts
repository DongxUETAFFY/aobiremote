const DIRECT_UPLOAD_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])
const HEIC_TYPES = new Set(['image/heic', 'image/heif', 'image/heic-sequence', 'image/heif-sequence'])
const SUPPORTED_EXTENSIONS = new Set(['jpg', 'jpeg', 'png', 'webp', 'heic', 'heif'])
const TARGET_SIZE = 400 * 1024
const ACCEPTABLE_MAX_SIZE = 500 * 1024
const DIMENSION_STEPS = [1600, 1280, 1024, 800]
const QUALITY_STEPS = [0.9, 0.82, 0.74, 0.66, 0.58]

export const IMAGE_INPUT_ACCEPT = 'image/jpeg,image/png,image/webp,image/heic,image/heif,.heic,.heif'

export interface CompressionResult {
  file: File
  originalSize: number
  compressedSize: number
  compressed: boolean
}

export const compressImageBeforeUpload = async (sourceFile: File): Promise<CompressionResult> => {
  if (!isSupportedInputFile(sourceFile)) {
    throw new Error('仅支持 JPG、PNG、WEBP、HEIC、HEIF 图片')
  }

  const normalizedFile = await normalizeInputImageFile(sourceFile)

  if (normalizedFile.size <= TARGET_SIZE) {
    return {
      file: normalizedFile,
      originalSize: sourceFile.size,
      compressedSize: normalizedFile.size,
      compressed: normalizedFile !== sourceFile,
    }
  }

  const image = await loadImage(normalizedFile)
  let bestCandidate: File | null = null

  for (const maxDimension of DIMENSION_STEPS) {
    const { width, height } = resolveTargetSize(image.width, image.height, maxDimension)
    const canvas = document.createElement('canvas')
    canvas.width = width
    canvas.height = height

    const context = canvas.getContext('2d')
    if (!context) {
      throw new Error('浏览器暂时无法处理图片压缩')
    }

    context.clearRect(0, 0, width, height)
    context.drawImage(image, 0, 0, width, height)

    for (const quality of QUALITY_STEPS) {
      const blob = await canvasToBlob(canvas, 'image/webp', quality)
      const candidate = new File([blob], replaceExtension(normalizedFile.name, 'webp'), {
        type: 'image/webp',
        lastModified: Date.now(),
      })

      if (!bestCandidate || candidate.size < bestCandidate.size) {
        bestCandidate = candidate
      }

      if (candidate.size <= TARGET_SIZE) {
        return {
          file: candidate,
          originalSize: sourceFile.size,
          compressedSize: candidate.size,
          compressed: true,
        }
      }
    }
  }

  if (bestCandidate && bestCandidate.size <= ACCEPTABLE_MAX_SIZE) {
    return {
      file: bestCandidate,
      originalSize: sourceFile.size,
      compressedSize: bestCandidate.size,
      compressed: true,
    }
  }

  throw new Error('压缩后仍超过 500KB，请换一张更小的图片')
}

export const formatFileSize = (bytes: number) => {
  if (bytes < 1024) {
    return `${bytes} B`
  }
  if (bytes < 1024 * 1024) {
    return `${(bytes / 1024).toFixed(1)} KB`
  }
  return `${(bytes / 1024 / 1024).toFixed(2)} MB`
}

const normalizeInputImageFile = async (sourceFile: File) => {
  if (!isHeicLikeFile(sourceFile)) {
    return sourceFile
  }

  const { default: heic2any } = await import('heic2any')
  const converted = await heic2any({
    blob: sourceFile,
    toType: 'image/jpeg',
    quality: 0.92,
  })
  const convertedBlob = Array.isArray(converted) ? converted[0] : converted

  if (!(convertedBlob instanceof Blob)) {
    throw new Error('HEIC 图片转换失败，请换一张图片重试')
  }

  return new File([convertedBlob], replaceExtension(sourceFile.name, 'jpg'), {
    type: 'image/jpeg',
    lastModified: Date.now(),
  })
}

const isSupportedInputFile = (file: File) => {
  const normalizedType = normalizeMimeType(file.type)
  if (DIRECT_UPLOAD_TYPES.has(normalizedType) || HEIC_TYPES.has(normalizedType)) {
    return true
  }

  const extension = extractExtension(file.name)
  return SUPPORTED_EXTENSIONS.has(extension)
}

const isHeicLikeFile = (file: File) => {
  const normalizedType = normalizeMimeType(file.type)
  if (HEIC_TYPES.has(normalizedType)) {
    return true
  }

  const extension = extractExtension(file.name)
  return extension === 'heic' || extension === 'heif'
}

const normalizeMimeType = (type: string) => type.trim().toLowerCase()

const extractExtension = (filename: string) => {
  const trimmed = filename.trim()
  const dotIndex = trimmed.lastIndexOf('.')
  if (dotIndex < 0) {
    return ''
  }
  return trimmed.slice(dotIndex + 1).toLowerCase()
}

const loadImage = (file: File) =>
  new Promise<HTMLImageElement>((resolve, reject) => {
    const objectUrl = URL.createObjectURL(file)
    const image = new Image()

    image.onload = () => {
      URL.revokeObjectURL(objectUrl)
      resolve(image)
    }
    image.onerror = () => {
      URL.revokeObjectURL(objectUrl)
      reject(new Error('图片读取失败'))
    }
    image.src = objectUrl
  })

const resolveTargetSize = (width: number, height: number, maxDimension: number) => {
  const longestEdge = Math.max(width, height)
  if (longestEdge <= maxDimension) {
    return { width, height }
  }

  const ratio = maxDimension / longestEdge
  return {
    width: Math.max(1, Math.round(width * ratio)),
    height: Math.max(1, Math.round(height * ratio)),
  }
}

const canvasToBlob = (canvas: HTMLCanvasElement, type: string, quality: number) =>
  new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        resolve(blob)
        return
      }
      reject(new Error('图片压缩失败'))
    }, type, quality)
  })

const replaceExtension = (filename: string, nextExtension: string) => {
  const normalized = filename.trim() || 'image'
  const dotIndex = normalized.lastIndexOf('.')
  const basename = dotIndex > 0 ? normalized.slice(0, dotIndex) : normalized
  return `${basename}.${nextExtension}`
}
