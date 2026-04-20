import { mount } from '@vue/test-utils'
import { describe, expect, it, vi } from 'vitest'
import type { AdminPublicPost } from '@/types/admin'
import AdminPublicPostContent from '../AdminPublicPostContent.vue'

vi.mock('@/api/file', () => ({
  buildImagePreviewUrl: (fileId?: string | null) => (fileId ? `/api/files/${fileId}/preview` : ''),
}))

const post: AdminPublicPost = {
  id: 12,
  userId: 34,
  userEmail: 'moderation@example.com',
  itemName: '星梦礼服',
  price: 88.5,
  tradeTime: '2026-04-20T12:30:00',
  direction: 'sell',
  channel: 'xianyu',
  category: 'obi',
  imageFileId: 'image-file-1',
  untrustedCount: 3,
  createdAt: '2026-04-20T13:00:00',
}

describe('AdminPublicPostContent', () => {
  it('renders public-post cards with preview metadata and delete action', async () => {
    const wrapper = mount(AdminPublicPostContent, {
      props: {
        loading: false,
        items: [post],
        formatDateTime: (value: string | null) => value ?? '-',
        channelLabel: () => '闲鱼',
        categoryLabel: () => '奥比岛',
        directionLabel: () => '出',
      },
      global: {
        stubs: {
          SquareImagePreview: {
            props: ['previewUrl', 'emptyText'],
            template: '<div class="square-image-preview-stub">{{ previewUrl }}</div>',
          },
          'el-button': {
            template: '<button type="button" class="el-button-stub" @click="$emit(\'click\')"><slot /></button>',
          },
        },
      },
    })

    expect(wrapper.find('.admin-post-list').exists()).toBe(true)
    expect(wrapper.find('.admin-post-card').exists()).toBe(true)
    expect(wrapper.text()).toContain('星梦礼服')
    expect(wrapper.text()).toContain('moderation@example.com')
    expect(wrapper.text()).toContain('88.5')
    expect(wrapper.text()).toContain('闲鱼')
    expect(wrapper.text()).toContain('奥比岛')
    expect(wrapper.text()).toContain('出')
    expect(wrapper.text()).toContain('3')
    expect(wrapper.find('.square-image-preview-stub').text()).toContain('/api/files/image-file-1/preview')

    await wrapper.get('.admin-post-card__delete').trigger('click')

    expect(wrapper.emitted('delete')?.[0]).toEqual([post])
  })
})
