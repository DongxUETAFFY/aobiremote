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
  <div class="profit-content profit-content--mobile">
    <section class="profit-mobile-hero ah-glass-card ah-page-section">
      <p class="profit-mobile-hero__label">盈亏统计</p>
      <h2 class="profit-mobile-hero__count">{{ summaryTotalCount }} 件已卖出</h2>
      <div class="profit-mobile-hero__stats">
        <div class="profit-mobile-hero__stat">
          <span>总盈利</span>
          <strong class="is-profit">+¥{{ profitDisplay.profit.toFixed(2) }}</strong>
        </div>
        <div class="profit-mobile-hero__stat">
          <span>总亏损</span>
          <strong class="is-loss">-¥{{ profitDisplay.loss.toFixed(2) }}</strong>
        </div>
        <div class="profit-mobile-hero__stat">
          <span>净收益</span>
          <strong :class="profitDisplay.net >= 0 ? 'is-profit' : 'is-loss'">
            {{ profitDisplay.net >= 0 ? '+' : '' }}¥{{ profitDisplay.net.toFixed(2) }}
          </strong>
        </div>
      </div>
      <el-radio-group
        class="profit-mobile-hero__scope"
        :model-value="currentScope"
        @update:model-value="emit('scope-change', $event)"
      >
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="profit">盈利</el-radio-button>
        <el-radio-button label="loss">亏损</el-radio-button>
      </el-radio-group>
    </section>

    <section class="profit-mobile-category ah-glass-card ah-page-section">
      <button
        type="button"
        class="profit-mobile-category__card"
        :class="{ 'is-active': currentCategory === 'obi' }"
        @click="emit('category-filter', 'obi')"
      >
        <span class="profit-mobile-category__label">奥比总价格</span>
        <strong class="profit-mobile-category__price">¥{{ summary.obiBuyAmount }}</strong>
        <span class="profit-mobile-category__count">奥比总件数 {{ summary.obiCount }} 件</span>
      </button>
      <button
        type="button"
        class="profit-mobile-category__card"
        :class="{ 'is-active': currentCategory === 'magic' }"
        @click="emit('category-filter', 'magic')"
      >
        <span class="profit-mobile-category__label">魔力总价格</span>
        <strong class="profit-mobile-category__price">¥{{ summary.magicBuyAmount }}</strong>
        <span class="profit-mobile-category__count">魔力总件数 {{ summary.magicCount }} 件</span>
      </button>
    </section>

    <section class="profit-mobile-search ah-glass-card ah-page-section">
      <span class="profit-mobile-search__label">名称</span>
      <el-input
        :model-value="filterKeyword"
        clearable
        placeholder="按物品名称筛选"
        @update:model-value="emit('update:filterKeyword', $event)"
        @keyup.enter="emit('keyword-search')"
        @clear="emit('keyword-clear')"
      />
      <el-button class="profit-mobile-search__button" @click="emit('keyword-search')">搜索</el-button>
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
          hide-status
          @change="emit('page-change', $event)"
        />
      </div>

      <article
        v-for="item in items"
        :key="item.id"
        class="profit-mobile-item ah-glass-card ah-page-section"
      >
        <div class="profit-mobile-item__top">
          <div class="profit-mobile-item__thumb">
            <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
          </div>
          <div class="profit-mobile-item__body">
            <div class="profit-mobile-item__title-wrap">
              <h3 class="profit-mobile-item__name">{{ item.itemName }}</h3>
              <span class="profit-mobile-item__profit" :class="item.profitAmount >= 0 ? 'is-profit' : 'is-loss'">
                {{ item.profitAmount >= 0 ? '+' : '' }}¥{{ item.profitAmount }}
              </span>
            </div>

            <div class="profit-mobile-item__trade">
              <p class="profit-mobile-item__trade-line">买入: ¥{{ item.buyPrice }} / {{ formatDate(item.buyTime) }}</p>
              <p class="profit-mobile-item__trade-line">卖出: ¥{{ item.sellPrice }} / {{ formatDate(item.sellTime) }}</p>
            </div>

            <div class="profit-mobile-item__meta-grid">
              <p class="profit-mobile-item__meta">渠道: {{ channelLabel(item.channel) }}</p>
              <p class="profit-mobile-item__meta">分类: {{ categoryLabel(item.category) }}</p>
            </div>
          </div>
        </div>

        <p v-if="item.publicPosted" class="profit-mobile-item__public-badge">已公开</p>
        <p v-if="item.remark" class="profit-mobile-item__remark">备注：{{ item.remark }}</p>

        <div class="profit-mobile-item__actions">
          <el-button @click="emit('edit', item)">编辑</el-button>
          <el-button
            :type="item.publicPosted ? 'warning' : 'primary'"
            @click="emit('public-action', item)"
          >
            {{ item.publicPosted ? '取消公开' : '公开' }}
          </el-button>
          <el-button type="danger" @click="emit('delete', item)">删除</el-button>
        </div>
      </article>

      <div class="profit-pagination ah-glass-card">
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
.profit-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.profit-mobile-hero__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.profit-mobile-hero__count {
  margin: 8px 0 0;
  color: var(--ah-title);
  font-size: 30px;
  line-height: 1.05;
}

.profit-mobile-hero__stats {
  display: grid;
  gap: 10px;
  margin-top: 14px;
}

.profit-mobile-hero__stat {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  padding: 12px 14px;
  border-radius: 16px;
  background: rgba(255, 250, 247, 0.72);
}

.profit-mobile-hero__stat span {
  color: #8d7080;
  font-size: 13px;
}

.profit-mobile-hero__stat strong {
  font-size: 16px;
}

.is-profit {
  color: #4caf7d;
}

.is-loss {
  color: #cf5d75;
}

.profit-mobile-hero__scope {
  margin-top: 16px;
}

.profit-mobile-category {
  display: grid;
  gap: 12px;
}

.profit-mobile-category__card {
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

.profit-mobile-category__card.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.profit-mobile-category__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-mobile-category__price {
  color: var(--ah-title);
  font-size: 24px;
}

.profit-mobile-category__count {
  color: var(--ah-text);
  font-size: 14px;
}

.profit-mobile-search {
  display: grid;
  gap: 12px;
}

.profit-mobile-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px 24px;
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
  padding: 44px 24px;
  color: var(--ah-title);
}

.profit-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.profit-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profit-pagination {
  padding: 8px 0;
}

.profit-mobile-item {
  display: grid;
  gap: 14px;
}

.profit-mobile-item__top {
  display: grid;
  gap: 12px;
}

.profit-mobile-item__body {
  display: grid;
  gap: 10px;
  min-width: 0;
}

.profit-mobile-item__title-wrap {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 10px;
}

.profit-mobile-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
  line-height: 1.3;
  flex: 1;
  min-width: 0;
  overflow-wrap: anywhere;
}

.profit-mobile-item__profit {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.profit-mobile-item__profit.is-profit {
  background: rgba(76, 175, 125, 0.15);
  color: #3d8c5a;
}

.profit-mobile-item__profit.is-loss {
  background: rgba(207, 93, 117, 0.15);
  color: #c44d73;
}

.profit-mobile-item__thumb {
  display: flex;
  justify-content: center;
}

.profit-mobile-item__trade {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profit-mobile-item__trade-line {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.35;
}

.profit-mobile-item__meta-grid {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.profit-mobile-item__meta {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.35;
}

.profit-mobile-item__public-badge {
  margin: 0;
  justify-self: start;
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
}

.profit-mobile-item__remark {
  margin: 0;
  color: var(--ah-text);
  font-size: 13px;
  line-height: 1.6;
}

.profit-mobile-item__actions {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 10px;
}

.profit-mobile-item__actions :deep(.el-button) {
  margin: 0;
}

@media (max-width: 768px) {
  .profit-mobile-hero,
  .profit-mobile-category,
  .profit-mobile-search,
  .profit-pagination,
  .profit-mobile-item {
    border-color: rgba(146, 174, 118, 0.25);
    background:
      linear-gradient(135deg, rgba(255, 254, 244, 0.95) 0%, rgba(250, 244, 226, 0.9) 100%);
    box-shadow: 0 3px 10px rgba(116, 142, 94, 0.1);
  }

  .profit-content {
    gap: 5px;
  }

  .profit-mobile-hero__label,
  .profit-mobile-category__label,
  .profit-mobile-search__label {
    font-size: 11px;
  }

  .profit-mobile-hero__count {
    margin-top: 2px;
    font-size: 16px;
  }

  .profit-mobile-hero__stats {
    gap: 4px;
    margin-top: 5px;
  }

  .profit-mobile-hero__stat {
    padding: 5px 7px;
    border-radius: 9px;
    border: 1px solid rgba(146, 174, 118, 0.16);
    background: rgba(255, 253, 245, 0.72);
  }

  .profit-mobile-hero__stat span,
  .profit-mobile-category__count,
  .profit-mobile-item__remark {
    font-size: 11px;
  }

  .profit-mobile-hero__stat strong {
    font-size: 11px;
  }

  .profit-mobile-hero__scope {
    margin-top: 5px;
  }

  .profit-mobile-hero__scope :deep(.el-radio-button__inner) {
    min-height: 24px;
    padding: 4px 9px;
    border-color: rgba(146, 174, 118, 0.28);
    background: rgba(255, 255, 250, 0.78);
    color: #6d7657;
    font-size: 10px;
    box-shadow: none;
  }

  .profit-mobile-hero__scope :deep(.el-radio-button__original-radio:checked + .el-radio-button__inner) {
    border-color: #6fa45c;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.18);
  }

  .profit-mobile-category {
    grid-template-columns: repeat(2, minmax(0, 1fr));
    gap: 5px;
  }

  .profit-mobile-category__card {
    gap: 1px;
    padding: 5px 7px;
    border-radius: 9px;
    border-color: rgba(146, 174, 118, 0.2);
    background: rgba(255, 253, 245, 0.82);
    box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.72);
  }

  .profit-mobile-category__card.is-active {
    border-color: rgba(100, 151, 86, 0.42);
    background: linear-gradient(135deg, rgba(232, 244, 215, 0.94), rgba(255, 247, 224, 0.92));
    box-shadow: 0 4px 12px rgba(116, 142, 94, 0.14);
  }

  .profit-mobile-category__price {
    font-size: 13px;
  }

  .profit-mobile-search {
    gap: 4px;
  }

  .profit-mobile-search {
    grid-template-columns: minmax(0, 1fr) auto;
    align-items: center;
  }

  .profit-mobile-search :deep(.el-input__wrapper) {
    border: 1px solid rgba(146, 174, 118, 0.25);
    background: rgba(255, 255, 250, 0.94);
    box-shadow: inset 0 1px 2px rgba(116, 142, 94, 0.06);
  }

  .profit-mobile-search__label {
    display: none;
  }

  .profit-mobile-search__button {
    min-height: 26px;
    padding: 3px 9px;
    border-radius: 999px;
    border: 0;
    background: linear-gradient(135deg, #9fc87f, #6fa45c);
    color: #fff;
    font-size: 10px;
    box-shadow: 0 4px 10px rgba(111, 164, 92, 0.22);
  }

  .profit-list {
    gap: 5px;
  }

  .profit-pagination {
    padding: 0;
  }

  .profit-mobile-item {
    gap: 4px;
  }

  .profit-mobile-item__top {
    grid-template-columns: 62px minmax(0, 1fr);
    align-items: start;
    gap: 7px;
  }

  .profit-mobile-item__thumb {
    justify-content: flex-start;
    --square-preview-size: 56px;
    --square-preview-radius: 14px;
  }

  .profit-mobile-item__body {
    gap: 2px;
  }

  .profit-mobile-item__title-wrap {
    align-items: flex-start;
    flex-direction: row;
    gap: 5px;
  }

  .profit-mobile-item__name {
    font-size: 12px;
    line-height: 1.25;
  }

  .profit-mobile-item__profit {
    background: linear-gradient(135deg, rgba(223, 238, 205, 0.95), rgba(244, 232, 192, 0.92));
    padding: 2px 6px;
    font-size: 9px;
  }

  .profit-mobile-item__profit.is-loss {
    background: rgba(251, 232, 228, 0.86);
  }

  .profit-mobile-item__trade {
    gap: 2px;
  }

  .profit-mobile-item__meta-grid {
    gap: 2px;
  }

  .profit-mobile-item__trade-line,
  .profit-mobile-item__meta {
    color: #4f4135;
    font-size: 10px;
    line-height: 1.25;
  }

  .profit-mobile-item__public-badge {
    border: 1px solid rgba(111, 164, 92, 0.24);
    background: rgba(232, 244, 215, 0.8);
    color: #4f8745;
    padding: 2px 6px;
    font-size: 9px;
  }

  .profit-mobile-item__actions {
    gap: 5px;
    grid-template-columns: repeat(3, max-content);
    justify-content: start;
  }

  .profit-mobile-item__actions :deep(.el-button) {
    min-height: 22px;
    padding: 2px 7px;
    border-radius: 9px;
    border: 1px solid rgba(146, 174, 118, 0.34);
    background: rgba(255, 255, 250, 0.78);
    color: #526a45;
    font-size: 10px;
    box-shadow: none;
  }

  .profit-mobile-item__actions :deep(.el-button--primary) {
    border-color: rgba(111, 164, 92, 0.32);
    background: rgba(232, 244, 215, 0.82);
    color: #4f8745;
  }

  .profit-mobile-item__actions :deep(.el-button--warning) {
    border-color: rgba(211, 164, 91, 0.32);
    background: rgba(252, 237, 205, 0.84);
    color: #9a6a2d;
  }

  .profit-mobile-item__actions :deep(.el-button--danger) {
    border-color: rgba(199, 105, 119, 0.28);
    background: rgba(251, 232, 228, 0.82);
    color: #b95662;
  }

  .profit-mobile-item__remark {
    font-size: 10px;
    line-height: 1.35;
  }
}
</style>
