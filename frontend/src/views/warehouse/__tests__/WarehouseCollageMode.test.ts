import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import WarehouseDesktopContent from '@/views/warehouse/WarehouseDesktopContent.vue'
import type { InventoryListItem, InventorySummary } from '@/types/inventory'

const summary: InventorySummary = {
  totalBuyPrice: '100.00',
  obiCount: 1,
  obiBuyPrice: '50.00',
  magicCount: 1,
  magicBuyPrice: '50.00',
}

const baseItem: InventoryListItem = {
  id: 1,
  itemName: '测试物品',
  buyPrice: 50,
  buyTime: '2026-04-30',
  channel: 'xianyu',
  category: 'obi',
  remark: null,
  imageFileId: 'file-1',
  status: 'unsold',
  publicPosted: false,
  createdAt: '2026-04-30T00:00:00',
  updatedAt: '2026-04-30T00:00:00',
}

const mountDesktopContent = (items: InventoryListItem[]) =>
  mount(WarehouseDesktopContent, {
    props: {
      loading: false,
      items,
      totalCount: items.length,
      summary,
      summaryTotalCount: items.length,
      currentCategory: '',
      sortType: 'buyTimeDesc',
      filterKeyword: '',
      currentPage: 1,
      pageSize: 30,
      selectedIds: [],
      selectedSelectableIds: [],
      allSelectableChecked: false,
      hasSelectableItems: true,
      batchPublicLoading: false,
      isBatchPublicSelectable: () => true,
      formatDate: (value: string) => value,
      channelLabel: () => '闲鱼',
      categoryLabel: () => '奥比时装',
      isCollageMode: true,
      collageSelectedIds: [],
      collageSelectionLimit: 20,
      collageLayoutMode: 'auto',
      collageSelectedCount: 0,
      canSelectMoreForCollage: true,
      isCollageSelectable: (item: InventoryListItem) => Boolean(item.imageFileId),
      isCollageSelected: () => false,
      collageGenerating: false,
    },
    global: {
      stubs: {
        SquareImagePreview: { template: '<div class="square-image-preview-stub" />' },
        PaginationBar: { template: '<div class="pagination-bar-stub" />' },
        PriceSortToggle: { template: '<div class="price-sort-toggle-stub" />' },
        ElButton: { template: '<button><slot /></button>' },
        ElInput: { template: '<input />' },
        ElCheckbox: {
          template: '<label><input type="checkbox" /><slot /></label>',
        },
      },
    },
  })

describe('Warehouse collage mode', () => {
  it('renders collage selection controls when collage mode is enabled', () => {
    const wrapper = mountDesktopContent([baseItem])

    expect(wrapper.text()).toContain('加入拼图')
    expect(wrapper.text()).toContain('生成拼图')
  })

  it('shows a category filter hint below the price cards', () => {
    const wrapper = mountDesktopContent([baseItem])

    expect(wrapper.text()).toContain('点击卡片可筛选')
    expect(wrapper.find('.warehouse-category-summary__hint').exists()).toBe(true)
  })

  it('shows non-selectable collage text for items without images', () => {
    const wrapper = mountDesktopContent([
      {
        ...baseItem,
        id: 2,
        imageFileId: null,
      },
    ])

    expect(wrapper.text()).toContain('无图不可拼图')
  })
})
