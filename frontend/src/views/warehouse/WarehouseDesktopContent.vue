<script setup lang="ts">
import type { InventoryCategory, InventoryChannel, InventoryListItem, InventorySummary, SortType } from '@/types/inventory'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import PaginationBar from '@/components/common/PaginationBar.vue'
import PriceSortToggle from '@/components/common/PriceSortToggle.vue'

const props = defineProps<{
  loading: boolean
  items: InventoryListItem[]
  totalCount: number
  summary: InventorySummary
  summaryTotalCount: number
  currentCategory: InventoryCategory | ''
  sortType: SortType
  filterKeyword: string
  currentPage: number
  pageSize: number
  selectedIds: number[]
  selectedSelectableIds: number[]
  allSelectableChecked: boolean
  hasSelectableItems: boolean
  batchPublicLoading: boolean
  isCollageMode: boolean
  collageSelectedIds: number[]
  collageSelectionLimit: number
  collageLayoutMode: 'auto' | '3' | '4' | '5'
  collageSelectedCount: number
  collageGenerating: boolean
  canSelectMoreForCollage: boolean
  isBatchPublicSelectable: (item: InventoryListItem) => boolean
  isCollageSelectable: (item: InventoryListItem) => boolean
  isCollageSelected: (itemId: number) => boolean
  formatDate: (value: string) => string
  channelLabel: (value: InventoryChannel) => string
  categoryLabel: (value: InventoryCategory) => string
}>()

const emit = defineEmits<{
  (e: 'open-add'): void
  (e: 'open-upload-collage'): void
  (e: 'page-change', page: number): void
  (e: 'category-filter', category: InventoryCategory): void
  (e: 'update:filterKeyword', value: string): void
  (e: 'keyword-search'): void
  (e: 'keyword-clear'): void
  (e: 'sort-change', value: SortType | 'default'): void
  (e: 'toggle-select-all', checked: boolean | string | number): void
  (e: 'batch-public'): void
  (e: 'update:selectedIds', value: number[]): void
  (e: 'toggle-collage-item', item: InventoryListItem, checked: boolean | string | number): void
  (e: 'clear-collage-selection'): void
  (e: 'collage-layout-change', value: 'auto' | '3' | '4' | '5'): void
  (e: 'generate-collage'): void
  (e: 'exit-collage-mode'): void
  (e: 'edit', item: InventoryListItem): void
  (e: 'open-sold', item: InventoryListItem): void
  (e: 'public-action', item: InventoryListItem): void
  (e: 'delete', item: InventoryListItem): void
}>()

const toggleItemSelection = (itemId: number, checked: boolean | string | number) => {
  if (checked) {
    emit('update:selectedIds', [...new Set([...props.selectedIds, itemId])])
    return
  }
  emit(
    'update:selectedIds',
    props.selectedIds.filter((id) => id !== itemId),
  )
}

const toggleCollageSelection = (item: InventoryListItem, checked: boolean | string | number) => {
  emit('toggle-collage-item', item, checked)
}

const collageSelectionText = (item: InventoryListItem) => {
  if (!item.imageFileId) {
    return '无图不可拼图'
  }
  if (!props.canSelectMoreForCollage && !props.isCollageSelected(item.id)) {
    return `已达上限 (${props.collageSelectionLimit})`
  }
  return '加入拼图'
}
</script>

<template>
  <div class="warehouse-content">
    <section class="warehouse-summary ah-glass-card ah-page-section">
      <div class="warehouse-summary__info">
        <p class="warehouse-summary__label">当前仓库</p>
        <h2 class="warehouse-summary__count">{{ summaryTotalCount }} 件宝贝</h2>
        <p class="warehouse-summary__total">累计买入 ¥{{ summary.totalBuyPrice }}</p>
        <p class="warehouse-summary__filter">
          当前筛选：{{ currentCategory ? categoryLabel(currentCategory) : '全部分类' }}
          <span v-if="currentCategory">，列表显示 {{ totalCount }} 件</span>
        </p>
      </div>
      <div class="warehouse-summary__actions">
        <el-button plain size="large" @click="emit('open-upload-collage')">
          拼图
        </el-button>
        <el-button type="primary" size="large" @click="emit('open-add')">
          + 新增记录
        </el-button>
      </div>
    </section>

    <section class="warehouse-category-summary ah-glass-card ah-page-section">
      <div
        class="warehouse-category-summary__item"
        :class="{ 'is-active': currentCategory === 'obi' }"
      >
        <p class="warehouse-category-summary__label">奥比总价格</p>
        <button
          type="button"
          class="warehouse-category-summary__price"
          @click="emit('category-filter', 'obi')"
        >
          ¥{{ summary.obiBuyPrice }}
        </button>
        <p class="warehouse-category-summary__count">奥比总件数 {{ summary.obiCount }} 件</p>
      </div>
      <div
        class="warehouse-category-summary__item"
        :class="{ 'is-active': currentCategory === 'magic' }"
      >
        <p class="warehouse-category-summary__label">魔力总价格</p>
        <button
          type="button"
          class="warehouse-category-summary__price"
          @click="emit('category-filter', 'magic')"
        >
          ¥{{ summary.magicBuyPrice }}
        </button>
        <p class="warehouse-category-summary__count">魔力总件数 {{ summary.magicCount }} 件</p>
      </div>
    </section>

    <section class="warehouse-search ah-glass-card ah-page-section">
      <div class="warehouse-search__group">
        <span class="warehouse-search__label">名称</span>
        <el-input
          :model-value="filterKeyword"
          class="warehouse-search__input"
          clearable
          placeholder="按物品名称筛选"
          @update:model-value="emit('update:filterKeyword', $event)"
          @keyup.enter="emit('keyword-search')"
          @clear="emit('keyword-clear')"
        />
        <el-button @click="emit('keyword-search')">搜索</el-button>
      </div>
      <PriceSortToggle
        :model-value="sortType === 'buyTimeDesc' ? 'default' : sortType"
        asc-value="buyPriceAsc"
        desc-value="buyPriceDesc"
        @change="emit('sort-change', $event as SortType | 'default')"
      />
    </section>

    <div v-if="loading" class="warehouse-loading">
      <span class="warehouse-loading__spinner" />
      <p>加载中...</p>
    </div>

    <div v-else-if="items.length === 0" class="warehouse-empty ah-glass-card ah-page-section">
      <p>还没有任何记录</p>
      <p class="warehouse-empty__sub">点击上方「新增记录」添加你的第一件宝贝</p>
    </div>

    <div v-else class="warehouse-list">
      <section class="warehouse-batch ah-glass-card ah-page-section">
        <div class="warehouse-batch__toolbar">
          <div class="warehouse-batch__main">
            <el-checkbox
              :model-value="allSelectableChecked"
              :disabled="!hasSelectableItems"
              @change="emit('toggle-select-all', $event)"
            >
              全选本页可公开物品
            </el-checkbox>
            <span class="warehouse-batch__count">已选 {{ selectedSelectableIds.length }} 件</span>
          </div>
          <el-button
            type="warning"
            :disabled="!selectedSelectableIds.length"
            :loading="batchPublicLoading"
            @click="emit('batch-public')"
          >
            批量公开 {{ selectedSelectableIds.length ? `(${selectedSelectableIds.length})` : '' }}
          </el-button>
        </div>
        <p class="warehouse-batch__hint">仅支持选择未卖出且未公开的物品，避免误触取消公开。</p>
      </section>

      <section v-if="isCollageMode" class="warehouse-collage ah-glass-card ah-page-section">
        <div class="warehouse-collage__toolbar">
          <div class="warehouse-collage__main">
            <span class="warehouse-collage__count">
              已选 {{ collageSelectedCount }} / {{ collageSelectionLimit }} 张
            </span>
            <div class="warehouse-collage__layout">
              <el-button
                size="small"
                :type="collageLayoutMode === 'auto' ? 'primary' : 'default'"
                @click="emit('collage-layout-change', 'auto')"
              >
                自动
              </el-button>
              <el-button
                size="small"
                :type="collageLayoutMode === '3' ? 'primary' : 'default'"
                @click="emit('collage-layout-change', '3')"
              >
                每行3张
              </el-button>
              <el-button
                size="small"
                :type="collageLayoutMode === '4' ? 'primary' : 'default'"
                @click="emit('collage-layout-change', '4')"
              >
                每行4张
              </el-button>
              <el-button
                size="small"
                :type="collageLayoutMode === '5' ? 'primary' : 'default'"
                @click="emit('collage-layout-change', '5')"
              >
                每行5张
              </el-button>
            </div>
          </div>
          <div class="warehouse-collage__actions">
            <el-button size="small" @click="emit('clear-collage-selection')">清空</el-button>
            <el-button
              size="small"
              type="success"
              :disabled="!collageSelectedCount"
              :loading="collageGenerating"
              @click="emit('generate-collage')"
            >
              生成拼图
            </el-button>
            <el-button size="small" type="warning" plain @click="emit('exit-collage-mode')">
              退出拼图模式
            </el-button>
          </div>
        </div>
        <p class="warehouse-collage__hint">
          拼图选择支持跨页、跨搜索和跨排序保留；退出拼图模式后会清空已选内容。
        </p>
      </section>

      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
          @change="emit('page-change', $event)"
        />
      </div>

      <div
        v-for="item in items"
        :key="item.id"
        class="warehouse-item ah-glass-card ah-page-section"
      >
        <div class="warehouse-item__selection" :class="{ 'is-collage-mode': isCollageMode }">
          <el-checkbox
            :model-value="selectedIds.includes(item.id)"
            :disabled="!isBatchPublicSelectable(item)"
            @change="toggleItemSelection(item.id, $event)"
          >
            {{
              isBatchPublicSelectable(item)
                ? '加入批量公开'
                : item.publicPosted
                  ? '已公开，不可批量公开'
                  : '已卖出，不可批量公开'
            }}
          </el-checkbox>
          <el-checkbox
            v-if="isCollageMode"
            :model-value="isCollageSelected(item.id)"
            :disabled="!isCollageSelectable(item) || (!canSelectMoreForCollage && !isCollageSelected(item.id))"
            @change="toggleCollageSelection(item, $event)"
          >
            {{ collageSelectionText(item) }}
          </el-checkbox>
        </div>

        <div class="warehouse-item__thumb">
          <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
        </div>

        <div class="warehouse-item__body">
          <div class="warehouse-item__header">
            <h3 class="warehouse-item__name">{{ item.itemName }}</h3>
            <span class="warehouse-item__status" :class="`is-${item.status}`">
              {{ item.status === 'unsold' ? '未卖出' : '已卖出' }}
            </span>
          </div>

          <div class="warehouse-item__meta">
            <span>买入价 ¥{{ item.buyPrice }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ formatDate(item.buyTime) }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
          </div>

          <p v-if="item.remark" class="warehouse-item__remark">备注：{{ item.remark }}</p>

          <div class="warehouse-item__actions">
            <el-button size="small" @click="emit('edit', item)">编辑</el-button>
            <el-button
              v-if="item.status === 'unsold'"
              size="small"
              type="success"
              @click="emit('open-sold', item)"
            >
              标记卖出
            </el-button>
            <el-button
              size="small"
              :type="item.publicPosted ? 'warning' : 'primary'"
              @click="emit('public-action', item)"
            >
              {{ item.publicPosted ? '取消公开' : '公开' }}
            </el-button>
            <el-button size="small" type="danger" @click="emit('delete', item)">删除</el-button>
          </div>
        </div>
      </div>

      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
          @change="emit('page-change', $event)"
        />
      </div>
    </div>
  </div>
</template>

<style scoped>
.warehouse-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.warehouse-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.warehouse-summary__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-summary__label {
  margin: 0 0 4px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.warehouse-summary__count {
  margin: 0;
  color: var(--ah-title);
  font-size: 28px;
}

.warehouse-summary__total {
  margin: 4px 0 0;
  color: var(--ah-text);
}

.warehouse-summary__filter {
  margin: 6px 0 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-category-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.warehouse-category-summary__item {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 250, 247, 0.78);
  border: 1px solid rgba(216, 168, 183, 0.16);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.warehouse-category-summary__item.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.warehouse-category-summary__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-category-summary__price {
  margin-top: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ah-title);
  font-size: 26px;
  font-weight: 800;
  cursor: pointer;
}

.warehouse-category-summary__price:hover {
  color: #cf5d75;
}

.warehouse-category-summary__count {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 14px;
}

.warehouse-batch {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 16px;
}

.warehouse-batch__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.warehouse-batch__main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-batch__count {
  color: var(--ah-title);
  font-size: 14px;
  font-weight: 700;
}

.warehouse-batch__hint {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-collage {
  display: grid;
  gap: 14px;
}

.warehouse-collage__toolbar {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.warehouse-collage__main {
  display: grid;
  gap: 10px;
}

.warehouse-collage__count {
  color: var(--ah-title);
  font-size: 14px;
  font-weight: 700;
}

.warehouse-collage__layout,
.warehouse-collage__actions {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.warehouse-collage__hint {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-search {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(360px, 760px) max-content;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding-left: 34px;
  background:
    linear-gradient(90deg, rgba(255, 143, 177, 0.16), rgba(255, 252, 247, 0.92) 36%, rgba(255, 255, 255, 0.78));
}

.warehouse-search::before {
  position: absolute;
  left: 14px;
  top: 18px;
  bottom: 18px;
  width: 6px;
  content: '';
  border-radius: 999px;
  background: linear-gradient(180deg, var(--ah-accent), var(--ah-highlight));
  box-shadow: 0 8px 18px rgba(240, 111, 154, 0.26);
}

.warehouse-search__group {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  width: min(100%, 760px);
}

.warehouse-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-search__input {
  flex: 1;
  min-width: 260px;
  max-width: 620px;
}

.warehouse-search :deep(.price-sort-toggle) {
  flex-wrap: nowrap;
  justify-self: end;
}

.warehouse-search :deep(.price-sort-toggle__label),
.warehouse-search :deep(.price-sort-toggle__button) {
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .warehouse-search {
    grid-template-columns: 1fr;
    justify-content: stretch;
  }

  .warehouse-search :deep(.price-sort-toggle) {
    justify-self: start;
  }
}

.warehouse-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.warehouse-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: warehouse-spin 0.8s linear infinite;
}

@keyframes warehouse-spin {
  to {
    transform: rotate(360deg);
  }
}

.warehouse-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.warehouse-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.warehouse-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(460px, 1fr));
  gap: 14px;
}

.warehouse-batch,
.warehouse-pagination {
  grid-column: 1 / -1;
}

.warehouse-pagination {
  padding: 8px 0;
}

.warehouse-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.warehouse-item.ah-page-section {
  padding: 18px;
}

.warehouse-item__selection {
  width: 100%;
}

.warehouse-item__selection.is-collage-mode {
  display: flex;
  align-items: center;
  gap: 16px;
  flex-wrap: wrap;
}

.warehouse-item__thumb {
  flex-shrink: 0;
}

.warehouse-item__thumb :deep(.square-preview) {
  width: 112px;
  height: 112px;
  border-radius: 18px;
}

.warehouse-item__body {
  flex: 1;
  min-width: 0;
}

.warehouse-item__header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 16px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.warehouse-item__status {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.warehouse-item__status.is-unsold {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.warehouse-item__status.is-sold {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.warehouse-item__meta {
  margin-top: 6px;
  color: var(--ah-text);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.warehouse-item__dot {
  color: #c9a0b0;
}

.warehouse-item__remark {
  margin: 6px 0 0;
  color: var(--ah-text);
  font-size: 13px;
}

.warehouse-item__actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
