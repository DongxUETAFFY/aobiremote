<script setup lang="ts">
const props = withDefaults(defineProps<{
  modelValue: string
  ascValue: string
  descValue: string
  defaultValue?: string
  label?: string
}>(), {
  defaultValue: 'default',
  label: '价格排序',
})

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
  (e: 'change', value: string): void
}>()

const updateSort = (value: string) => {
  const nextValue = props.modelValue === value ? props.defaultValue : value
  emit('update:modelValue', nextValue)
  emit('change', nextValue)
}
</script>

<template>
  <div class="price-sort-toggle" role="group" :aria-label="label">
    <span class="price-sort-toggle__label">{{ label }}</span>
    <button
      class="price-sort-toggle__button"
      :class="{ 'price-sort-toggle__button--active': modelValue === ascValue }"
      type="button"
      :aria-pressed="modelValue === ascValue"
      @click="updateSort(ascValue)"
    >
      从低到高
    </button>
    <button
      class="price-sort-toggle__button"
      :class="{ 'price-sort-toggle__button--active': modelValue === descValue }"
      type="button"
      :aria-pressed="modelValue === descValue"
      @click="updateSort(descValue)"
    >
      从高到低
    </button>
  </div>
</template>

<style scoped>
.price-sort-toggle {
  display: inline-flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 7px;
}

.price-sort-toggle__label {
  color: var(--ah-muted);
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

.price-sort-toggle__button {
  min-height: 34px;
  border: 1px solid var(--ah-control-border);
  border-radius: 999px;
  background: var(--ah-control-bg);
  color: var(--ah-text);
  cursor: pointer;
  font-size: 13px;
  font-weight: 700;
  line-height: 1;
  padding: 8px 13px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.54);
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s, color 0.2s, transform 0.2s;
}

.price-sort-toggle__button:hover {
  transform: translateY(-1px);
  border-color: rgba(240, 111, 154, 0.45);
  background: var(--ah-control-hover);
}

.price-sort-toggle__button:focus-visible {
  outline: 0;
  box-shadow: var(--ah-focus-ring);
}

.price-sort-toggle__button--active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  box-shadow: 0 10px 20px rgba(240, 111, 154, 0.2);
}

@media (max-width: 640px) {
  .price-sort-toggle {
    width: 100%;
  }

  .price-sort-toggle__label {
    width: 100%;
  }

  .price-sort-toggle__button {
    flex: 1;
    min-width: 0;
    padding: 10px 8px;
  }
}
</style>
