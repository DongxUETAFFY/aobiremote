import { afterEach, describe, expect, it, vi } from 'vitest'
import {
  computeCollageLayout,
  renderCollageImage,
  resolveCollageColumns,
} from '@/utils/warehouse-collage'

describe('warehouse-collage layout helpers', () => {
  it('uses a 2 by 2 layout for four items in auto mode', () => {
    const columns = resolveCollageColumns(4, 'auto')
    const layout = computeCollageLayout({ itemCount: 4, mode: 'auto' })

    expect(columns).toBe(2)
    expect(layout.columns).toBe(2)
    expect(layout.rows).toBe(2)
  })

  it('uses a 4 by 4 layout for sixteen items in auto mode', () => {
    const columns = resolveCollageColumns(16, 'auto')
    const layout = computeCollageLayout({ itemCount: 16, mode: 'auto' })

    expect(columns).toBe(4)
    expect(layout.columns).toBe(4)
    expect(layout.rows).toBe(4)
  })

  it('uses five columns for seventeen items in auto mode', () => {
    const columns = resolveCollageColumns(17, 'auto')
    const layout = computeCollageLayout({ itemCount: 17, mode: 'auto' })

    expect(columns).toBe(5)
    expect(layout.columns).toBe(5)
    expect(layout.rows).toBe(4)
  })

  it('preserves a manual three-column layout', () => {
    const columns = resolveCollageColumns(10, '3')
    const layout = computeCollageLayout({ itemCount: 10, mode: '3' })

    expect(columns).toBe(3)
    expect(layout.columns).toBe(3)
    expect(layout.rows).toBe(4)
  })

  it('preserves a manual four-column layout', () => {
    const columns = resolveCollageColumns(10, '4')
    const layout = computeCollageLayout({ itemCount: 10, mode: '4' })

    expect(columns).toBe(4)
    expect(layout.columns).toBe(4)
    expect(layout.rows).toBe(3)
  })

  it('preserves a manual five-column layout', () => {
    const columns = resolveCollageColumns(10, '5')
    const layout = computeCollageLayout({ itemCount: 10, mode: '5' })

    expect(columns).toBe(5)
    expect(layout.columns).toBe(5)
    expect(layout.rows).toBe(2)
  })

  it('caps the output width and computes positive canvas metrics', () => {
    const layout = computeCollageLayout({ itemCount: 20, mode: '5' })

    expect(layout.cellSize).toBeGreaterThan(0)
    expect(layout.canvasWidth).toBeLessThanOrEqual(1250)
    expect(layout.canvasHeight).toBeGreaterThan(layout.canvasWidth / 2)
    expect(layout.padding).toBeGreaterThan(0)
    expect(layout.gap).toBeGreaterThan(0)
  })

  it('supports ten columns for upload-collage manual mode', () => {
    const columns = resolveCollageColumns(20, '10', {
      maxColumns: 10,
    })
    const layout = computeCollageLayout({
      itemCount: 20,
      mode: '10',
      maxColumns: 10,
    })

    expect(columns).toBe(10)
    expect(layout.columns).toBe(10)
    expect(layout.rows).toBe(2)
  })

  it('supports a fixed five-column auto strategy for upload-collage mode', () => {
    const columns = resolveCollageColumns(4, 'auto', {
      autoColumns: 5,
      maxColumns: 10,
    })
    const layout = computeCollageLayout({
      itemCount: 4,
      mode: 'auto',
      autoColumns: 5,
      maxColumns: 10,
    })

    expect(columns).toBe(5)
    expect(layout.columns).toBe(5)
    expect(layout.rows).toBe(1)
  })

  it('supports a portrait-style 4:3 crop for long collage mode', () => {
    const layout = computeCollageLayout({
      itemCount: 6,
      mode: '3',
      maxColumns: 10,
      cropMode: 'portrait43',
    })

    expect(layout.cellHeight).toBeGreaterThan(layout.cellWidth)
    expect(layout.cellHeight / layout.cellWidth).toBeCloseTo(4 / 3, 1)
  })

  it('supports custom compact gap and padding values for upload collage mode', () => {
    const layout = computeCollageLayout({
      itemCount: 6,
      mode: '3',
      maxColumns: 10,
      cropMode: 'square',
      gap: 4,
      padding: 12,
    })

    expect(layout.gap).toBe(4)
    expect(layout.padding).toBe(12)
  })

  it('clips every rendered image to its own cell', async () => {
    const drawImage = vi.fn()
    const save = vi.fn()
    const beginPath = vi.fn()
    const rect = vi.fn()
    const clip = vi.fn()
    const restore = vi.fn()
    const fillRect = vi.fn()

    const context = {
      drawImage,
      save,
      beginPath,
      rect,
      clip,
      restore,
      fillRect,
      fillStyle: '#ffffff',
    } as unknown as CanvasRenderingContext2D

    const canvas = {
      width: 0,
      height: 0,
      getContext: vi.fn(() => context),
      toBlob: (callback: BlobCallback) => callback(new Blob(['preview'], { type: 'image/png' })),
    } as unknown as HTMLCanvasElement

    const originalCreateElement = document.createElement.bind(document)
    const createElementMock = vi
      .spyOn(document, 'createElement')
      .mockImplementation(((tagName: string) =>
        tagName === 'canvas' ? canvas : originalCreateElement(tagName)) as typeof document.createElement)

    class MockImage {
      onload: null | (() => void) = null
      onerror: null | (() => void) = null
      naturalWidth = 320
      naturalHeight = 640

      set src(_value: string) {
        this.onload?.()
      }
    }

    const originalImage = globalThis.Image
    const createObjectURLMock = vi.fn(() => 'blob:collage-preview')
    const revokeObjectURLMock = vi.fn()

    vi.stubGlobal('Image', MockImage)
    vi.stubGlobal('URL', {
      createObjectURL: createObjectURLMock,
      revokeObjectURL: revokeObjectURLMock,
    })

    try {
      const result = await renderCollageImage({
        items: [{ imageUrl: 'blob:1' }, { imageUrl: 'blob:2' }],
        mode: '2',
        cropMode: 'portrait43',
        maxColumns: 10,
      })

      expect(result.layout.cellHeight).toBeGreaterThan(result.layout.cellWidth)
      expect(save).toHaveBeenCalledTimes(2)
      expect(rect).toHaveBeenNthCalledWith(
        1,
        result.layout.padding,
        result.layout.padding,
        result.layout.cellWidth,
        result.layout.cellHeight,
      )
      expect(rect).toHaveBeenNthCalledWith(
        2,
        result.layout.padding + result.layout.cellWidth + result.layout.gap,
        result.layout.padding,
        result.layout.cellWidth,
        result.layout.cellHeight,
      )
      expect(clip).toHaveBeenCalledTimes(2)
      expect(drawImage).toHaveBeenCalledTimes(2)
      expect(restore).toHaveBeenCalledTimes(2)
    } finally {
      createElementMock.mockRestore()
      vi.unstubAllGlobals()
      globalThis.Image = originalImage
    }
  })
})

afterEach(() => {
  vi.restoreAllMocks()
})
