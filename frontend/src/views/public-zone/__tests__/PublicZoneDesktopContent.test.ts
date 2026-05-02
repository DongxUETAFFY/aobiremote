import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PublicZoneDesktopContent from '@/views/public-zone/PublicZoneDesktopContent.vue'
import type { PublicPostListItem } from '@/types/public-post'

const items: PublicPostListItem[] = []

const mountDesktopContent = (isAuthenticated = true) =>
  mount(PublicZoneDesktopContent, {
    props: {
      loading: false,
      items,
      totalCount: 0,
      currentPage: 1,
      pageSize: 30,
      filterScope: 'all',
      filterDirection: 'all',
      filterChannel: 'all',
      filterCategory: 'all',
      filterKeyword: '',
      filterMinPrice: null,
      filterMaxPrice: null,
      sortType: 'tradeTimeDesc',
      isAuthenticated,
      channelOptions: [{ label: '全部渠道', value: 'all' }],
      categoryOptions: [{ label: '全部分类', value: 'all' }],
      channelLabel: () => '闲鱼',
      categoryLabel: () => '奥比时装',
      formatDate: (value: string) => value,
      directionLabel: (value: 'buy' | 'sell') => (value === 'buy' ? '买入' : '卖出'),
    },
    global: {
      stubs: {
        PaginationBar: { template: '<div class="pagination-bar-stub" />' },
        PriceSortToggle: { template: '<div class="price-sort-toggle-stub" />' },
        SquareImagePreview: { template: '<div class="square-preview-stub" />' },
        ElButton: {
          emits: ['click'],
          template: '<button class="el-button-stub" @click="$emit(\'click\')"><slot /></button>',
        },
        ElInput: { template: '<input />' },
        ElInputNumber: { template: '<input type="number" />' },
        ElRadioGroup: { template: '<div><slot /></div>' },
        ElRadioButton: { template: '<button><slot /></button>' },
      },
    },
  })

describe('PublicZone desktop content', () => {
  it('renders a foreground publish action and emits open-add when clicked', async () => {
    const wrapper = mountDesktopContent(true)
    const publishButton = wrapper.get('.public-zone-header__action')

    await publishButton.trigger('click')

    expect(publishButton.text()).toContain('发布交易')
    expect(wrapper.emitted('open-add')).toHaveLength(1)
  })

  it('hides the publish action for unauthenticated users', () => {
    const wrapper = mountDesktopContent(false)

    expect(wrapper.find('.public-zone-header__action').exists()).toBe(false)
  })
})
