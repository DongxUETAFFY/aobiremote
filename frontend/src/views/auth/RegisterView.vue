<script setup lang="ts">
import { computed, onBeforeUnmount, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const sendingCode = ref(false)
const registering = ref(false)
const countdown = ref(0)
const timer = ref<number | null>(null)

const EMAIL_PATTERN = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
const VERIFICATION_CODE_PATTERN = /^\d{6}$/
const PASSWORD_RULE_PATTERN = /^(?=.*[A-Za-z])(?=.*\d).+$/

const form = reactive({
  email: '',
  code: '',
  password: '',
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

const validateEmail = () => {
  if (!form.email) {
    ElMessage.warning('请先输入邮箱')
    return false
  }
  if (!EMAIL_PATTERN.test(form.email.trim())) {
    ElMessage.warning('请输入正确的邮箱地址')
    return false
  }
  return true
}

const validateRegisterForm = () => {
  if (!validateEmail()) {
    return false
  }
  if (!form.code || !form.password) {
    ElMessage.warning('请填写完整注册信息')
    return false
  }
  if (!VERIFICATION_CODE_PATTERN.test(form.code.trim())) {
    ElMessage.warning('验证码必须是 6 位数字')
    return false
  }
  if (form.password.length < 8 || form.password.length > 64) {
    ElMessage.warning('密码长度必须在 8 到 64 位之间')
    return false
  }
  if (!PASSWORD_RULE_PATTERN.test(form.password)) {
    ElMessage.warning('密码必须同时包含字母和数字')
    return false
  }
  if (form.password !== form.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致')
    return false
  }
  return true
}

const handleSendCode = async () => {
  if (!validateEmail()) {
    return
  }

  sendingCode.value = true
  try {
    await authStore.sendRegisterCode({ email: form.email.trim() })
    ElMessage.success('验证码已发送')
    startCountdown()
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '发送验证码失败')
  } finally {
    sendingCode.value = false
  }
}

const handleSubmit = async () => {
  if (!validateRegisterForm()) {
    return
  }

  registering.value = true
  try {
    await authStore.register({
      email: form.email.trim(),
      code: form.code.trim(),
      password: form.password,
    })
    ElMessage.success('注册成功，请登录')
    router.push('/auth/login')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '注册失败')
  } finally {
    registering.value = false
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
      <h1>注册</h1>
      <p class="auth-card__desc">使用邮箱验证码完成注册，后续登录只需要密码。</p>

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

        <el-form-item label="密码">
          <el-input
            v-model="form.password"
            type="password"
            show-password
            placeholder="至少 8 位，包含字母和数字"
          />
        </el-form-item>

        <el-form-item label="确认密码">
          <el-input
            v-model="form.confirmPassword"
            type="password"
            show-password
            placeholder="再次输入密码"
          />
        </el-form-item>

        <el-button class="auth-form__submit" type="primary" :loading="registering" @click="handleSubmit">
          注册
        </el-button>
      </el-form>

      <div class="auth-card__actions">
        <button type="button" @click="router.push('/auth/login')">已有账号？去登录</button>
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
}
</style>
