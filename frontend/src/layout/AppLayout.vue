<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const navItems = [
  { label: '我的仓库', path: '/warehouse' },
  { label: '盈亏统计', path: '/profit' },
  { label: '公开交易区', path: '/public-zone' },
  { label: '关于', path: '/about' },
]

const currentTitle = computed(() => String(route.meta.title || appStore.appName))
const currentUserLabel = computed(() => authStore.user?.nickname || authStore.user?.email || '游客')

const goTo = (path: string) => {
  router.push(path)
}

const handleLogout = async () => {
  await authStore.logout()
  router.push('/auth/login')
}
</script>

<template>
  <div class="layout-shell">
    <header class="layout-hero ah-page-shell">
      <div class="layout-hero__card ah-glass-card">
        <div class="layout-hero__copy">
          <p class="layout-hero__eyebrow">Aobi Helper Web</p>
          <h1>{{ currentTitle }}</h1>
        </div>
      </div>
    </header>

    <nav class="layout-nav ah-page-shell">
      <div class="layout-nav__card ah-glass-card">
        <div class="layout-nav__links">
          <button
            v-for="item in navItems"
            :key="item.path"
            class="layout-nav__item"
            :class="{ 'is-active': route.path === item.path }"
            type="button"
            @click="goTo(item.path)"
          >
            {{ item.label }}
          </button>
        </div>
        <div class="layout-nav__account">
          <template v-if="authStore.isAuthenticated">
            <span class="layout-nav__user">{{ currentUserLabel }}</span>
            <button class="layout-nav__ghost" type="button" @click="goTo('/auth/change-password')">
              修改密码
            </button>
            <button class="layout-nav__ghost" type="button" @click="handleLogout">
              退出登录
            </button>
          </template>
          <template v-else>
            <button class="layout-nav__ghost" type="button" @click="goTo('/auth/login')">登录</button>
            <button class="layout-nav__item is-active" type="button" @click="goTo('/auth/register')">
              注册
            </button>
          </template>
        </div>
      </div>
    </nav>

    <main class="layout-main ah-page-shell">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.layout-shell {
  padding: 28px 0 40px;
}

.layout-hero__card,
.layout-nav__card {
  display: flex;
  align-items: center;
}

.layout-hero__card {
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
}

.layout-hero__eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  letter-spacing: 0.2em;
  text-transform: uppercase;
  color: #b27f93;
}

.layout-hero h1 {
  margin: 0;
  font-size: 40px;
  line-height: 1.1;
  color: var(--ah-title);
}

.layout-hero__desc {
  max-width: 620px;
  margin: 14px 0 0;
  color: var(--ah-text);
}

.layout-hero__meta {
  display: grid;
  gap: 10px;
  min-width: 260px;
}

.layout-hero__meta span {
  display: block;
  padding: 12px 16px;
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.68);
  border: 1px solid rgba(205, 145, 168, 0.18);
  color: #7b5a6b;
}

.layout-nav {
  margin-top: 18px;
}

.layout-nav__card {
  justify-content: space-between;
  gap: 16px;
  padding: 16px;
  flex-wrap: wrap;
}

.layout-nav__links,
.layout-nav__account {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.layout-nav__item {
  border: 0;
  border-radius: 999px;
  padding: 12px 18px;
  cursor: pointer;
  color: #8a6478;
  background: rgba(255, 255, 255, 0.8);
  transition: 0.2s ease;
}

.layout-nav__item.is-active {
  color: #fff;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  box-shadow: 0 12px 24px rgba(240, 111, 154, 0.26);
}

.layout-main {
  margin-top: 20px;
}

.layout-nav__ghost {
  border: 1px solid rgba(205, 145, 168, 0.28);
  border-radius: 999px;
  padding: 12px 18px;
  cursor: pointer;
  color: #7e6170;
  background: rgba(255, 255, 255, 0.78);
}

.layout-nav__user {
  font-size: 14px;
  color: #7e6170;
  padding-right: 6px;
}

@media (max-width: 960px) {
  .layout-hero__card {
    flex-direction: column;
    align-items: flex-start;
  }

  .layout-hero h1 {
    font-size: 30px;
  }

  .layout-hero__meta {
    width: 100%;
    min-width: 0;
  }

  .layout-nav__card {
    flex-direction: column;
    align-items: stretch;
  }

  .layout-nav__links,
  .layout-nav__account {
    justify-content: flex-start;
  }
}
</style>
