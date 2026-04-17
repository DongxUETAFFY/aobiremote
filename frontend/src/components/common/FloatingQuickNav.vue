<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const navItems = [
  { label: '仓', title: '我的仓库', path: '/warehouse' },
  { label: '卖', title: '盈亏统计', path: '/profit' },
  { label: '公', title: '公开交易区', path: '/public-zone' },
]
</script>

<template>
  <nav class="floating-quick-nav" aria-label="业务区快捷导航">
    <RouterLink
      v-for="item in navItems"
      :key="item.path"
      class="floating-quick-nav__item"
      :class="{ 'is-active': route.path === item.path }"
      :to="item.path"
      :title="item.title"
      :aria-label="item.title"
    >
      {{ item.label }}
    </RouterLink>
  </nav>
</template>

<style scoped>
.floating-quick-nav {
  position: fixed;
  right: max(28px, calc((100vw - var(--ah-shell-width)) / 2 + 16px));
  bottom: 96px;
  z-index: 100;
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.floating-quick-nav__item {
  width: 46px;
  height: 46px;
  display: grid;
  place-items: center;
  border-radius: 50%;
  border: 1px solid rgba(205, 145, 168, 0.28);
  background: rgba(255, 255, 255, 0.92);
  color: #8a6478;
  font-size: 18px;
  font-weight: 800;
  box-shadow: 0 8px 22px rgba(213, 160, 178, 0.22);
  transition: transform 0.2s ease, box-shadow 0.2s ease, background 0.2s ease, color 0.2s ease;
}

.floating-quick-nav__item:hover {
  transform: scale(1.08);
  background: #fff;
  color: var(--ah-accent-deep);
  box-shadow: 0 12px 30px rgba(240, 111, 154, 0.28);
}

.floating-quick-nav__item.is-active {
  border-color: transparent;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  box-shadow: 0 12px 30px rgba(240, 111, 154, 0.38);
}

@media (max-width: 768px) {
  .floating-quick-nav {
    display: none;
  }
}
</style>
