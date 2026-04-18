<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAppStore } from '@/stores/app'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const appStore = useAppStore()
const authStore = useAuthStore()

const navItems = computed(() => [
  { label: '我的仓库', path: '/warehouse' },
  { label: '盈亏统计', path: '/profit' },
  { label: '公开交易区', path: '/public-zone' },
  { label: '关于', path: '/about' },
  ...(authStore.user?.admin ? [{ label: '管理员端', path: '/admin' }] : []),
])

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
        <div class="layout-hero__actions">
          <template v-if="authStore.isAuthenticated">
            <div class="layout-hero__account-chip">
              <span class="layout-hero__user">{{ currentUserLabel }}</span>
              <span class="layout-hero__divider" aria-hidden="true"></span>
              <button
                class="layout-hero__action-link"
                type="button"
                @click="goTo('/auth/change-password')"
              >
                修改密码
              </button>
              <button class="layout-hero__action-link is-danger" type="button" @click="handleLogout">
                退出登录
              </button>
            </div>
          </template>
          <template v-else>
            <div class="layout-hero__account-chip">
              <button class="layout-hero__action-link" type="button" @click="goTo('/auth/login')">
                登录
              </button>
              <button class="layout-hero__action-link is-primary" type="button" @click="goTo('/auth/register')">
                注册
              </button>
            </div>
          </template>
        </div>
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
  position: relative;
  justify-content: space-between;
  gap: 24px;
  padding: 30px 34px;
}

.layout-hero__actions {
  position: absolute;
  top: 16px;
  right: 20px;
  display: flex;
  align-items: center;
  justify-content: flex-end;
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

.layout-nav {
  margin-top: 18px;
}

.layout-nav__card {
  justify-content: flex-start;
  gap: 16px;
  padding: 16px;
  flex-wrap: wrap;
}

.layout-nav__links {
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

.layout-hero__account-chip {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  min-height: 38px;
  padding: 5px 8px 5px 12px;
  border-radius: 999px;
  border: 1px solid rgba(205, 145, 168, 0.2);
  background:
    linear-gradient(135deg, rgba(255, 255, 255, 0.88) 0%, rgba(255, 245, 248, 0.86) 100%);
  box-shadow:
    0 14px 28px rgba(217, 154, 176, 0.12),
    inset 0 1px 0 rgba(255, 255, 255, 0.65);
}

.layout-hero__action-link {
  border: 0;
  border-radius: 999px;
  padding: 7px 10px;
  cursor: pointer;
  color: #8b6376;
  background: transparent;
  font-size: 12px;
  line-height: 1;
  transition: 0.2s ease;
}

.layout-hero__action-link:hover {
  color: var(--ah-accent-deep);
  background: rgba(255, 143, 177, 0.14);
}

.layout-hero__action-link.is-primary {
  color: #fff;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  box-shadow: 0 8px 18px rgba(240, 111, 154, 0.16);
}

.layout-hero__user {
  font-size: 12px;
  font-weight: 600;
  color: #7e6170;
  max-width: 132px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.layout-hero__divider {
  width: 1px;
  height: 16px;
  background: rgba(205, 145, 168, 0.24);
}

.layout-hero__action-link.is-danger:hover {
  color: #c65b7f;
  background: rgba(233, 120, 154, 0.12);
}

@media (max-width: 960px) {
  .layout-hero__card {
    flex-direction: column;
    align-items: flex-start;
  }

  .layout-hero h1 {
    font-size: 30px;
  }

  .layout-hero__actions {
    position: static;
    width: 100%;
    justify-content: flex-end;
    margin-bottom: 8px;
  }

  .layout-hero__account-chip {
    margin-left: auto;
  }

  .layout-nav__card {
    justify-content: flex-start;
  }
}
</style>
