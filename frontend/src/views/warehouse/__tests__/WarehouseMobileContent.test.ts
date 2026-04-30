import { computed, defineComponent, ref } from 'vue'
import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import userEvent from '@testing-library/user-event'
import ElementPlus from 'element-plus'
import WarehouseMobileContent from '@/views/warehouse/WarehouseMobileContent.vue'
import type { InventoryListItem, InventorySummary } from '@/types/inventory'

const summary: InventorySummary = {
  totalBuyPrice: '100.00',
  obiCount: 1,
  obiBuyPrice: '50.00',
  magicCount: 0,
  magicBuyPrice: '0.00',
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

const mountMobileContent = (items: InventoryListItem[]) =>
  mount(WarehouseMobileContent, {
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
        ElCheckboxGroup: { template: '<div class="checkbox-group-stub"><slot /></div>' },
        ElCheckbox: { template: '<label class="checkbox-stub"><slot /></label>' },
      },
    },
  })

describe('Warehouse mobile content', () => {
  it('does not wrap item cards inside the batch checkbox group in collage mode', () => {
    const wrapper = mountMobileContent([baseItem])

    expect(wrapper.find('.checkbox-group-stub .warehouse-mobile-item').exists()).toBe(false)
  })

  it('checks only the clicked collage checkbox in real mobile interaction', async () => {
    const items: InventoryListItem[] = [
      baseItem,
      {
        ...baseItem,
        id: 2,
        itemName: '第二件物品',
        imageFileId: 'file-2',
      },
      {
        ...baseItem,
        id: 3,
        itemName: '第三件物品',
        imageFileId: 'file-3',
      },
    ]

    const Harness = defineComponent({
      components: { WarehouseMobileContent },
      setup() {
        const selectedIds = ref<number[]>([])
        const collageSelectedIds = ref<number[]>([])
        const collageSelectedCount = computed(() => collageSelectedIds.value.length)

        const toggleCollageItem = (item: InventoryListItem, checked: boolean | string | number) => {
          const next = new Set(collageSelectedIds.value)
          if (checked) {
            next.add(item.id)
          } else {
            next.delete(item.id)
          }
          collageSelectedIds.value = [...next]
        }

        return {
          items,
          summary,
          selectedIds,
          collageSelectedIds,
          collageSelectedCount,
          toggleCollageItem,
        }
      },
      template: `
        <WarehouseMobileContent
          v-model:selected-ids="selectedIds"
          :loading="false"
          :items="items"
          :total-count="items.length"
          :summary="summary"
          :summary-total-count="items.length"
          current-category=""
          sort-type="buyTimeDesc"
          filter-keyword=""
          :current-page="1"
          :page-size="30"
          :selected-selectable-ids="selectedIds"
          :all-selectable-checked="false"
          :has-selectable-items="true"
          :batch-public-loading="false"
          :is-batch-public-selectable="() => true"
          :format-date="(value) => value"
          :channel-label="() => '闲鱼'"
          :category-label="() => '奥比时装'"
          :is-collage-mode="true"
          :collage-selected-ids="collageSelectedIds"
          :collage-selection-limit="20"
          collage-layout-mode="auto"
          :collage-selected-count="collageSelectedCount"
          :can-select-more-for-collage="true"
          :is-collage-selectable="(item) => Boolean(item.imageFileId)"
          :is-collage-selected="(itemId) => collageSelectedIds.includes(itemId)"
          :collage-generating="false"
          @toggle-collage-item="toggleCollageItem"
        />
      `,
    })

    const wrapper = mount(Harness, {
      global: {
        plugins: [ElementPlus],
        stubs: {
          SquareImagePreview: { template: '<div class="square-image-preview-stub" />' },
          PaginationBar: { template: '<div class="pagination-bar-stub" />' },
          PriceSortToggle: { template: '<div class="price-sort-toggle-stub" />' },
        },
      },
    })

    const collageCheckboxes = wrapper.findAll('.warehouse-mobile-item__selection .el-checkbox')
    expect(collageCheckboxes).toHaveLength(6)

    await userEvent.click(collageCheckboxes[1].element as HTMLElement)

    const checkedInputs = wrapper.findAll('.warehouse-mobile-item__selection input[type="checkbox"]')
      .filter((input) => (input.element as HTMLInputElement).checked)

    expect(checkedInputs).toHaveLength(1)
  })
})
