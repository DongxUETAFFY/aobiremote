import { describe, expect, it } from 'vitest'
import { computeCollageLayout, resolveCollageColumns } from '@/utils/warehouse-collage'

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
})
