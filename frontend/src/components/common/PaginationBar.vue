<script setup lang="ts">
import { computed, ref, watch } from 'vue'

const props = defineProps<{
  current: number
  total: number
  pageSize?: number
}>()

const emit = defineEmits<{
  (e: 'change', page: number): void
}>()

const pageSize = computed(() => props.pageSize || 30)
const totalPages = computed(() => Math.max(1, Math.ceil(props.total / pageSize.value)))
const inputPage = ref(String(props.current))

watch(
  () => [props.current, totalPages.value],
  () => {
    inputPage.value = String(Math.min(props.current, totalPages.value))
  },
  { immediate: true },
)

const go = (page: number) => {
  if (page < 1 || page > totalPages.value || page === props.current) return
  emit('change', page)
}

const submitInput = () => {
  const parsed = Number.parseInt(inputPage.value.trim(), 10)
  if (!Number.isFinite(parsed)) {
    inputPage.value = String(props.current)
    return
  }
  const normalized = Math.min(Math.max(parsed, 1), totalPages.value)
  inputPage.value = String(normalized)
  go(normalized)
}
</script>

<template>
  <div class="pagination">
    <div class="pagination__status">
      第 {{ current }} / {{ totalPages }} 页
    </div>
    <button
      class="pagination__btn"
      :disabled="current === 1"
      title="上一页"
      @click="go(current - 1)"
    >
      <
    </button>
    <div class="pagination__jump">
      <span class="pagination__jump-label">跳转</span>
      <input
        v-model="inputPage"
        class="pagination__input"
        :disabled="totalPages === 1"
        inputmode="numeric"
        @blur="submitInput"
        @keyup.enter="submitInput"
      />
      <span class="pagination__jump-total">/ {{ totalPages }}</span>
    </div>

    <button
      class="pagination__btn"
      :disabled="current === totalPages"
      title="下一页"
      @click="go(current + 1)"
    >
      >
    </button>
  </div>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  padding: 16px 0;
  flex-wrap: wrap;
}

.pagination__status {
  color: #7e6170;
  font-size: 14px;
  font-weight: 700;
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

.pagination__jump {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 0 4px;
}

.pagination__jump-label,
.pagination__jump-total {
  color: #8d7080;
  font-size: 14px;
}

.pagination__input {
  width: 72px;
  height: 36px;
  padding: 0 10px;
  border: 1px solid rgba(205, 145, 168, 0.28);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: #7e6170;
  font-size: 14px;
  text-align: center;
  outline: none;
}

.pagination__input:disabled {
  opacity: 0.55;
  cursor: not-allowed;
}

.pagination__input:focus {
  border-color: rgba(240, 111, 154, 0.45);
  box-shadow: 0 0 0 3px rgba(240, 111, 154, 0.12);
}

@media (max-width: 600px) {
  .pagination {
    gap: 8px;
  }

  .pagination__status {
    width: 100%;
    text-align: center;
  }
}
</style>
