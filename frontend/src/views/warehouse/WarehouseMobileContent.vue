<script setup lang="ts">
import type { InventoryCategory, InventoryChannel, InventoryListItem, InventorySummary } from '@/types/inventory'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import PaginationBar from '@/components/common/PaginationBar.vue'

const props = defineProps<{
  loading: boolean
  items: InventoryListItem[]
  totalCount: number
  summary: InventorySummary
  summaryTotalCount: number
  currentCategory: InventoryCategory | ''
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
      <el-button class="warehouse-mobile-hero__button" type="primary" @click="emit('open-add')">
        + 新增记录
      </el-button>
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
      <el-button @click="emit('keyword-search')">搜索</el-button>
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
        <el-checkbox
          :model-value="allSelectableChecked"
          :disabled="!hasSelectableItems"
          @change="emit('toggle-select-all', $event)"
        >
          全选本页可公开物品
        </el-checkbox>
        <p class="warehouse-mobile-batch__count">已选 {{ selectedSelectableIds.length }} 件</p>
        <el-button
          type="warning"
          :disabled="!selectedSelectableIds.length"
          :loading="batchPublicLoading"
          @click="emit('batch-public')"
        >
          批量公开 {{ selectedSelectableIds.length ? `(${selectedSelectableIds.length})` : '' }}
        </el-button>
        <p class="warehouse-mobile-batch__hint">仅支持选择未卖出且未公开的物品，避免误触取消公开。</p>
      </section>

      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
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
            <div class="warehouse-mobile-item__title-wrap">
              <h3 class="warehouse-mobile-item__name">{{ item.itemName }}</h3>
              <span class="warehouse-mobile-item__status" :class="`is-${item.status}`">
                {{ item.status === 'unsold' ? '未卖出' : '已卖出' }}
              </span>
            </div>
            <div class="warehouse-mobile-item__thumb">
              <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
            </div>
          </div>

          <div class="warehouse-mobile-item__meta-grid">
            <div class="warehouse-mobile-item__meta">
              <strong>¥{{ item.buyPrice }}</strong>
            </div>
            <div class="warehouse-mobile-item__meta">
              <strong>{{ formatDate(item.buyTime) }}</strong>
            </div>
            <div class="warehouse-mobile-item__meta">
              <strong>{{ channelLabel(item.channel) }}</strong>
            </div>
            <div class="warehouse-mobile-item__meta">
              <strong>{{ categoryLabel(item.category) }}</strong>
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

.warehouse-mobile-hero__button {
  width: 100%;
  margin-top: 16px;
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
}

.warehouse-mobile-item__thumb {
  display: flex;
  justify-content: center;
}

.warehouse-mobile-item__status {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.warehouse-mobile-item__status.is-unsold {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.warehouse-mobile-item__status.is-sold {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.warehouse-mobile-item__meta-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.warehouse-mobile-item__meta {
  display: grid;
  gap: 4px;
  padding: 12px;
  border-radius: 16px;
  background: rgba(255, 250, 247, 0.72);
}

.warehouse-mobile-item__meta-label {
  color: #b27f93;
  font-size: 12px;
  font-weight: 700;
}

.warehouse-mobile-item__meta strong {
  color: var(--ah-title);
  font-size: 14px;
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
</style>
