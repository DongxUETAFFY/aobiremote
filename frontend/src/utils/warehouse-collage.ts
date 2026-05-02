export type CollageLayoutMode = 'auto' | `${number}`
export type CollageCropMode = 'square' | 'portrait43'

export interface CollageLayoutOptions {
  itemCount: number
  mode: CollageLayoutMode
  maxColumns?: number
  autoColumns?: number
  cropMode?: CollageCropMode
  gap?: number
  padding?: number
}

export interface CollageLayoutMetrics {
  columns: number
  rows: number
  cellSize: number
  cellWidth: number
  cellHeight: number
  gap: number
  padding: number
  canvasWidth: number
  canvasHeight: number
  cropMode: CollageCropMode
}

export interface CollageRenderSource {
  imageUrl: string
}

export interface CollageRenderResult {
  blob: Blob
  objectUrl: string
  layout: CollageLayoutMetrics
}

const DEFAULT_MAX_COLUMNS = 5
const DEFAULT_CROP_MODE: CollageCropMode = 'square'
const DEFAULT_PADDING = 24
const DEFAULT_GAP = 16
const PORTRAIT_HEIGHT_RATIO = 4 / 3

const clampItemCount = (itemCount: number) => Math.max(1, Math.floor(itemCount))
const clampColumnCount = (columns: number, maxColumns: number) =>
  Math.min(maxColumns, Math.max(1, Math.floor(columns)))

const getTargetCanvasWidth = (columns: number) => {
  switch (columns) {
    case 1:
      return 720
    case 2:
      return 880
    case 3:
      return 1080
    case 4:
      return 1200
    case 5:
      return 1250
    default:
      return Math.min(1600, 1250 + (columns - 5) * 70)
  }
}

const resolveCropMode = (cropMode?: CollageCropMode) => cropMode ?? DEFAULT_CROP_MODE
const resolveGap = (gap?: number) => Math.max(0, Math.floor(gap ?? DEFAULT_GAP))
const resolvePadding = (padding?: number) => Math.max(0, Math.floor(padding ?? DEFAULT_PADDING))

export const resolveCollageColumns = (
  itemCount: number,
  mode: CollageLayoutMode,
  options: Pick<CollageLayoutOptions, 'maxColumns' | 'autoColumns'> = {},
): number => {
  const normalizedCount = clampItemCount(itemCount)
  const maxColumns = clampColumnCount(options.maxColumns ?? DEFAULT_MAX_COLUMNS, 10)

  if (mode !== 'auto') {
    return clampColumnCount(Number(mode), maxColumns)
  }

  if (options.autoColumns) {
    return clampColumnCount(options.autoColumns, maxColumns)
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
  maxColumns,
  autoColumns,
  cropMode,
  gap,
  padding,
}: CollageLayoutOptions): CollageLayoutMetrics => {
  const normalizedCount = clampItemCount(itemCount)
  const columns = resolveCollageColumns(normalizedCount, mode, {
    maxColumns,
    autoColumns,
  })
  const rows = Math.ceil(normalizedCount / columns)
  const canvasWidthTarget = getTargetCanvasWidth(columns)
  const resolvedCropMode = resolveCropMode(cropMode)
  const resolvedGap = resolveGap(gap)
  const resolvedPadding = resolvePadding(padding)
  const cellWidth = Math.floor((canvasWidthTarget - resolvedPadding * 2 - resolvedGap * (columns - 1)) / columns)
  const cellHeight =
    resolvedCropMode === 'square' ? cellWidth : Math.floor(cellWidth * PORTRAIT_HEIGHT_RATIO)
  const canvasWidth = resolvedPadding * 2 + cellWidth * columns + resolvedGap * (columns - 1)
  const canvasHeight = resolvedPadding * 2 + cellHeight * rows + resolvedGap * (rows - 1)

  return {
    columns,
    rows,
    cellSize: cellWidth,
    cellWidth,
    cellHeight,
    gap: resolvedGap,
    padding: resolvedPadding,
    canvasWidth,
    canvasHeight,
    cropMode: resolvedCropMode,
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

  context.save()
  context.beginPath()
  context.rect(x, y, width, height)
  context.clip()
  context.drawImage(image, drawX, drawY, drawWidth, drawHeight)
  context.restore()
}

export const renderCollageImage = async ({
  items,
  mode,
  maxColumns,
  autoColumns,
  cropMode,
  gap,
  padding,
}: {
  items: CollageRenderSource[]
  mode: CollageLayoutMode
  maxColumns?: number
  autoColumns?: number
  cropMode?: CollageCropMode
  gap?: number
  padding?: number
}): Promise<CollageRenderResult> => {
  const layout = computeCollageLayout({
    itemCount: items.length,
    mode,
    maxColumns,
    autoColumns,
    cropMode,
    gap,
    padding,
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
    const x = layout.padding + columnIndex * (layout.cellWidth + layout.gap)
    const y = layout.padding + rowIndex * (layout.cellHeight + layout.gap)
    drawImageCover(context, image, layout.cellWidth, layout.cellHeight, x, y)
  })

  const blob = await exportCanvasBlob(canvas)
  return {
    blob,
    objectUrl: URL.createObjectURL(blob),
    layout,
  }
}
