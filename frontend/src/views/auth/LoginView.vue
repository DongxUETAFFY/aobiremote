<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  email: '',
  password: '',
})

const handleSubmit = async () => {
  if (!form.email || !form.password) {
    ElMessage.warning('请输入邮箱和密码')
    return
  }

  loading.value = true
  try {
    await authStore.login({
      email: form.email,
      password: form.password,
    })
    ElMessage.success('登录成功')
    const redirect = typeof route.query.redirect === 'string' ? route.query.redirect : '/warehouse'
    router.push(redirect)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '登录失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card ah-glass-card">
      <p class="auth-card__eyebrow">账户体系</p>
      <h1>登录</h1>
      <p class="auth-card__desc">使用邮箱和密码登录，进入仓库、盈亏统计和个人功能。</p>

      <el-form label-position="top" class="auth-form" @submit.prevent="handleSubmit">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <el-button class="auth-form__submit" type="primary" :loading="loading" @click="handleSubmit">
          登录
        </el-button>
      </el-form>

      <div class="auth-card__actions">
        <button type="button" @click="router.push('/auth/register')">没有账号？去注册</button>
      </div>
    </div>
  </section>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: grid;
  place-items: center;
  padding: 24px;
}

.auth-card {
  width: min(540px, 100%);
  padding: 34px;
}

.auth-card__eyebrow {
  margin: 0 0 10px;
  color: #b27f93;
  letter-spacing: 0.14em;
  text-transform: uppercase;
  font-size: 13px;
}

.auth-card h1 {
  margin: 0;
  color: var(--ah-title);
}

.auth-card__desc {
  margin-top: 10px;
  color: var(--ah-text);
}

.auth-form {
  margin-top: 22px;
}

.auth-form__submit {
  width: 100%;
  height: 44px;
}

.auth-card__actions {
  margin-top: 18px;
}

.auth-card__actions button {
  border: 0;
  background: transparent;
  color: var(--ah-accent-deep);
  cursor: pointer;
  padding: 0;
}
</style>
