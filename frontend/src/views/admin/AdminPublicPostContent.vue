<script setup lang="ts">
import { buildImagePreviewUrl } from '@/api/file'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import type { AdminPublicPost } from '@/types/admin'

defineProps<{
  loading: boolean
  items: AdminPublicPost[]
  formatDateTime: (value: string | null) => string
  channelLabel: (value: string) => string
  categoryLabel: (value: string) => string
  directionLabel: (value: string) => string
}>()

const emit = defineEmits<{
  (e: 'delete', item: AdminPublicPost): void
}>()
</script>

<template>
  <div v-if="loading && items.length === 0" class="admin-post-loading">
    <span class="admin-post-loading__spinner" />
    <p>加载中...</p>
  </div>

  <div v-else-if="items.length === 0" class="admin-post-empty">
    <p>暂无公开交易数据</p>
    <p class="admin-post-empty__sub">可以调整关键词后重新查询</p>
  </div>

  <div v-else class="admin-post-list">
    <article
      v-for="item in items"
      :key="item.id"
      class="admin-post-card ah-glass-card ah-page-section"
    >
      <div class="admin-post-card__thumb">
        <SquareImagePreview
          :preview-url="buildImagePreviewUrl(item.imageFileId)"
          empty-text="无图"
        />
      </div>

      <div class="admin-post-card__body">
        <div class="admin-post-card__header">
          <h3 class="admin-post-card__name">{{ item.itemName }}</h3>
          <span class="admin-post-card__direction" :class="`is-${item.direction}`">
            {{ directionLabel(item.direction) }}
          </span>
          <span class="admin-post-card__price">￥{{ item.price }}</span>
        </div>

        <div class="admin-post-card__meta">
          <span>{{ channelLabel(item.channel) }}</span>
          <span class="admin-post-card__dot">·</span>
          <span>{{ categoryLabel(item.category) }}</span>
          <span class="admin-post-card__dot">·</span>
          <span>{{ formatDateTime(item.createdAt) }}</span>
        </div>

        <div class="admin-post-card__publisher">
          发布用户：{{ item.userEmail || `用户 ${item.userId}` }}
        </div>

        <div class="admin-post-card__footer">
          <span class="admin-post-card__untrusted">不可信标记 {{ item.untrustedCount }}</span>
          <el-button
            type="danger"
            size="small"
            class="admin-post-card__delete"
            @click="emit('delete', item)"
          >
            删除
          </el-button>
        </div>
      </div>
    </article>
  </div>
</template>

<style scoped>
.admin-post-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 56px 24px;
  color: var(--ah-text);
}

.admin-post-loading__spinner {
  width: 34px;
  height: 34px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: admin-post-spin 0.8s linear infinite;
}

@keyframes admin-post-spin {
  to {
    transform: rotate(360deg);
  }
}

.admin-post-empty {
  padding: 56px 24px;
  border-radius: 24px;
  text-align: center;
  color: var(--ah-title);
  background: rgba(255, 255, 255, 0.7);
}

.admin-post-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

.admin-post-list {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(460px, 1fr));
  gap: 14px;
}

.admin-post-card {
  display: flex;
  gap: 14px;
  align-items: flex-start;
}

.admin-post-card.ah-page-section {
  padding: 18px;
}

.admin-post-card__thumb {
  flex-shrink: 0;
}

.admin-post-card__thumb :deep(.square-preview) {
  width: 112px;
  height: 112px;
  border-radius: 18px;
}

.admin-post-card__body {
  flex: 1;
  min-width: 0;
}

.admin-post-card__header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.admin-post-card__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 16px;
  line-height: 1.35;
  overflow-wrap: anywhere;
}

.admin-post-card__direction {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.admin-post-card__direction.is-sell {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.admin-post-card__direction.is-buy {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.admin-post-card__price {
  font-size: 16px;
  font-weight: 700;
  color: var(--ah-accent-deep);
}

.admin-post-card__meta {
  margin-top: 6px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
  color: var(--ah-text);
  font-size: 13px;
}

.admin-post-card__dot {
  color: #c9a0b0;
}

.admin-post-card__publisher {
  margin-top: 8px;
  color: #8d7080;
  font-size: 13px;
  overflow-wrap: anywhere;
}

.admin-post-card__footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.admin-post-card__untrusted {
  padding: 4px 10px;
  border-radius: 8px;
  background: rgba(207, 93, 117, 0.12);
  color: #c44d73;
  font-size: 13px;
}

@media (max-width: 768px) {
  .admin-post-list {
    grid-template-columns: 1fr;
  }

  .admin-post-card {
    flex-direction: column;
  }

  .admin-post-card__thumb :deep(.square-preview) {
    width: 100%;
    max-width: 220px;
    aspect-ratio: 1 / 1;
    height: auto;
  }
}
</style>
