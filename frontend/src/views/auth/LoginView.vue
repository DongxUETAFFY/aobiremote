<script setup lang="ts">
import { computed, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

interface SavedLoginAccount {
  email: string
  password: string
  lastLoginAt: string
}

const SAVED_LOGIN_ACCOUNTS_KEY = 'aobi-saved-login-accounts'
const MAX_SAVED_ACCOUNTS = 8

const route = useRoute()
const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const rememberPassword = ref(true)
const selectedSavedEmail = ref('')
const savedAccounts = ref<SavedLoginAccount[]>([])
const form = reactive({
  email: '',
  password: '',
})

const savedAccountOptions = computed(() =>
  savedAccounts.value.map((account) => ({
    label: account.email,
    value: account.email,
  })),
)

const normalizeEmail = (email: string) => email.trim().toLowerCase()

const loadSavedAccounts = () => {
  try {
    const raw = localStorage.getItem(SAVED_LOGIN_ACCOUNTS_KEY)
    if (!raw) {
      savedAccounts.value = []
      return
    }
    const parsed = JSON.parse(raw) as SavedLoginAccount[]
    savedAccounts.value = Array.isArray(parsed)
      ? parsed.filter((account) => account?.email && account?.password)
      : []
  } catch {
    savedAccounts.value = []
  }
}

const persistSavedAccounts = () => {
  localStorage.setItem(SAVED_LOGIN_ACCOUNTS_KEY, JSON.stringify(savedAccounts.value))
}

const saveSuccessfulLogin = () => {
  if (!rememberPassword.value) {
    return
  }
  const email = normalizeEmail(form.email)
  if (!email || !form.password) {
    return
  }

  const nextAccount: SavedLoginAccount = {
    email,
    password: form.password,
    lastLoginAt: new Date().toISOString(),
  }
  savedAccounts.value = [
    nextAccount,
    ...savedAccounts.value.filter((account) => normalizeEmail(account.email) !== email),
  ].slice(0, MAX_SAVED_ACCOUNTS)
  selectedSavedEmail.value = email
  persistSavedAccounts()
}

const handleSavedAccountChange = (email: string) => {
  const account = savedAccounts.value.find((item) => item.email === email)
  if (!account) {
    return
  }
  form.email = account.email
  form.password = account.password
  rememberPassword.value = true
}

const removeSavedAccount = (email: string) => {
  savedAccounts.value = savedAccounts.value.filter((account) => account.email !== email)
  if (selectedSavedEmail.value === email) {
    selectedSavedEmail.value = ''
  }
  if (normalizeEmail(form.email) === normalizeEmail(email)) {
    form.email = ''
    form.password = ''
  }
  persistSavedAccounts()
}

loadSavedAccounts()

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
    saveSuccessfulLogin()
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
        <el-form-item v-if="savedAccounts.length" label="已保存账号">
          <div class="saved-login-row">
            <el-select
              v-model="selectedSavedEmail"
              class="saved-login-row__select"
              placeholder="选择登录过的账号"
              filterable
              @change="handleSavedAccountChange"
            >
              <el-option
                v-for="account in savedAccountOptions"
                :key="account.value"
                :label="account.label"
                :value="account.value"
              />
            </el-select>
            <el-button
              :disabled="!selectedSavedEmail"
              plain
              type="danger"
              @click="removeSavedAccount(selectedSavedEmail)"
            >
              删除
            </el-button>
          </div>
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="name@example.com" />
        </el-form-item>
        <el-form-item label="密码">
          <el-input v-model="form.password" type="password" show-password placeholder="请输入密码" />
        </el-form-item>
        <div class="auth-form__remember">
          <el-checkbox v-model="rememberPassword">登录成功后记住这个账号和密码</el-checkbox>
          <p>仅保存在当前浏览器，适合自己的设备使用。</p>
        </div>
        <el-button class="auth-form__submit" type="primary" :loading="loading" @click="handleSubmit">
          登录
        </el-button>
      </el-form>

      <div class="auth-card__actions">
        <button type="button" @click="router.push('/auth/forgot-password')">忘记密码？去找回</button>
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

.saved-login-row {
  width: 100%;
  display: flex;
  gap: 10px;
}

.saved-login-row__select {
  flex: 1;
}

.auth-form__remember {
  margin: -2px 0 18px;
}

.auth-form__remember p {
  margin: 4px 0 0;
  color: var(--ah-muted);
  font-size: 12px;
}

.auth-form__submit {
  width: 100%;
  height: 44px;
}

.auth-card__actions {
  margin-top: 18px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.auth-card__actions button {
  border: 0;
  background: transparent;
  color: var(--ah-accent-deep);
  cursor: pointer;
  padding: 0;
}

@media (max-width: 640px) {
  .saved-login-row {
    flex-direction: column;
  }

  .auth-card__actions {
    justify-content: flex-start;
  }
}
</style>
