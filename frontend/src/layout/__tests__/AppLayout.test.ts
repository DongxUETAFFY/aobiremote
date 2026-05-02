import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import { APP_RELEASE_NOTICE_STORAGE_KEY } from '@/constants/app-meta'
import AppLayout from '@/layout/AppLayout.vue'

const alertMock = vi.fn()

vi.mock('element-plus', () => ({
  ElMessageBox: {
    alert: (...args: unknown[]) => alertMock(...args),
  },
}))

vi.mock('vue-router', () => ({
  useRoute: () => ({
    path: '/warehouse',
    meta: {
      title: '我的仓库',
    },
  }),
  useRouter: () => ({
    push: vi.fn(),
  }),
}))

describe('AppLayout', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    alertMock.mockReset()
    alertMock.mockResolvedValue(undefined)
    window.localStorage.clear()
    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      writable: true,
      value: 1440,
    })
  })

  const mountLayout = () =>
    mount(AppLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterView: { template: '<div class="router-view-stub" />' },
        },
      },
    })

  it('shows the version and updated date in the top-right info area', () => {
    const wrapper = mountLayout()

    expect(wrapper.text()).toContain('v1.0.9')
    expect(wrapper.text()).toContain('更新于 2026-05-02')
  })

  it('shows the release notice once on desktop when the current version is unseen', async () => {
    mountLayout()
    await Promise.resolve()

    expect(alertMock).toHaveBeenCalledTimes(1)
    expect(window.localStorage.getItem(APP_RELEASE_NOTICE_STORAGE_KEY)).toBe('v1.0.9')
  })

  it('does not show the release notice on mobile or when this version was already seen', async () => {
    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      writable: true,
      value: 640,
    })

    mountLayout()
    await Promise.resolve()

    expect(alertMock).not.toHaveBeenCalled()

    window.localStorage.setItem(APP_RELEASE_NOTICE_STORAGE_KEY, 'v1.0.9')
    Object.defineProperty(window, 'innerWidth', {
      configurable: true,
      writable: true,
      value: 1440,
    })

    mountLayout()
    await Promise.resolve()

    expect(alertMock).not.toHaveBeenCalled()
  })
})
