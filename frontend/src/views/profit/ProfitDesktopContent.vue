<script setup lang="ts">
import type { SortType, TradeCategory, TradeChannel, TradeListItem, TradeSummary } from '@/types/trade'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import PaginationBar from '@/components/common/PaginationBar.vue'
import PriceSortToggle from '@/components/common/PriceSortToggle.vue'

defineProps<{
  loading: boolean
  items: TradeListItem[]
  totalCount: number
  summary: TradeSummary
  profitDisplay: { profit: number; loss: number; net: number }
  summaryTotalCount: number
  currentScope: 'all' | 'profit' | 'loss'
  currentCategory: TradeCategory | ''
  sortType: SortType
  filterKeyword: string
  currentPage: number
  pageSize: number
  formatDate: (value: string) => string
  channelLabel: (value: TradeChannel) => string
  categoryLabel: (value: TradeCategory) => string
}>()

const emit = defineEmits<{
  (e: 'open-add'): void
  (e: 'page-change', page: number): void
  (e: 'scope-change', value: 'all' | 'profit' | 'loss'): void
  (e: 'category-filter', category: TradeCategory): void
  (e: 'update:filterKeyword', value: string): void
  (e: 'keyword-search'): void
  (e: 'keyword-clear'): void
  (e: 'sort-change', value: SortType | 'default'): void
  (e: 'edit', item: TradeListItem): void
  (e: 'public-action', item: TradeListItem): void
  (e: 'delete', item: TradeListItem): void
}>()
</script>

<template>
  <div class="profit-content">
    <section class="profit-summary ah-glass-card ah-page-section">
      <div class="profit-summary__stats">
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">总盈利</p>
          <p class="profit-summary__stat-value is-profit">+¥{{ profitDisplay.profit.toFixed(2) }}</p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">总亏损</p>
          <p class="profit-summary__stat-value is-loss">-¥{{ profitDisplay.loss.toFixed(2) }}</p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">净收益</p>
          <p class="profit-summary__stat-value" :class="profitDisplay.net >= 0 ? 'is-profit' : 'is-loss'">
            {{ profitDisplay.net >= 0 ? '+' : '' }}¥{{ profitDisplay.net.toFixed(2) }}
          </p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">已卖出</p>
          <p class="profit-summary__stat-value">{{ summaryTotalCount }} 件</p>
        </div>
      </div>
      <div class="profit-summary__actions">
        <el-radio-group :model-value="currentScope" size="default" @update:model-value="emit('scope-change', $event)">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="profit">盈利</el-radio-button>
          <el-radio-button label="loss">亏损</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="emit('open-add')">+ 新增记录</el-button>
      </div>
    </section>

    <section class="profit-category-summary ah-glass-card ah-page-section">
      <div class="profit-category-summary__status">
        当前筛选：{{ currentCategory ? categoryLabel(currentCategory) : '全部分类' }}
        <span v-if="currentCategory">，列表显示 {{ totalCount }} 件</span>
      </div>
      <div class="profit-category-summary__grid">
        <div
          class="profit-category-summary__item"
          :class="{ 'is-active': currentCategory === 'obi' }"
        >
          <p class="profit-category-summary__label">奥比总价格</p>
          <button
            type="button"
            class="profit-category-summary__price"
            @click="emit('category-filter', 'obi')"
          >
            ¥{{ summary.obiBuyAmount }}
          </button>
          <p class="profit-category-summary__count">奥比总件数 {{ summary.obiCount }} 件</p>
        </div>
        <div
          class="profit-category-summary__item"
          :class="{ 'is-active': currentCategory === 'magic' }"
        >
          <p class="profit-category-summary__label">魔力总价格</p>
          <button
            type="button"
            class="profit-category-summary__price"
            @click="emit('category-filter', 'magic')"
          >
            ¥{{ summary.magicBuyAmount }}
          </button>
          <p class="profit-category-summary__count">魔力总件数 {{ summary.magicCount }} 件</p>
        </div>
      </div>
    </section>

    <section class="profit-search ah-glass-card ah-page-section">
      <div class="profit-search__group">
        <span class="profit-search__label">名称</span>
        <el-input
          :model-value="filterKeyword"
          class="profit-search__input"
          clearable
          placeholder="按物品名称筛选"
          @update:model-value="emit('update:filterKeyword', $event)"
          @keyup.enter="emit('keyword-search')"
          @clear="emit('keyword-clear')"
        />
        <el-button @click="emit('keyword-search')">搜索</el-button>
      </div>
      <PriceSortToggle
        :model-value="sortType === 'sellTimeDesc' ? 'default' : sortType"
        asc-value="sellPriceAsc"
        desc-value="sellPriceDesc"
        @change="emit('sort-change', $event as SortType | 'default')"
      />
    </section>

    <div v-if="loading" class="profit-loading">
      <span class="profit-loading__spinner" />
      <p>加载中...</p>
    </div>

    <div v-else-if="items.length === 0" class="profit-empty ah-glass-card ah-page-section">
      <p>还没有已卖出的记录</p>
      <p class="profit-empty__sub">从「我的仓库」标记卖出，或在此新增记录</p>
    </div>

    <div v-else class="profit-list">
      <div class="profit-pagination ah-glass-card">
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
        class="profit-item ah-glass-card ah-page-section"
      >
        <div class="profit-item__thumb">
          <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
        </div>

        <div class="profit-item__body">
          <div class="profit-item__header">
            <h3 class="profit-item__name">{{ item.itemName }}</h3>
            <span
              class="profit-item__profit"
              :class="item.profitAmount >= 0 ? 'is-profit' : 'is-loss'"
            >
              {{ item.profitAmount >= 0 ? '+' : '' }}¥{{ item.profitAmount }}
            </span>
          </div>

          <div class="profit-item__meta">
            <span>买入 ¥{{ item.buyPrice }} · {{ formatDate(item.buyTime) }}</span>
            <span class="profit-item__arrow">→</span>
            <span>卖出 ¥{{ item.sellPrice }} · {{ formatDate(item.sellTime) }}</span>
          </div>

          <div class="profit-item__meta">
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="profit-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
            <span v-if="item.publicPosted" class="profit-item__dot">·</span>
            <span v-if="item.publicPosted" class="profit-item__public-badge">已公开</span>
          </div>

          <p v-if="item.remark" class="profit-item__remark">备注：{{ item.remark }}</p>

          <div class="profit-item__actions">
            <el-button size="small" @click="emit('edit', item)">编辑</el-button>
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

      <div class="profit-pagination ah-glass-card">
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
.profit-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profit-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.profit-summary__stats {
  display: grid;
  grid-template-columns: repeat(4, auto);
  gap: 20px;
}

.profit-summary__stat {
  text-align: center;
}

.profit-summary__stat-label {
  margin: 0 0 4px;
  color: #b27f93;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.profit-summary__stat-value {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.profit-summary__stat-value.is-profit { color: #4caf7d; }
.profit-summary__stat-value.is-loss { color: #cf5d75; }

.profit-summary__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profit-category-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profit-category-summary__status {
  color: #8d7080;
  font-size: 13px;
}

.profit-category-summary__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.profit-category-summary__item {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 250, 247, 0.78);
  border: 1px solid rgba(216, 168, 183, 0.16);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.profit-category-summary__item.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.profit-category-summary__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-category-summary__price {
  margin-top: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ah-title);
  font-size: 26px;
  font-weight: 800;
  cursor: pointer;
}

.profit-category-summary__price:hover {
  color: #cf5d75;
}

.profit-category-summary__count {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 14px;
}

.profit-search {
  position: relative;
  overflow: hidden;
  display: grid;
  grid-template-columns: minmax(360px, 760px) max-content;
  align-items: center;
  justify-content: space-between;
  gap: 18px;
  padding-left: 34px;
  background:
    linear-gradient(90deg, rgba(255, 216, 107, 0.18), rgba(255, 252, 247, 0.92) 36%, rgba(255, 255, 255, 0.78));
}

.profit-search::before {
  position: absolute;
  left: 14px;
  top: 18px;
  bottom: 18px;
  width: 6px;
  content: '';
  border-radius: 999px;
  background: linear-gradient(180deg, var(--ah-highlight), var(--ah-accent));
  box-shadow: 0 8px 18px rgba(255, 194, 80, 0.24);
}

.profit-search__group {
  display: flex;
  align-items: center;
  gap: 12px;
  min-width: 0;
  width: min(100%, 760px);
}

.profit-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-search__input {
  flex: 1;
  min-width: 260px;
  max-width: 620px;
}

.profit-search :deep(.price-sort-toggle) {
  flex-wrap: nowrap;
  justify-self: end;
}

.profit-search :deep(.price-sort-toggle__label),
.profit-search :deep(.price-sort-toggle__button) {
  white-space: nowrap;
}

@media (max-width: 1100px) {
  .profit-search {
    grid-template-columns: 1fr;
    justify-content: stretch;
  }

  .profit-search :deep(.price-sort-toggle) {
    justify-self: start;
  }
}

.profit-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.profit-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: profit-spin 0.8s linear infinite;
}

@keyframes profit-spin {
  to { transform: rotate(360deg); }
}

.profit-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.profit-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.profit-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(460px, 1fr));
  gap: 14px;
}

.profit-pagination {
  grid-column: 1 / -1;
  padding: 8px 0;
}

.profit-item {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.profit-item.ah-page-section {
  padding: 18px;
}

.profit-item__thumb {
  flex-shrink: 0;
}

.profit-item__thumb :deep(.square-preview) {
  width: 112px;
  height: 112px;
  border-radius: 18px;
}

.profit-item__body {
  flex: 1;
  min-width: 0;
}

.profit-item__header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profit-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 16px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.profit-item__profit {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
}

.profit-item__profit.is-profit {
  background: rgba(76, 175, 125, 0.15);
  color: #3d8c5a;
}

.profit-item__profit.is-loss {
  background: rgba(207, 93, 117, 0.15);
  color: #c44d73;
}

.profit-item__meta {
  margin-top: 6px;
  color: var(--ah-text);
  font-size: 13px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.profit-item__arrow {
  color: var(--ah-accent);
  font-weight: 700;
}

.profit-item__dot {
  color: #c9a0b0;
}

.profit-item__public-badge {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.profit-item__remark {
  margin: 6px 0 0;
  color: var(--ah-text);
  font-size: 13px;
}

.profit-item__actions {
  margin-top: 10px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}
</style>
