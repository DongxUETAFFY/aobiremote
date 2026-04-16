<script setup lang="ts">
defineProps<{
  title: string
  description: string
  stage: string
}>()
</script>

<template>
  <section class="page-card ah-glass-card ah-page-section">
    <div class="page-card__header">
      <div>
        <p class="page-card__stage">{{ stage }}</p>
        <h2>{{ title }}</h2>
      </div>
      <span class="page-card__status">骨架已就位</span>
    </div>
    <p class="page-card__description">
      {{ description }}
    </p>
    <div class="page-card__blocks">
      <div class="page-card__block" />
      <div class="page-card__block is-large" />
      <div class="page-card__block" />
    </div>
  </section>
</template>

<style scoped>
.page-card__header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.page-card__stage {
  margin: 0 0 8px;
  color: #bb8d54;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.page-card__status {
  padding: 10px 14px;
  border-radius: 999px;
  background: rgba(255, 216, 107, 0.3);
  color: #8d6b18;
  font-size: 13px;
  white-space: nowrap;
}

.page-card h2 {
  margin: 0;
  font-size: 28px;
  color: var(--ah-title);
}

.page-card__description {
  margin: 14px 0 0;
  color: var(--ah-text);
}

.page-card__blocks {
  display: grid;
  grid-template-columns: repeat(3, minmax(0, 1fr));
  gap: 16px;
  margin-top: 22px;
}

.page-card__block {
  min-height: 130px;
  border-radius: 22px;
  background:
    linear-gradient(180deg, rgba(255, 255, 255, 0.8), rgba(255, 236, 244, 0.8)),
    rgba(255, 255, 255, 0.92);
  border: 1px solid rgba(216, 168, 183, 0.18);
}

.page-card__block.is-large {
  min-height: 180px;
}

@media (max-width: 900px) {
  .page-card__blocks {
    grid-template-columns: 1fr;
  }

  .page-card__header {
    flex-direction: column;
  }
}
</style>
