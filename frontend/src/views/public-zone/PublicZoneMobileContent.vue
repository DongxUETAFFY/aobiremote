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
  <div class="public-zone-content public-zone-content--mobile">
    <section class="public-zone-mobile-hero ah-glass-card ah-page-section">
      <p class="public-zone-mobile-hero__label">公开交易区</p>
      <h2 class="public-zone-mobile-hero__count">{{ totalCount }} 条交易记录</h2>
      <p class="public-zone-mobile-hero__desc">浏览社区公开买入与卖出信息</p>
    </section>

    <section class="public-zone-mobile-search ah-glass-card ah-page-section">
      <span class="public-zone-mobile-search__label">名称</span>
      <el-input
        :model-value="filterKeyword"
        clearable
        placeholder="按物品名称筛选"
        @update:model-value="emit('update:filterKeyword', $event)"
        @keyup.enter="emit('keyword-search')"
        @clear="emit('keyword-clear')"
      />
      <el-button class="public-zone-mobile-search__button" @click="emit('keyword-search')">搜索</el-button>
    </section>

    <section class="public-zone-mobile-filters ah-glass-card ah-page-section">
      <div class="public-zone-mobile-filters__block public-zone-mobile-filters__block--price">
        <span class="public-zone-mobile-filters__label">价格</span>
        <div class="public-zone-mobile-filters__price-line">
          <el-input-number
            :model-value="filterMinPrice"
            :min="0"
            :precision="2"
            :step="1"
            :controls="false"
            placeholder="最低价"
            @update:model-value="emit('update:filterMinPrice', $event ?? null)"
          />
          <el-input-number
            :model-value="filterMaxPrice"
            :min="0"
            :precision="2"
            :step="1"
            :controls="false"
            placeholder="最高价"
            @update:model-value="emit('update:filterMaxPrice', $event ?? null)"
          />
          <el-button @click="emit('price-search')">查询</el-button>
          <el-button @click="emit('price-clear')">清空</el-button>
        </div>
      </div>

      <div class="public-zone-mobile-filters__block">
        <span class="public-zone-mobile-filters__label">范围</span>
        <el-radio-group
          :model-value="filterScope"
          @update:model-value="emit('update:filterScope', $event); emit('filter-change')"
        >
          <el-radio-button label="all">全部记录</el-radio-button>
          <el-radio-button label="mine">我的记录</el-radio-button>
        </el-radio-group>
      </div>

      <div class="public-zone-mobile-filters__block">
        <span class="public-zone-mobile-filters__label">方向</span>
        <el-radio-group
          :model-value="filterDirection"
          @update:model-value="emit('update:filterDirection', $event); emit('filter-change')"
        >
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="buy">买入</el-radio-button>
          <el-radio-button label="sell">卖出</el-radio-button>
        </el-radio-group>
      </div>

      <div class="public-zone-mobile-filters__select-row">
        <div class="public-zone-mobile-filters__block public-zone-mobile-filters__block--select">
          <span class="public-zone-mobile-filters__label">渠道</span>
          <el-select
            :model-value="filterChannel"
            @update:model-value="emit('update:filterChannel', $event); emit('filter-change')"
          >
            <el-option v-for="opt in channelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>

        <div class="public-zone-mobile-filters__block public-zone-mobile-filters__block--select">
          <span class="public-zone-mobile-filters__label">分类</span>
          <el-select
            :model-value="filterCategory"
            @update:model-value="emit('update:filterCategory', $event); emit('filter-change')"
          >
            <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
          </el-select>
        </div>
      </div>
    </section>

    <div v-if="loading && items.length === 0" class="public-zone-loading">
      <span class="public-zone-loading__spinner" />
      <p>加载中...</p>
    </div>

    <div v-else-if="items.length === 0" class="public-zone-empty ah-glass-card ah-page-section">
      <p>还没有任何公开交易</p>
      <p class="public-zone-empty__sub">成为第一个发布的人吧</p>
    </div>

    <div v-else class="public-zone-list">
      <div class="public-zone-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="pageSize"
          hide-status
          @change="emit('page-change', $event)"
        />
      </div>

      <article
        v-for="item in items"
        :key="item.id"
        class="public-zone-mobile-item ah-glass-card ah-page-section"
      >
        <div class="public-zone-mobile-item__title-row">
          <h3 class="public-zone-mobile-item__name">{{ item.itemName }}</h3>
          <span class="public-zone-mobile-item__direction" :class="`is-${item.direction}`">
            {{ directionLabel(item.direction) }}
          </span>
        </div>

        <div class="public-zone-mobile-item__price-row">
          <strong class="public-zone-mobile-item__price">¥{{ item.price }}</strong>
          <span class="public-zone-mobile-item__trade-time">{{ formatDate(item.tradeTime) }}</span>
        </div>

        <div class="public-zone-mobile-item__thumb">
          <SquareImagePreview :preview-url="buildImagePreviewUrl(item.imageFileId)" empty-text="无图" />
        </div>

        <div class="public-zone-mobile-item__meta-grid">
          <p class="public-zone-mobile-item__meta">渠道: {{ channelLabel(item.channel) }}</p>
          <p class="public-zone-mobile-item__meta">分类: {{ categoryLabel(item.category) }}</p>
        </div>

        <p class="public-zone-mobile-item__created-at">发布时间 {{ formatDate(item.createdAt) }}</p>

        <div class="public-zone-mobile-item__untrusted-row">
          <button
            v-if="isAuthenticated && !item.mine"
            type="button"
            class="public-zone-mobile-item__untrusted public-zone-mobile-item__untrusted--trigger"
            :class="{ 'is-active': item.untrusted }"
            @click="emit('toggle-untrusted', item)"
          >
            不可信 × {{ item.untrustedCount }}
          </button>
          <span v-else class="public-zone-mobile-item__untrusted">
            不可信 × {{ item.untrustedCount }}
          </span>
        </div>

        <div v-if="item.mine" class="public-zone-mobile-item__actions">
          <el-button @click="emit('edit', item)">编辑</el-button>
          <el-button type="danger" @click="emit('delete', item)">删除</el-button>
        </div>
      </article>

      <div class="public-zone-pagination ah-glass-card">
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
.public-zone-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.public-zone-mobile-hero__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.public-zone-mobile-hero__count {
  margin: 8px 0 0;
  color: var(--ah-title);
  font-size: 30px;
  line-height: 1.05;
}

.public-zone-mobile-hero__desc {
  margin: 10px 0 0;
  color: var(--ah-text);
}

.public-zone-mobile-search,
.public-zone-mobile-filters {
  display: grid;
  gap: 12px;
}

.public-zone-mobile-search__label,
.public-zone-mobile-filters__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.public-zone-mobile-filters__block {
  display: grid;
  gap: 10px;
}

.public-zone-mobile-filters__price-row,
.public-zone-mobile-filters__price-line,
.public-zone-mobile-filters__select-row {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.public-zone-mobile-filters__price-actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.public-zone-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px 24px;
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
  padding: 44px 24px;
  color: var(--ah-title);
}

.public-zone-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.public-zone-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.public-zone-pagination {
  padding: 8px 0;
}

.public-zone-mobile-item {
  display: grid;
  gap: 14px;
}

.public-zone-mobile-item__title-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.public-zone-mobile-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
  line-height: 1.3;
  flex: 1;
  min-width: 0;
  overflow-wrap: anywhere;
}

.public-zone-mobile-item__direction {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
  white-space: nowrap;
}

.public-zone-mobile-item__direction.is-sell {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.public-zone-mobile-item__direction.is-buy {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.public-zone-mobile-item__price-row {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.public-zone-mobile-item__price {
  color: var(--ah-accent-deep);
  font-size: 24px;
  font-weight: 800;
}

.public-zone-mobile-item__trade-time {
  color: #8d7080;
  font-size: 13px;
}

.public-zone-mobile-item__thumb {
  display: flex;
  justify-content: center;
}

.public-zone-mobile-item__meta-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.public-zone-mobile-item__meta {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.35;
}

.public-zone-mobile-item__created-at {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
}

.public-zone-mobile-item__untrusted-row {
  display: flex;
  align-items: center;
}

.public-zone-mobile-item__untrusted {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  padding: 6px 12px;
  border-radius: 10px;
  background: rgba(207, 93, 117, 0.12);
  color: #c44d73;
  font-size: 13px;
  border: 0;
}

.public-zone-mobile-item__untrusted--trigger {
  cursor: pointer;
  transition: transform 0.18s ease, background 0.18s ease, color 0.18s ease;
}

.public-zone-mobile-item__untrusted--trigger:hover {
  background: rgba(207, 93, 117, 0.2);
  transform: translateY(-1px);
}

.public-zone-mobile-item__untrusted--trigger.is-active {
  background: rgba(207, 93, 117, 0.2);
  color: #a33860;
}

.public-zone-mobile-item__actions {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 10px;
}

.public-zone-mobile-item__actions :deep(.el-button) {
  margin: 0;
}

@media (max-width: 768px) {
  .public-zone-mobile-hero,
  .public-zone-mobile-search,
  .public-zone-mobile-filters,
  .public-zone-pagination,
  .public-zone-mobile-item {
    border-color: rgba(146, 174, 118, 0.25);
    background:
      linear-gradient(135deg, rgba(255, 254, 244, 0.95) 0%, rgba(250, 244, 226, 0.9) 100%);
    box-shadow: 0 3px 10px rgba(116, 142, 94, 0.1);
  }

  .public-zone-content {
    gap: 5px;
  }

  .public-zone-mobile-hero__label,
  .public-zone-mobile-search__label,
  .public-zone-mobile-filters__label {
    font-size: 11px;
  }

  .public-zone-mobile-hero__count {
    margin-top: 2px;
    font-size: 16px;
  }

  .public-zone-mobile-hero__desc,
  .public-zone-mobile-item__created-at {
    font-size: 10px;
  }

  .public-zone-mobile-hero__desc {
    margin-top: 2px;
  }

  .public-zone-mobile-search,
  .public-zone-mobile-filters {
    gap: 4px;
  }

  .public-zone-mobile-search {
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
  }

  .public-zone-mobile-search :deep(.el-input__wrapper),
  .public-zone-mobile-filters :deep(.el-input-number .el-input__wrapper),
  .public-zone-mobile-filters :deep(.el-select__wrapper) {
    border: 1px solid rgba(146, 174, 118, 0.25);
    background: rgba(255, 255, 250, 0.94);
    box-shadow: inset 0 1px 2px rgba(116, 142, 94, 0.06);
  }

  .public-zone-mobile-search__label {
    display: none;
  }

  .public-zone-mobile-search__button {
    min-height: 26px;
    padding: 3px 9px;
    border-radius: 999px;
    border: 0;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    font-size: 10px;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.22);
  }

  .public-zone-mobile-filters__block {
    gap: 4px;
  }

  .public-zone-mobile-filters__block--price > .public-zone-mobile-filters__label,
  .public-zone-mobile-filters__block--select > .public-zone-mobile-filters__label {
    display: none;
  }

  .public-zone-mobile-filters__price-row,
  .public-zone-mobile-filters__price-line,
  .public-zone-mobile-filters__price-actions,
  .public-zone-mobile-filters__select-row {
    gap: 4px;
  }

  .public-zone-mobile-filters__price-line {
    grid-template-columns: minmax(0, 1fr) minmax(0, 1fr) 42px 42px;
    align-items: center;
  }

  .public-zone-mobile-filters__price-line :deep(.el-input-number) {
    width: 100%;
    min-width: 0;
  }

  .public-zone-mobile-filters__price-line :deep(.el-input__inner) {
    text-align: center;
  }

  .public-zone-mobile-filters__select-row {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    align-items: center;
  }

  .public-zone-mobile-filters__block--select {
    min-width: 0;
  }

  .public-zone-mobile-filters__price-line :deep(.el-button),
  .public-zone-mobile-filters__price-actions :deep(.el-button) {
    min-height: 24px;
    width: 42px;
    padding: 3px 0;
    border-radius: 999px;
    border: 1px solid rgba(146, 174, 118, 0.3);
    background: rgba(255, 255, 250, 0.78);
    color: #526a45;
    font-size: 10px;
    box-shadow: none;
  }

  .public-zone-mobile-filters__price-line :deep(.el-button:first-of-type),
  .public-zone-mobile-filters__price-actions :deep(.el-button:first-child) {
    border: 0;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.18);
  }

  .public-zone-mobile-filters :deep(.el-radio-button__inner) {
    min-height: 24px;
    padding: 4px 9px;
    border-color: rgba(146, 174, 118, 0.28);
    background: rgba(255, 255, 250, 0.78);
    color: #6d7657;
    font-size: 10px;
    box-shadow: none;
  }

  .public-zone-mobile-filters :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    border-color: #6fa45c;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.18);
  }

  .public-zone-list {
    gap: 5px;
  }

  .public-zone-pagination {
    padding: 0;
  }

  .public-zone-mobile-item {
    grid-template-columns: 62px minmax(0, 1fr);
    gap: 4px 7px;
  }

  .public-zone-mobile-item__title-row,
  .public-zone-mobile-item__price-row,
  .public-zone-mobile-item__meta-grid,
  .public-zone-mobile-item__created-at,
  .public-zone-mobile-item__untrusted-row,
  .public-zone-mobile-item__actions {
    grid-column: 2;
  }

  .public-zone-mobile-item__thumb {
    grid-column: 1;
    grid-row: 1 / span 3;
    justify-content: flex-start;
  }

  .public-zone-mobile-item__title-row {
    align-items: flex-start;
    flex-direction: row;
    gap: 4px;
  }

  .public-zone-mobile-item__name {
    font-size: 12px;
    line-height: 1.25;
  }

  .public-zone-mobile-item__direction,
  .public-zone-mobile-item__untrusted {
    padding: 2px 6px;
    border-radius: 8px;
    font-size: 9px;
  }

  .public-zone-mobile-item__direction.is-buy {
    background: rgba(232, 244, 215, 0.88);
    color: #4f8745;
  }

  .public-zone-mobile-item__direction.is-sell {
    background: rgba(252, 237, 205, 0.9);
    color: #9a6a2d;
  }

  .public-zone-mobile-item__price {
    font-size: 13px;
  }

  .public-zone-mobile-item__trade-time {
    font-size: 10px;
  }

  .public-zone-mobile-item__meta-grid {
    gap: 2px;
  }

  .public-zone-mobile-item__meta {
    color: #4f4135;
    font-size: 10px;
    line-height: 1.25;
  }

  .public-zone-mobile-item__actions {
    gap: 5px;
    grid-template-columns: repeat(2, max-content);
    justify-content: start;
  }

  .public-zone-mobile-item__actions :deep(.el-button) {
    min-height: 22px;
    padding: 2px 7px;
    border-radius: 9px;
    border: 1px solid rgba(146, 174, 118, 0.34);
    background: rgba(255, 255, 250, 0.78);
    color: #526a45;
    font-size: 10px;
    box-shadow: none;
  }

  .public-zone-mobile-item__actions :deep(.el-button--danger) {
    border-color: rgba(199, 105, 119, 0.28);
    background: rgba(251, 232, 228, 0.82);
    color: #b95662;
  }

  .public-zone-mobile-item__untrusted {
    border: 1px solid rgba(199, 105, 119, 0.22);
    background: rgba(251, 232, 228, 0.78);
    color: #b95662;
  }
}
</style>
