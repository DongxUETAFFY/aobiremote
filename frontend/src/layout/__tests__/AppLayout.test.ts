import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import AppLayout from '@/layout/AppLayout.vue'

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
  })

  it('shows the version and updated date in the top-right info area', () => {
    const wrapper = mount(AppLayout, {
      global: {
        plugins: [createPinia()],
        stubs: {
          RouterView: { template: '<div class="router-view-stub" />' },
        },
      },
    })

    expect(wrapper.text()).toContain('v1.0.8')
    expect(wrapper.text()).toContain('更新于 2026-04-30')
  })
})
