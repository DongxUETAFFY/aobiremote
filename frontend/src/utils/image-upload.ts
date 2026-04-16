const SUPPORTED_TYPES = new Set(['image/jpeg', 'image/png', 'image/webp'])
const TARGET_SIZE = 400 * 1024
const ACCEPTABLE_MAX_SIZE = 500 * 1024
const DIMENSION_STEPS = [1600, 1280, 1024, 800]
const QUALITY_STEPS = [0.9, 0.82, 0.74, 0.66, 0.58]

export interface CompressionResult {
  file: File
  originalSize: number
  compressedSize: number
  compressed: boolean
}

export const compressImageBeforeUpload = async (sourceFile: File): Promise<CompressionResult> => {
  if (!SUPPORTED_TYPES.has(sourceFile.type)) {
    throw new Error('仅支持 JPG、PNG、WEBP 图片')
  }

  if (sourceFile.size <= TARGET_SIZE) {
    return {
      file: sourceFile,
      originalSize: sourceFile.size,
      compressedSize: sourceFile.size,
      compressed: false,
    }
  }

  const image = await loadImage(sourceFile)
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
      const candidate = new File([blob], replaceExtension(sourceFile.name, 'webp'), {
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
