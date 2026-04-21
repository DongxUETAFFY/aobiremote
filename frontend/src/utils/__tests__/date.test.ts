import { describe, expect, it } from 'vitest'
import { formatLocalDateInputValue } from '@/utils/date'

describe('formatLocalDateInputValue', () => {
  it('formats a date as YYYY-MM-DD using local date parts', () => {
    const date = new Date(2026, 3, 21)

    expect(formatLocalDateInputValue(date)).toBe('2026-04-21')
  })
})
