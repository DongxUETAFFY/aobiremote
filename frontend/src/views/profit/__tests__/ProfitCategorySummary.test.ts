import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import ProfitDesktopContent from '@/views/profit/ProfitDesktopContent.vue'
import ProfitMobileContent from '@/views/profit/ProfitMobileContent.vue'
import type { TradeListItem, TradeSummary } from '@/types/trade'

const summary: TradeSummary = {
  totalBuyAmount: '120.00',
  totalSellAmount: '158.00',
  totalProfit: '38.00',
  totalLoss: '50.00',
  obiCount: 1,
  obiBuyAmount: '80.00',
  magicCount: 1,
  magicBuyAmount: '40.00',
}

const baseItem: TradeListItem = {
  id: 1,
  itemName: '测试物品',
  buyPrice: 50,
  buyTime: '2026-05-02',
  sellPrice: 88,
  sellTime: '2026-05-02',
  profitAmount: 38,
  channel: 'xianyu',
  category: 'obi',
  remark: null,
  imageFileId: 'file-1',
  publicPosted: false,
  createdAt: '2026-05-02T00:00:00',
  updatedAt: '2026-05-02T00:00:00',
}

const baseProps = {
  loading: false,
  items: [baseItem],
  totalCount: 1,
  summary,
  profitDisplay: { profit: 88, loss: 50, net: 38 },
  summaryTotalCount: 1,
  currentScope: 'all' as const,
  currentCategory: '' as const,
  sortType: 'sellTimeDesc' as const,
  filterKeyword: '',
  currentPage: 1,
  pageSize: 30,
  formatDate: (value: string) => value,
  channelLabel: () => '闲鱼',
  categoryLabel: () => '奥比时装',
}

const globalConfig = {
  stubs: {
    SquareImagePreview: { template: '<div class="square-image-preview-stub" />' },
    PaginationBar: { template: '<div class="pagination-bar-stub" />' },
    PriceSortToggle: { template: '<div class="price-sort-toggle-stub" />' },
    ElButton: { template: '<button><slot /></button>' },
    ElInput: { template: '<input />' },
    ElRadioGroup: { template: '<div class="radio-group-stub"><slot /></div>' },
    ElRadioButton: { template: '<label class="radio-button-stub"><slot /></label>' },
  },
}

describe('Profit category summary', () => {
  it('shows a category filter hint on desktop', () => {
    const wrapper = mount(ProfitDesktopContent, {
      props: baseProps,
      global: globalConfig,
    })

    expect(wrapper.find('.profit-category-summary__hint').text()).toBe('点击卡片可筛选')
  })

  it('shows a category filter hint on mobile', () => {
    const wrapper = mount(ProfitMobileContent, {
      props: baseProps,
      global: globalConfig,
    })

    expect(wrapper.find('.profit-mobile-category__hint').text()).toBe('点击卡片可筛选')
  })
})
