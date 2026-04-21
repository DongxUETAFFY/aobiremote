<script setup lang="ts">
defineProps<{
  modelValue: string
  options: { label: string; value: string }[]
  label?: string
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="form-option-button-group" role="group" :aria-label="label || '选项选择'">
    <button
      v-for="option in options"
      :key="option.value"
      class="form-option-button-group__button"
      :class="{ 'form-option-button-group__button--active': modelValue === option.value }"
      type="button"
      :aria-pressed="modelValue === option.value"
      @click="emit('update:modelValue', option.value)"
    >
      {{ option.label }}
    </button>
  </div>
</template>

<style scoped>
.form-option-button-group {
  display: flex;
  flex-wrap: wrap;
  gap: 8px;
  width: 100%;
}

.form-option-button-group__button {
  min-height: 38px;
  min-width: 86px;
  border: 1px solid var(--ah-control-border);
  border-radius: 999px;
  background: var(--ah-control-bg);
  color: var(--ah-text);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  padding: 10px 17px;
  box-shadow: inset 0 1px 0 rgba(255, 255, 255, 0.54);
  transition: background 0.2s, border-color 0.2s, box-shadow 0.2s, color 0.2s, transform 0.2s;
}

.form-option-button-group__button:hover {
  transform: translateY(-1px);
  border-color: rgba(240, 111, 154, 0.46);
  background: var(--ah-control-hover);
}

.form-option-button-group__button:focus-visible {
  outline: 0;
  box-shadow: var(--ah-focus-ring);
}

.form-option-button-group__button--active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  box-shadow: 0 10px 20px rgba(240, 111, 154, 0.2);
}

@media (max-width: 640px) {
  .form-option-button-group__button {
    flex: 1;
    min-width: 0;
    padding: 12px 10px;
  }
}
</style>
