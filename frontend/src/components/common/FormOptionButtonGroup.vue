<script setup lang="ts">
defineProps<{
  modelValue: string
  options: { label: string; value: string }[]
}>()

const emit = defineEmits<{
  (e: 'update:modelValue', value: string): void
}>()
</script>

<template>
  <div class="form-option-button-group">
    <button
      v-for="option in options"
      :key="option.value"
      class="form-option-button-group__button"
      :class="{ 'form-option-button-group__button--active': modelValue === option.value }"
      type="button"
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
  gap: 10px;
  width: 100%;
}

.form-option-button-group__button {
  min-width: 86px;
  border: 1px solid rgba(205, 145, 168, 0.3);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.78);
  color: var(--ah-text);
  cursor: pointer;
  font-size: 14px;
  font-weight: 700;
  line-height: 1;
  padding: 11px 18px;
  transition: background 0.2s, border-color 0.2s, color 0.2s, transform 0.2s;
}

.form-option-button-group__button:hover {
  transform: translateY(-1px);
  border-color: rgba(240, 111, 154, 0.46);
  background: rgba(255, 248, 251, 0.96);
}

.form-option-button-group__button--active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  box-shadow: 0 8px 18px rgba(240, 111, 154, 0.2);
}

@media (max-width: 640px) {
  .form-option-button-group__button {
    flex: 1;
    min-width: 0;
    padding: 12px 10px;
  }
}
</style>
