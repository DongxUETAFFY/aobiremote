export type CollageLayoutMode = 'auto' | '3' | '4' | '5'

export interface CollageLayoutOptions {
  itemCount: number
  mode: CollageLayoutMode
}

export interface CollageLayoutMetrics {
  columns: number
  rows: number
  cellSize: number
  gap: number
  padding: number
  canvasWidth: number
  canvasHeight: number
}

export interface CollageRenderSource {
  imageUrl: string
}

export interface CollageRenderResult {
  blob: Blob
  objectUrl: string
  layout: CollageLayoutMetrics
}

const MAX_COLUMNS = 5
const PADDING = 24
const GAP = 16

const clampItemCount = (itemCount: number) => Math.max(1, Math.floor(itemCount))

const getTargetCanvasWidth = (columns: number) => {
  switch (columns) {
    case 5:
      return 1250
    case 4:
      return 1200
    case 3:
      return 1080
    case 2:
      return 880
    default:
      return 720
  }
}

export const resolveCollageColumns = (itemCount: number, mode: CollageLayoutMode): number => {
  const normalizedCount = clampItemCount(itemCount)

  if (mode !== 'auto') {
    return Math.min(MAX_COLUMNS, Math.max(1, Number(mode)))
  }

  if (normalizedCount <= 3) {
    return normalizedCount
  }
  if (normalizedCount === 4) {
    return 2
  }
  if (normalizedCount <= 9) {
    return 3
  }
  if (normalizedCount <= 16) {
    return 4
  }
  return 5
}

export const computeCollageLayout = ({
  itemCount,
  mode,
}: CollageLayoutOptions): CollageLayoutMetrics => {
  const normalizedCount = clampItemCount(itemCount)
  const columns = resolveCollageColumns(normalizedCount, mode)
  const rows = Math.ceil(normalizedCount / columns)
  const canvasWidthTarget = getTargetCanvasWidth(columns)
  const cellSize = Math.floor((canvasWidthTarget - PADDING * 2 - GAP * (columns - 1)) / columns)
  const canvasWidth = PADDING * 2 + cellSize * columns + GAP * (columns - 1)
  const canvasHeight = PADDING * 2 + cellSize * rows + GAP * (rows - 1)

  return {
    columns,
    rows,
    cellSize,
    gap: GAP,
    padding: PADDING,
    canvasWidth,
    canvasHeight,
  }
}

const loadImageElement = (imageUrl: string) =>
  new Promise<HTMLImageElement>((resolve, reject) => {
    const image = new Image()
    image.decoding = 'async'
    image.onload = () => resolve(image)
    image.onerror = () => reject(new Error(`Failed to load image: ${imageUrl}`))
    image.src = imageUrl
  })

const exportCanvasBlob = (canvas: HTMLCanvasElement) =>
  new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (!blob) {
        reject(new Error('Failed to export collage image'))
        return
      }
      resolve(blob)
    }, 'image/png')
  })

const drawImageCover = (
  context: CanvasRenderingContext2D,
  image: CanvasImageSource,
  width: number,
  height: number,
  x: number,
  y: number,
) => {
  const sourceWidth = 'naturalWidth' in image ? image.naturalWidth : width
  const sourceHeight = 'naturalHeight' in image ? image.naturalHeight : height
  const scale = Math.max(width / sourceWidth, height / sourceHeight)
  const drawWidth = sourceWidth * scale
  const drawHeight = sourceHeight * scale
  const drawX = x + (width - drawWidth) / 2
  const drawY = y + (height - drawHeight) / 2

  context.drawImage(image, drawX, drawY, drawWidth, drawHeight)
}

export const renderCollageImage = async ({
  items,
  mode,
}: {
  items: CollageRenderSource[]
  mode: CollageLayoutMode
}): Promise<CollageRenderResult> => {
  const layout = computeCollageLayout({
    itemCount: items.length,
    mode,
  })
  const images = await Promise.all(items.map((item) => loadImageElement(item.imageUrl)))
  const canvas = document.createElement('canvas')
  canvas.width = layout.canvasWidth
  canvas.height = layout.canvasHeight

  const context = canvas.getContext('2d')
  if (!context) {
    throw new Error('Canvas context is unavailable')
  }

  context.fillStyle = '#ffffff'
  context.fillRect(0, 0, canvas.width, canvas.height)

  images.forEach((image, index) => {
    const columnIndex = index % layout.columns
    const rowIndex = Math.floor(index / layout.columns)
    const x = layout.padding + columnIndex * (layout.cellSize + layout.gap)
    const y = layout.padding + rowIndex * (layout.cellSize + layout.gap)
    drawImageCover(context, image, layout.cellSize, layout.cellSize, x, y)
  })

  const blob = await exportCanvasBlob(canvas)
  return {
    blob,
    objectUrl: URL.createObjectURL(blob),
    layout,
  }
}
