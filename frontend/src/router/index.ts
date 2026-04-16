import { createRouter, createWebHistory } from 'vue-router'
import pinia from '@/stores'
import { useAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      redirect: '/warehouse',
    },
    {
      path: '/',
      component: () => import('@/layout/AppLayout.vue'),
      children: [
        {
          path: 'warehouse',
          name: 'warehouse',
          component: () => import('@/views/warehouse/WarehouseView.vue'),
          meta: { title: '我的仓库', requiresAuth: true },
        },
        {
          path: 'profit',
          name: 'profit',
          component: () => import('@/views/profit/ProfitView.vue'),
          meta: { title: '盈亏统计', requiresAuth: true },
        },
        {
          path: 'public-zone',
          name: 'public-zone',
          component: () => import('@/views/public-zone/PublicZoneView.vue'),
          meta: { title: '公开交易区' },
        },
        {
          path: 'about',
          name: 'about',
          component: () => import('@/views/about/AboutView.vue'),
          meta: { title: '关于' },
        },
      ],
    },
    {
      path: '/auth/login',
      name: 'login',
      component: () => import('@/views/auth/LoginView.vue'),
      meta: { title: '登录', guestOnly: true },
    },
    {
      path: '/auth/register',
      name: 'register',
      component: () => import('@/views/auth/RegisterView.vue'),
      meta: { title: '注册', guestOnly: true },
    },
    {
      path: '/auth/change-password',
      name: 'change-password',
      component: () => import('@/views/auth/ChangePasswordView.vue'),
      meta: { title: '修改密码', requiresAuth: true },
    },
    {
      path: '/:pathMatch(.*)*',
      name: 'not-found',
      component: () => import('@/views/not-found/NotFoundView.vue'),
      meta: { title: '页面不存在' },
    },
  ],
  scrollBehavior() {
    return { top: 0 }
  },
})

router.beforeEach(async (to) => {
  const authStore = useAuthStore(pinia)
  await authStore.initialize()

  if (to.meta.requiresAuth && !authStore.isAuthenticated) {
    return {
      path: '/auth/login',
      query: {
        redirect: to.fullPath,
      },
    }
  }

  if (to.meta.guestOnly && authStore.isAuthenticated) {
    return '/warehouse'
  }

  return true
})

router.afterEach((to) => {
  const title = to.meta.title ? `${String(to.meta.title)} - Aobi Helper Web` : 'Aobi Helper Web'
  document.title = title
})

export default router
