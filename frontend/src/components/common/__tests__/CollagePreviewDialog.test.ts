import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import CollagePreviewDialog from '@/components/common/CollagePreviewDialog.vue'

describe('CollagePreviewDialog', () => {
  it('shows a desktop download action', () => {
    const wrapper = mount(CollagePreviewDialog, {
      props: {
        visible: true,
        imageUrl: 'blob:test',
        layoutMode: 'auto',
        selectedCount: 6,
        mobile: false,
        generating: false,
      },
      global: {
        stubs: {
          ElDialog: {
            props: ['modelValue'],
            template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
          },
          ElButton: {
            template: '<button><slot /></button>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('下载图片')
    expect(wrapper.text()).not.toContain('长按图片可保存')
  })

  it('shows mobile save guidance', () => {
    const wrapper = mount(CollagePreviewDialog, {
      props: {
        visible: true,
        imageUrl: 'blob:test',
        layoutMode: '5',
        selectedCount: 12,
        mobile: true,
        generating: false,
      },
      global: {
        stubs: {
          ElDialog: {
            props: ['modelValue'],
            template: '<div v-if="modelValue"><slot /><slot name="footer" /></div>',
          },
          ElButton: {
            template: '<button><slot /></button>',
          },
        },
      },
    })

    expect(wrapper.text()).toContain('保存图片')
    expect(wrapper.text()).toContain('长按图片可保存')
  })
})
