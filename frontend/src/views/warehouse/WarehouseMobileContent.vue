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
  isBatchPublicSelectable: (item: InventoryListItem) => boolean
  formatDate: (value: string) => string
  channelLabel: (value: InventoryChannel) => string
  categoryLabel: (value: InventoryCategory) => string
}>()

const emit = defineEmits<{
  (e: 'open-add'): void
  (e: 'page-change', page: number): void
  (e: 'category-filter', category: InventoryCategory): void
  (e: 'update:filterKeyword', value: string): void
  (e: 'keyword-search'): void
  (e: 'keyword-clear'): void
  (e: 'sort-change', value: SortType | 'default'): void
  (e: 'toggle-select-all', checked: boolean | string | number): void
  (e: 'batch-public'): void
  (e: 'update:selectedIds', value: number[]): void
  (e: 'edit', item: InventoryListItem): void
  (e: 'open-sold', item: InventoryListItem): void
  (e: 'public-action', item: InventoryListItem): void
  (e: 'delete', item: InventoryListItem): void
}>()
</script>

<template>
  <div class="warehouse-content warehouse-content--mobile">
    <section class="warehouse-mobile-hero ah-glass-card ah-page-section">
      <p class="warehouse-mobile-hero__label">当前仓库</p>
      <h2 class="warehouse-mobile-hero__count">{{ summaryTotalCount }} 件宝贝</h2>
      <p class="warehouse-mobile-hero__total">累计买入 ¥{{ summary.totalBuyPrice }}</p>
      <p class="warehouse-mobile-hero__filter">
        当前筛选：{{ currentCategory ? categoryLabel(currentCategory) : '全部分类' }}
      </p>
    </section>

    <section class="warehouse-mobile-category ah-glass-card ah-page-section">
      <button
        type="button"
        class="warehouse-mobile-category__card"
        :class="{ 'is-active': currentCategory === 'obi' }"
        @click="emit('category-filter', 'obi')"
      >
        <span class="warehouse-mobile-category__label">奥比总价格</span>
        <strong class="warehouse-mobile-category__price">¥{{ summary.obiBuyPrice }}</strong>
        <span class="warehouse-mobile-category__count">奥比总件数 {{ summary.obiCount }} 件</span>
      </button>
      <button
        type="button"
        class="warehouse-mobile-category__card"
        :class="{ 'is-active': currentCategory === 'magic' }"
        @click="emit('category-filter', 'magic')"
      >
        <span class="warehouse-mobile-category__label">魔力总价格</span>
        <strong class="warehouse-mobile-category__price">¥{{ summary.magicBuyPrice }}</strong>
        <span class="warehouse-mobile-category__count">魔力总件数 {{ summary.magicCount }} 件</span>
      </button>
    </section>

    <section class="warehouse-mobile-search ah-glass-card ah-page-section">
      <span class="warehouse-mobile-search__label">名称</span>
      <el-input
        :model-value="filterKeyword"
        clearable
        placeholder="按物品名称筛选"
        @update:model-value="emit('update:filterKeyword', $event)"
        @keyup.enter="emit('keyword-search')"
        @clear="emit('keyword-clear')"
      />
      <el-button class="warehouse-mobile-search__button" @click="emit('keyword-search')">搜索</el-button>
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
      <section class="warehouse-mobile-batch ah-glass-card ah-page-section">
        <div class="warehouse-mobile-batch__toolbar">
          <el-checkbox
            :model-value="allSelectableChecked"
            :disabled="!hasSelectableItems"
            @change="emit('toggle-select-all', $event)"
          >
            全选本页可公开物品
          </el-checkbox>
          <el-button
            class="warehouse-mobile-batch__button"
            type="warning"
            :disabled="!selectedSelectableIds.length"
            :loading="batchPublicLoading"
            @click="emit('batch-public')"
          >
            批量公开
          </el-button>
        </div>
        <p class="warehouse-mobile-batch__count">已选 {{ selectedSelectableIds.length }} 件</p>
        <p class="warehouse-mobile-batch__hint">仅支持选择未卖出且未公开的物品，避免误触取消公开。</p>
      </section>

      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
          hide-status
          @change="emit('page-change', $event)"
        />
      </div>

      <el-checkbox-group
        :model-value="selectedIds"
        class="warehouse-list__group"
        @update:model-value="emit('update:selectedIds', $event)"
      >
        <article
          v-for="item in items"
          :key="item.id"
          class="warehouse-mobile-item ah-glass-card ah-page-section"
        >
          <div class="warehouse-mobile-item__selection">
            <el-checkbox :label="item.id" :disabled="!isBatchPublicSelectable(item)">
              {{
                isBatchPublicSelectable(item)
                  ? '加入批量公开'
                  : item.publicPosted
                    ? '已公开，不可批量公开'
                    : '已卖出，不可批量公开'
              }}
            </el-checkbox>
          </div>

          <div class="warehouse-mobile-item__top">
            <div class="warehouse-mobile-item__thumb">
              <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" enable-mobile-long-press-save />
            </div>
            <div class="warehouse-mobile-item__body">
              <div class="warehouse-mobile-item__title-wrap">
                <h3 class="warehouse-mobile-item__name">{{ item.itemName }}</h3>
                <span class="warehouse-mobile-item__price">¥{{ item.buyPrice }}</span>
              </div>
              <div class="warehouse-mobile-item__meta-grid">
                <p class="warehouse-mobile-item__meta">买入时间: {{ formatDate(item.buyTime) }}</p>
                <p class="warehouse-mobile-item__meta">渠道: {{ channelLabel(item.channel) }}</p>
                <p class="warehouse-mobile-item__meta">分类: {{ categoryLabel(item.category) }}</p>
              </div>
            </div>
          </div>

          <p v-if="item.remark" class="warehouse-mobile-item__remark">备注：{{ item.remark }}</p>

          <div class="warehouse-mobile-item__actions">
            <el-button @click="emit('edit', item)">编辑</el-button>
            <el-button
              v-if="item.status === 'unsold'"
              type="success"
              @click="emit('open-sold', item)"
            >
              标记卖出
            </el-button>
            <el-button
              :type="item.publicPosted ? 'warning' : 'primary'"
              @click="emit('public-action', item)"
            >
              {{ item.publicPosted ? '取消公开' : '公开' }}
            </el-button>
            <el-button type="danger" @click="emit('delete', item)">删除</el-button>
          </div>
        </article>
      </el-checkbox-group>

      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
          hide-status
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

.warehouse-mobile-hero__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.warehouse-mobile-hero__count {
  margin: 8px 0 0;
  color: var(--ah-title);
  font-size: 30px;
  line-height: 1.05;
}

.warehouse-mobile-hero__total,
.warehouse-mobile-hero__filter {
  margin: 8px 0 0;
  color: var(--ah-text);
}

.warehouse-mobile-hero__filter {
  font-size: 13px;
  color: #8d7080;
}

.warehouse-mobile-category {
  display: grid;
  gap: 12px;
}

.warehouse-mobile-category__card {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 6px;
  width: 100%;
  padding: 16px;
  border: 1px solid rgba(216, 168, 183, 0.16);
  border-radius: 18px;
  background: rgba(255, 250, 247, 0.78);
  text-align: left;
  cursor: pointer;
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.warehouse-mobile-category__card.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.warehouse-mobile-category__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-mobile-category__price {
  color: var(--ah-title);
  font-size: 24px;
}

.warehouse-mobile-category__count {
  color: var(--ah-text);
  font-size: 14px;
}

.warehouse-mobile-search {
  display: grid;
  gap: 12px;
}

.warehouse-mobile-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px 24px;
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
  padding: 44px 24px;
  color: var(--ah-title);
}

.warehouse-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.warehouse-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.warehouse-list__group {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.warehouse-mobile-batch {
  display: grid;
  gap: 12px;
}

.warehouse-mobile-batch__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 8px;
}

.warehouse-mobile-batch__count {
  margin: 0;
  color: var(--ah-title);
  font-size: 14px;
  font-weight: 700;
}

.warehouse-mobile-batch__hint {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-pagination {
  padding: 8px 0;
}

.warehouse-mobile-item {
  display: grid;
  gap: 14px;
}

.warehouse-mobile-item__selection {
  width: 100%;
}

.warehouse-mobile-item__top {
  display: grid;
  gap: 12px;
}

.warehouse-mobile-item__body {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.warehouse-mobile-item__title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.warehouse-mobile-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
  line-height: 1.3;
  flex: 1;
  min-width: 0;
  overflow-wrap: anywhere;
}

.warehouse-mobile-item__thumb {
  display: flex;
  justify-content: center;
}

.warehouse-mobile-item__price {
  padding: 4px 10px;
  border-radius: 999px;
  background: rgba(110, 200, 140, 0.16);
  color: #3d5d36;
  font-size: 12px;
  font-weight: 800;
  white-space: nowrap;
}

.warehouse-mobile-item__meta-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.warehouse-mobile-item__meta {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.35;
}

.warehouse-mobile-item__remark {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.6;
}

.warehouse-mobile-item__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.warehouse-mobile-item__actions :deep(.el-button) {
  margin: 0;
}

@media (max-width: 768px) {
  .warehouse-mobile-hero,
  .warehouse-mobile-category,
  .warehouse-mobile-search,
  .warehouse-mobile-batch,
  .warehouse-pagination,
  .warehouse-mobile-item {
    border-color: rgba(146, 174, 118, 0.25);
    background:
      linear-gradient(135deg, rgba(255, 254, 244, 0.95) 0%, rgba(250, 244, 226, 0.9) 100%);
    box-shadow: 0 3px 10px rgba(116, 142, 94, 0.1);
  }

  .warehouse-content {
    gap: 5px;
  }

  .warehouse-mobile-hero__label,
  .warehouse-mobile-category__label,
  .warehouse-mobile-search__label {
    font-size: 11px;
  }

  .warehouse-mobile-hero__count {
    margin-top: 2px;
    font-size: 16px;
  }

  .warehouse-mobile-hero__total,
  .warehouse-mobile-hero__filter,
  .warehouse-mobile-category__count,
  .warehouse-mobile-batch__hint,
  .warehouse-mobile-item__remark {
    font-size: 11px;
  }

  .warehouse-mobile-hero__total,
  .warehouse-mobile-hero__filter {
    margin-top: 1px;
  }

  .warehouse-mobile-category {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 5px;
  }

  .warehouse-mobile-category__card {
    gap: 1px;
    padding: 5px 7px;
    border-radius: 9px;
    border-color: rgba(146, 174, 118, 0.2);
    background: rgba(255, 253, 245, 0.82);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
  }

  .warehouse-mobile-category__card.is-active {
    border-color: rgba(100, 151, 86, 0.42);
    background: linear-gradient(135deg, rgba(232, 244, 215, 0.94), rgba(255, 247, 224, 0.92));
    box-shadow: 0 4px 12px rgba(116, 142, 94, 0.14);
  }

  .warehouse-mobile-category__price {
    font-size: 13px;
  }

  .warehouse-mobile-search,
  .warehouse-mobile-batch {
    gap: 4px;
  }

  .warehouse-mobile-search {
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
  }

  .warehouse-mobile-search :deep(.el-input__wrapper) {
    border: 1px solid rgba(146, 174, 118, 0.25);
    background: rgba(255, 255, 250, 0.94);
    box-shadow: inset 0 1px 2px rgba(116, 142, 94, 0.06);
  }

  .warehouse-mobile-search__label {
    display: none;
  }

  .warehouse-mobile-search__button,
  .warehouse-mobile-batch__button {
    min-height: 26px;
    padding: 3px 9px;
    border-radius: 999px;
    font-size: 10px;
  }

  .warehouse-mobile-search__button {
    border: 0;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.22);
  }

  .warehouse-mobile-batch__button {
    border: 0;
    background: linear-gradient(135deg, #f2d39a, #dcae63);
    color: #65452a;
    box-shadow: 0 4px 10px rgba(198, 148, 74, 0.18);
  }

  .warehouse-mobile-batch__toolbar {
    gap: 6px;
  }

  .warehouse-mobile-batch__toolbar :deep(.el-checkbox) {
    min-width: 0;
    flex: 1;
  }

  .warehouse-mobile-batch__toolbar :deep(.el-checkbox__label) {
    padding-left: 5px;
    font-size: 10px;
    line-height: 1.2;
    white-space: normal;
  }

  .warehouse-mobile-batch__toolbar :deep(.el-checkbox__inner) {
    border-color: rgba(146, 174, 118, 0.42);
    background: rgba(255, 255, 250, 0.88);
  }

  .warehouse-mobile-batch__toolbar :deep(.el-checkbox__input.is-checked .el-checkbox__inner) {
    border-color: #6fa45c;
    background: #6fa45c;
  }

  .warehouse-list,
  .warehouse-list__group {
    gap: 5px;
  }

  .warehouse-pagination {
    padding: 0;
  }

  .warehouse-mobile-item {
    gap: 4px;
  }

  .warehouse-mobile-item__price {
    background: linear-gradient(135deg, rgba(223, 238, 205, 0.95), rgba(244, 232, 192, 0.92));
    color: #3f5d35;
  }

  .warehouse-mobile-item__top {
    grid-template-columns: 62px minmax(0, 1fr);
    align-items: start;
    gap: 7px;
  }

  .warehouse-mobile-item__thumb {
    justify-content: flex-start;
    --square-preview-size: 56px;
    --square-preview-radius: 14px;
  }

  .warehouse-mobile-item__body {
    gap: 3px;
  }

  .warehouse-mobile-item__title-wrap {
    align-items: flex-start;
    flex-direction: row;
    gap: 5px;
  }

  .warehouse-mobile-item__name {
    font-size: 12px;
    line-height: 1.25;
  }

  .warehouse-mobile-item__price {
    padding: 2px 6px;
    font-size: 9px;
  }

  .warehouse-mobile-item__meta-grid {
    gap: 2px;
  }

  .warehouse-mobile-item__meta {
    color: #4f4135;
    font-size: 10px;
    line-height: 1.25;
  }

  .warehouse-mobile-item__actions {
    gap: 5px;
    grid-template-columns: repeat(4, max-content);
    justify-content: start;
  }

  .warehouse-mobile-item__actions :deep(.el-button) {
    min-height: 22px;
    padding: 2px 7px;
    border-radius: 9px;
    border: 1px solid rgba(146, 174, 118, 0.34);
    background: rgba(255, 255, 250, 0.78);
    color: #526a45;
    font-size: 10px;
    box-shadow: none;
  }

  .warehouse-mobile-item__actions :deep(.el-button--primary),
  .warehouse-mobile-item__actions :deep(.el-button--success) {
    border-color: rgba(111, 164, 92, 0.32);
    background: rgba(232, 244, 215, 0.82);
    color: #4f8745;
  }

  .warehouse-mobile-item__actions :deep(.el-button--warning) {
    border-color: rgba(211, 164, 91, 0.32);
    background: rgba(252, 237, 205, 0.84);
    color: #9a6a2d;
  }

  .warehouse-mobile-item__actions :deep(.el-button--danger) {
    border-color: rgba(199, 105, 119, 0.28);
    background: rgba(251, 232, 228, 0.82);
    color: #b95662;
  }

  .warehouse-mobile-item__selection {
    font-size: 10px;
  }

  .warehouse-mobile-item__remark {
    font-size: 10px;
    line-height: 1.35;
  }
}
</style>
