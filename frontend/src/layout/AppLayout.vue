<script setup lang="ts">
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { APP_UPDATED_AT, APP_VERSION } from '@/constants/app-meta'
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
const versionLabel = APP_VERSION
const updatedAtLabel = APP_UPDATED_AT

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
        <span class="layout-hero__orb layout-hero__orb--pink" aria-hidden="true"></span>
        <span class="layout-hero__orb layout-hero__orb--gold" aria-hidden="true"></span>
        <div class="layout-hero__actions">
          <div class="layout-hero__meta" aria-label="version-meta">
            <span class="layout-hero__meta-version">{{ versionLabel }}</span>
            <span class="layout-hero__meta-date">更新于 {{ updatedAtLabel }}</span>
          </div>
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
          <p class="layout-hero__tagline">记录每一次买入、卖出和公开展示</p>
        </div>
        <div class="layout-hero__stamp" aria-hidden="true">
          <span>OBI</span>
          <strong>账本</strong>
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
  padding: 30px 0 44px;
}

.layout-hero__card,
.layout-nav__card {
  display: flex;
  align-items: center;
}

.layout-hero__card {
  overflow: hidden;
  position: relative;
  justify-content: space-between;
  gap: 24px;
  min-height: 178px;
  padding: 38px 40px;
  background:
    linear-gradient(120deg, rgba(255, 249, 239, 0.96) 0%, rgba(255, 244, 249, 0.94) 48%, rgba(255, 236, 242, 0.9) 100%);
}

.layout-hero__card::before {
  position: absolute;
  right: -70px;
  bottom: -118px;
  width: 360px;
  height: 240px;
  content: '';
  border-radius: 54% 46% 0 0;
  background:
    radial-gradient(circle at 58% 34%, rgba(255, 255, 255, 0.7), transparent 18%),
    linear-gradient(135deg, rgba(255, 143, 177, 0.42), rgba(255, 216, 107, 0.32));
  transform: rotate(-8deg);
}

.layout-hero__card::after {
  position: absolute;
  left: 28px;
  right: 28px;
  bottom: 18px;
  height: 1px;
  content: '';
  background: linear-gradient(90deg, transparent, rgba(127, 54, 84, 0.24), transparent);
}

.layout-hero__orb {
  position: absolute;
  border-radius: 999px;
  pointer-events: none;
  filter: blur(0.2px);
}

.layout-hero__orb--pink {
  top: 24px;
  left: 34%;
  width: 12px;
  height: 12px;
  background: var(--ah-accent);
  box-shadow: 0 0 0 9px rgba(255, 143, 177, 0.12);
}

.layout-hero__orb--gold {
  right: 190px;
  bottom: 36px;
  width: 16px;
  height: 16px;
  background: var(--ah-highlight);
  box-shadow: 0 0 0 11px rgba(255, 216, 107, 0.16);
}

.layout-hero__actions {
  position: absolute;
  top: 18px;
  right: 22px;
  z-index: 2;
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  flex-wrap: wrap;
}

.layout-hero__meta {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  gap: 2px;
  text-align: right;
  color: #8b6376;
}

.layout-hero__meta-version {
  font-size: 12px;
  font-weight: 800;
  line-height: 1.1;
}

.layout-hero__meta-date {
  font-size: 11px;
  line-height: 1.1;
  color: var(--ah-muted);
}

.layout-hero__copy {
  position: relative;
  z-index: 1;
  max-width: 680px;
}

.layout-hero__eyebrow {
  margin: 0 0 8px;
  font-size: 13px;
  font-weight: 800;
  letter-spacing: 0.24em;
  text-transform: uppercase;
  color: var(--ah-accent-ink);
}

.layout-hero h1 {
  margin: 0;
  font-size: clamp(38px, 5vw, 58px);
  line-height: 1.02;
  letter-spacing: -0.04em;
  color: var(--ah-title);
  text-shadow: 0 12px 30px rgba(127, 54, 84, 0.12);
}

.layout-hero__tagline {
  margin: 14px 0 0;
  color: var(--ah-muted);
  font-size: 15px;
  font-weight: 700;
}

.layout-hero__stamp {
  position: absolute;
  right: 58px;
  bottom: 32px;
  z-index: 1;
  display: grid;
  place-items: center;
  width: 96px;
  height: 96px;
  border: 2px dashed rgba(127, 54, 84, 0.28);
  border-radius: 999px;
  color: rgba(127, 54, 84, 0.56);
  transform: rotate(10deg);
}

.layout-hero__stamp span {
  font-size: 12px;
  font-weight: 900;
  letter-spacing: 0.18em;
}

.layout-hero__stamp strong {
  margin-top: -34px;
  font-size: 24px;
  letter-spacing: 0.08em;
}

.layout-nav {
  margin-top: 18px;
}

.layout-nav__card {
  justify-content: flex-start;
  gap: 16px;
  padding: 10px;
  flex-wrap: wrap;
  background: rgba(255, 250, 241, 0.76);
  border-radius: 999px;
}

.layout-nav__links {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.layout-nav__item {
  position: relative;
  overflow: hidden;
  border: 0;
  border-radius: 999px;
  padding: 12px 20px;
  cursor: pointer;
  color: var(--ah-accent-ink);
  background: rgba(255, 255, 255, 0.58);
  font-weight: 800;
  transition: background 0.2s ease, box-shadow 0.2s ease, color 0.2s ease, transform 0.2s ease;
}

.layout-nav__item:hover {
  transform: translateY(-1px);
  background: rgba(255, 255, 255, 0.92);
}

.layout-nav__item.is-active {
  color: #fff;
  background:
    linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  box-shadow: 0 14px 28px rgba(240, 111, 154, 0.28);
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

  .layout-hero__stamp {
    right: 28px;
    bottom: 28px;
    width: 76px;
    height: 76px;
  }

  .layout-hero__stamp strong {
    margin-top: -28px;
    font-size: 19px;
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

  .layout-hero__meta {
    align-items: flex-end;
  }

  .layout-hero__account-chip {
    margin-left: auto;
  }

  .layout-nav__card {
    justify-content: flex-start;
  }
}

@media (max-width: 768px) {
  .layout-shell {
    padding: 4px 0 82px;
  }

  .layout-hero__card {
    gap: 3px;
    padding: 7px 10px;
    border-radius: 12px;
    min-height: auto;
  }

  .layout-hero__card::before,
  .layout-hero__card::after,
  .layout-hero__orb,
  .layout-hero__stamp,
  .layout-hero__tagline {
    display: none;
  }

  .layout-hero__actions {
    margin-bottom: 0;
    width: 100%;
    justify-content: space-between;
    align-items: flex-start;
  }

  .layout-hero__meta {
    gap: 1px;
    text-align: right;
  }

  .layout-hero__meta-version {
    font-size: 10px;
  }

  .layout-hero__meta-date {
    font-size: 9px;
  }

  .layout-hero__account-chip {
    min-height: 24px;
    padding: 2px 5px 2px 7px;
  }

  .layout-hero__user {
    max-width: 88px;
    font-size: 11px;
  }

  .layout-hero__action-link {
    padding: 4px 5px;
    font-size: 10px;
  }

  .layout-hero__eyebrow {
    margin-bottom: 2px;
    font-size: 9px;
    letter-spacing: 0.12em;
  }

  .layout-hero h1 {
    font-size: 16px;
    line-height: 1.12;
  }

  .layout-nav {
    position: fixed;
    left: 0;
    right: 0;
    bottom: 0;
    z-index: 200;
    width: 100%;
    margin: 0;
    padding: 8px 8px max(8px, env(safe-area-inset-bottom));
    background: rgba(255, 250, 247, 0.82);
    backdrop-filter: blur(18px);
    border-top: 1px solid rgba(205, 145, 168, 0.18);
  }

  .layout-nav__card {
    width: 100%;
    padding: 0;
    border: 0;
    border-radius: 0;
    background: transparent;
    box-shadow: none;
  }

  .layout-nav__links {
    width: 100%;
    display: grid;
    grid-template-columns: repeat(auto-fit, minmax(58px, 1fr));
    gap: 6px;
  }

  .layout-nav__item {
    width: 100%;
    min-height: 38px;
    padding: 8px 3px;
    border-radius: 13px;
    font-size: 11px;
    line-height: 1.15;
    background: rgba(255, 255, 255, 0.62);
  }

  .layout-nav__item.is-active {
    box-shadow: 0 8px 18px rgba(240, 111, 154, 0.2);
  }

  .layout-main {
    margin-top: 6px;
  }
}
</style>
