<script setup lang="ts">
import { buildImagePreviewUrl } from '@/api/file'
import PaginationBar from '@/components/common/PaginationBar.vue'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import type { PublicPostCategory, PublicPostChannel, PublicPostListItem } from '@/types/public-post'

defineProps<{
  loading: boolean
  items: PublicPostListItem[]
  totalCount: number
  currentPage: number
  pageSize: number
  filterScope: 'all' | 'mine'
  filterDirection: 'all' | 'buy' | 'sell'
  filterChannel: PublicPostChannel | 'all'
  filterCategory: PublicPostCategory | 'all'
  filterKeyword: string
  filterMinPrice: number | null
  filterMaxPrice: number | null
  isAuthenticated: boolean
  channelOptions: { label: string; value: PublicPostChannel | 'all' }[]
  categoryOptions: { label: string; value: PublicPostCategory | 'all' }[]
  channelLabel: (value: PublicPostChannel) => string
  categoryLabel: (value: PublicPostCategory) => string
  formatDate: (value: string) => string
  directionLabel: (value: 'buy' | 'sell') => string
}>()

const emit = defineEmits<{
  (e: 'open-add'): void
  (e: 'page-change', page: number): void
  (e: 'update:filterKeyword', value: string): void
  (e: 'update:filterMinPrice', value: number | null): void
  (e: 'update:filterMaxPrice', value: number | null): void
  (e: 'update:filterScope', value: 'all' | 'mine'): void
  (e: 'update:filterDirection', value: 'all' | 'buy' | 'sell'): void
  (e: 'update:filterChannel', value: PublicPostChannel | 'all'): void
  (e: 'update:filterCategory', value: PublicPostCategory | 'all'): void
  (e: 'keyword-search'): void
  (e: 'keyword-clear'): void
  (e: 'price-search'): void
  (e: 'price-clear'): void
  (e: 'filter-change'): void
  (e: 'edit', item: PublicPostListItem): void
  (e: 'delete', item: PublicPostListItem): void
  (e: 'toggle-untrusted', item: PublicPostListItem): void
}>()
</script>

<template>
  <div class="public-zone-content">
    <section class="public-zone-header ah-glass-card ah-page-section">
      <div class="public-zone-header__info">
        <p class="public-zone-header__eyebrow">公开交易区</p>
        <h2>社区动态</h2>
        <p class="public-zone-header__desc">共 {{ totalCount }} 条交易记录</p>
      </div>
      <el-button v-if="isAuthenticated" type="primary" @click="emit('open-add')">
        + 发布交易
      </el-button>
    </section>

    <section class="public-zone-filters ah-glass-card ah-page-section">
      <div class="public-zone-filters__group public-zone-filters__search">
        <span class="public-zone-filters__label">名称</span>
        <el-input
          :model-value="filterKeyword"
          class="public-zone-filters__search-input"
          clearable
          placeholder="按物品名称筛选"
          @update:model-value="emit('update:filterKeyword', $event)"
          @keyup.enter="emit('keyword-search')"
          @clear="emit('keyword-clear')"
        />
        <el-button @click="emit('keyword-search')">搜索</el-button>
      </div>
      <div class="public-zone-filters__group public-zone-filters__price">
        <span class="public-zone-filters__label">价格</span>
        <el-input-number
          :model-value="filterMinPrice"
          class="public-zone-filters__price-input"
          :min="0"
          :precision="2"
          :step="1"
          placeholder="最低价"
          @update:model-value="emit('update:filterMinPrice', $event ?? null)"
        />
        <span class="public-zone-filters__range-sep">-</span>
        <el-input-number
          :model-value="filterMaxPrice"
          class="public-zone-filters__price-input"
          :min="0"
          :precision="2"
          :step="1"
          placeholder="最高价"
          @update:model-value="emit('update:filterMaxPrice', $event ?? null)"
        />
        <el-button @click="emit('price-search')">查询</el-button>
        <el-button @click="emit('price-clear')">清空</el-button>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">范围</span>
        <el-radio-group
          :model-value="filterScope"
          size="small"
          @update:model-value="emit('update:filterScope', $event); emit('filter-change')"
        >
          <el-radio-button label="all">全部记录</el-radio-button>
          <el-radio-button label="mine">我的记录</el-radio-button>
        </el-radio-group>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">方向</span>
        <el-radio-group
          :model-value="filterDirection"
          size="small"
          @update:model-value="emit('update:filterDirection', $event); emit('filter-change')"
        >
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="buy">买入</el-radio-button>
          <el-radio-button label="sell">卖出</el-radio-button>
        </el-radio-group>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">渠道</span>
        <el-select
          :model-value="filterChannel"
          size="small"
          @update:model-value="emit('update:filterChannel', $event); emit('filter-change')"
        >
          <el-option v-for="opt in channelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">分类</span>
        <el-select
          :model-value="filterCategory"
          size="small"
          @update:model-value="emit('update:filterCategory', $event); emit('filter-change')"
        >
          <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
    </section>

    <div class="public-zone-pagination ah-glass-card">
      <PaginationBar
        :current="currentPage"
        :total="totalCount"
        :page-size="pageSize"
        @change="emit('page-change', $event)"
      />
    </div>

    <div v-if="loading && items.length === 0" class="public-zone-loading">
      <span class="public-zone-loading__spinner" />
      <p>加载中...</p>
    </div>

    <div v-else-if="items.length === 0" class="public-zone-empty ah-glass-card ah-page-section">
      <p>还没有任何公开交易</p>
      <p class="public-zone-empty__sub">成为第一个发布的人吧</p>
    </div>

    <div v-else class="public-zone-list">
      <div
        v-for="item in items"
        :key="item.id"
        class="public-zone-item ah-glass-card ah-page-section"
      >
        <div class="public-zone-item__thumb">
          <SquareImagePreview :preview-url="buildImagePreviewUrl(item.imageFileId)" empty-text="无图" />
        </div>

        <div class="public-zone-item__body">
          <div class="public-zone-item__header">
            <h3 class="public-zone-item__name">{{ item.itemName }}</h3>
            <span class="public-zone-item__direction" :class="`is-${item.direction}`">
              {{ directionLabel(item.direction) }}
            </span>
            <span class="public-zone-item__price">¥{{ item.price }}</span>
          </div>

          <div class="public-zone-item__meta">
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="public-zone-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
            <span class="public-zone-item__dot">·</span>
            <span>{{ formatDate(item.tradeTime) }}</span>
          </div>

          <div class="public-zone-item__footer">
            <div class="public-zone-item__left">
              <div class="public-zone-item__time">
                <span>{{ formatDate(item.createdAt) }}</span>
              </div>
              <div class="public-zone-item__untrusted-row">
                <button
                  v-if="isAuthenticated && !item.mine"
                  type="button"
                  class="public-zone-item__untrusted-badge public-zone-item__untrusted-trigger"
                  :class="{ 'is-active': item.untrusted }"
                  @click="emit('toggle-untrusted', item)"
                >
                  不可信 × {{ item.untrustedCount }}
                </button>
                <span v-else class="public-zone-item__untrusted-badge">
                  不可信 × {{ item.untrustedCount }}
                </span>
              </div>
            </div>

            <div class="public-zone-item__actions">
              <template v-if="item.mine">
                <el-button size="small" @click="emit('edit', item)">编辑</el-button>
                <el-button size="small" type="danger" @click="emit('delete', item)">删除</el-button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <div class="public-zone-pagination ah-glass-card">
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
.public-zone-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.public-zone-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.public-zone-header__eyebrow {
  margin: 0 0 8px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.public-zone-header h2 {
  margin: 0;
  color: var(--ah-title);
  font-size: 28px;
}

.public-zone-header__desc {
  margin: 8px 0 0;
  color: var(--ah-text);
}

.public-zone-filters {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.public-zone-filters__group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.public-zone-filters__search {
  flex: 1 1 320px;
}

.public-zone-filters__search-input {
  min-width: 220px;
  max-width: 360px;
}

.public-zone-filters__price {
  flex: 1 1 420px;
  flex-wrap: wrap;
}

.public-zone-filters__price-input {
  width: 140px;
}

.public-zone-filters__range-sep {
  color: #8d7080;
  font-size: 14px;
}

.public-zone-filters__label {
  color: #8d7080;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.public-zone-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.public-zone-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: public-zone-spin 0.8s linear infinite;
}

@keyframes public-zone-spin {
  to {
    transform: rotate(360deg);
  }
}

.public-zone-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.public-zone-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.public-zone-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(460px, 1fr));
  gap: 14px;
}

.public-zone-pagination {
  grid-column: 1 / -1;
  padding: 8px 0;
}

.public-zone-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.public-zone-item.ah-page-section {
  padding: 18px;
}

.public-zone-item__thumb {
  flex-shrink: 0;
}

.public-zone-item__thumb :deep(.square-preview) {
  width: 112px;
  height: 112px;
  border-radius: 18px;
}

.public-zone-item__body {
  flex: 1;
  min-width: 0;
}

.public-zone-item__header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.public-zone-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 16px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.public-zone-item__direction {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.public-zone-item__direction.is-sell {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.public-zone-item__direction.is-buy {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.public-zone-item__price {
  font-size: 16px;
  font-weight: 700;
  color: var(--ah-accent-deep);
}

.public-zone-item__meta {
  margin-top: 6px;
  color: var(--ah-text);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.public-zone-item__dot {
  color: #c9a0b0;
}

.public-zone-item__untrusted-badge {
  background: rgba(207, 93, 117, 0.12);
  color: #c44d73;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
  border: 0;
}

.public-zone-item__footer {
  margin-top: 8px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.public-zone-item__left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.public-zone-item__time {
  font-size: 13px;
  color: #8d7080;
}

.public-zone-item__untrusted-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.public-zone-item__untrusted-trigger {
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.public-zone-item__untrusted-trigger:hover {
  background: rgba(207, 93, 117, 0.2);
  transform: translateY(-1px);
}

.public-zone-item__untrusted-trigger.is-active {
  background: rgba(207, 93, 117, 0.2);
  color: #a33860;
}

.public-zone-item__actions {
  display: flex;
  gap: 8px;
}
</style>
