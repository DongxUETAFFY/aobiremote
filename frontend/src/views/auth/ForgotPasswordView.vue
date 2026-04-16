<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const sendingCode = ref(false)
const resetting = ref(false)
const countdown = ref(0)
const timer = ref<number | null>(null)

const form = reactive({
  email: '',
  code: '',
  newPassword: '',
  confirmPassword: '',
})

const canSendCode = computed(() => countdown.value === 0 && !sendingCode.value)

const startCountdown = () => {
  countdown.value = 60
  timer.value = window.setInterval(() => {
    countdown.value -= 1
    if (countdown.value <= 0 && timer.value) {
      window.clearInterval(timer.value)
      timer.value = null
      countdown.value = 0
    }
  }, 1000)
}

const handleSendCode = async () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return
  }

  sendingCode.value = true
  try {
    await authStore.sendResetPasswordCode({ email: form.email })
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  if (!form.email || !form.code || !form.newPassword) {
    ElMessage.warning('请填写完整找回信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return
  }

  resetting.value = true
  try {
    await authStore.resetPassword({
      email: form.email,
      code: form.code,
      newPassword: form.newPassword,
    })
    ElMessage.success('密码已重置，请重新登录')
    router.push('/auth/login')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '重置密码失败')
  } finally {
    resetting.value = false
  }
}

onBeforeUnmount(() => {
  if (timer.value) {
    window.clearInterval(timer.value)
  }
})
</script>

<template>
  <section class="auth-page">
    <div class="auth-card ah-glass-card">
      <p class="auth-card__eyebrow">账户体系</p>
      <h1>忘记密码</h1>
      <p class="auth-card__desc">通过邮箱验证码重置密码，完成后用新密码重新登录。</p>

      <el-form label-position="top" class="auth-form" @submit.prevent="handleSubmit">
        <el-form-item label="邮箱">
          <el-input v-model="form.email" placeholder="name@example.com" />
        </el-form-item>

        <el-form-item label="验证码">
          <div class="auth-inline">
            <el-input v-model="form.code" maxlength="6" placeholder="请输入 6 位验证码" />
            <el-button :disabled="!canSendCode" :loading="sendingCode" @click="handleSendCode">
              {{ countdown > 0 ? `${countdown}s` : '发送验证码' }}
            </el-button>
          </div>
        </el-form-item>

        <el-form-item label="新密码">
          <el-input
            v-model="form.newPassword"
            type="password"
            show-password
            placeholder="至少 8 位，包含字母和数字"
          />
        </el-form-item>

        <el-form-item label="确认新密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入新密码"
          />
        </el-form-item>

        <el-button class="auth-form__submit" type="primary" :loading="resetting" @click="handleSubmit">
          重置密码
        </el-button>
      </el-form>

      <div class="auth-card__actions">
        <button type="button" @click="router.push('/auth/login')">想起密码了？去登录</button>
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
  width: min(560px, 100%);
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

.auth-inline {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 132px;
  gap: 12px;
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
  .auth-inline {
    grid-template-columns: 1fr;
  }

  .auth-card__actions {
    justify-content: flex-start;
  }
}
</style>
