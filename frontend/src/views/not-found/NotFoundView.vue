<script setup lang="ts">
import { useRouter } from 'vue-router'

const router = useRouter()
</script>

<template>
  <section class="not-found-page">
    <div class="not-found-card ah-glass-card">
      <p class="not-found-card__code">404</p>
      <h1>页面不存在</h1>
      <p>当前地址没有对应页面，后续如果是受保护页面也会在这里补权限判断。</p>
      <button type="button" @click="router.push('/warehouse')">返回我的仓库</button>
    </div>
  </section>
</template>

<style scoped>
.not-found-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.not-found-card {
  width: min(520px, 100%);
  padding: 34px;
  text-align: center;
}

.not-found-card__code {
  margin: 0;
  font-size: 64px;
  font-weight: 800;
  color: var(--ah-accent-deep);
}

.not-found-card h1 {
  margin: 8px 0 10px;
  color: var(--ah-title);
}

.not-found-card p {
  margin: 0;
  color: var(--ah-text);
}

.not-found-card button {
  margin-top: 18px;
  border: 0;
  border-radius: 999px;
  padding: 12px 18px;
  cursor: pointer;
  color: #fff;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
}
</style>
