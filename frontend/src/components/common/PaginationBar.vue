<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  current: number
  total: number
  pageSize?: number
}>()

const emit = defineEmits<{
  (e: 'change', page: number): void
}>()

const pageSize = computed(() => props.pageSize || 20)
const totalPages = computed(() => Math.ceil(props.total / pageSize.value))

const pages = computed(() => {
  const cur = props.current
  const total = totalPages.value
  if (total <= 7) {
    return Array.from({ length: total }, (_, i) => i + 1)
  }
  const result: (number | '...')[] = []

  result.push(1)

  if (cur > 4) result.push('...')

  const start = Math.max(2, cur - 1)
  const end = Math.min(total - 1, cur + 1)

  for (let i = start; i <= end; i++) {
    result.push(i)
  }

  if (cur < total - 3) result.push('...')

  result.push(total)

  return result
})

const go = (page: number) => {
  if (page < 1 || page > totalPages.value || page === props.current) return
  emit('change', page)
}
</script>

<template>
  <div v-if="totalPages > 1" class="pagination">
    <button
      class="pagination__btn"
      :disabled="current === 1"
      title="首页"
      @click="go(1)"
    >
      ‹‹
    </button>
    <button
      class="pagination__btn"
      :disabled="current === 1"
      title="上一页"
      @click="go(current - 1)"
    >
      ‹
    </button>

    <template v-for="(p, idx) in pages" :key="idx">
      <button
        v-if="p !== '...'"
        class="pagination__btn"
        :class="{ 'is-active': p === current }"
        @click="go(p as number)"
      >
        {{ p }}
      </button>
      <span v-else class="pagination__ellipsis">…</span>
    </template>

    <button
      class="pagination__btn"
      :disabled="current === totalPages"
      title="下一页"
      @click="go(current + 1)"
    >
      ›
    </button>
    <button
      class="pagination__btn"
      :disabled="current === totalPages"
      title="末页"
      @click="go(totalPages)"
    >
      › ›
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 4px;
  padding: 16px 0;
  flex-wrap: wrap;
}

.pagination__btn {
  min-width: 36px;
  height: 36px;
  padding: 0 8px;
  border: 1px solid rgba(205, 145, 168, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #7e6170;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.pagination__btn:hover:not(:disabled) {
  background: rgba(255, 143, 177, 0.15);
  color: var(--ah-accent-deep);
  border-color: rgba(240, 111, 154, 0.4);
}

.pagination__btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}

.pagination__btn.is-active {
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  border-color: transparent;
  font-weight: 700;
  box-shadow: 0 4px 12px rgba(240, 111, 154, 0.28);
}

.pagination__ellipsis {
  color: #b8a0ac;
  padding: 0 4px;
  font-size: 14px;
  line-height: 36px;
}
</style>
