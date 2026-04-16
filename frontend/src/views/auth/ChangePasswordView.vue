<script setup lang="ts">
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const authStore = useAuthStore()

const loading = ref(false)
const form = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: '',
})

const handleSubmit = async () => {
  if (!form.oldPassword || !form.newPassword || !form.confirmPassword) {
    ElMessage.warning('请填写完整信息')
    return
  }
  if (form.newPassword !== form.confirmPassword) {
    ElMessage.warning('两次输入的新密码不一致')
    return
  }

  loading.value = true
  try {
    await authStore.changePassword({
      oldPassword: form.oldPassword,
      newPassword: form.newPassword,
    })
    ElMessage.success('密码修改成功，请重新登录')
    router.push('/auth/login')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '修改密码失败')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <section class="auth-page">
    <div class="auth-card ah-glass-card">
      <p class="auth-card__eyebrow">账户体系</p>
      <h1>修改密码</h1>
      <p class="auth-card__desc">修改成功后会自动退出登录，需要重新使用新密码登录。</p>

      <el-form label-position="top" class="auth-form" @submit.prevent="handleSubmit">
        <el-form-item label="旧密码">
          <el-input v-model="form.oldPassword" type="password" show-password placeholder="请输入旧密码" />
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
        <el-button class="auth-form__submit" type="primary" :loading="loading" @click="handleSubmit">
          确认修改
        </el-button>
      </el-form>
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

.auth-form__submit {
  width: 100%;
  height: 44px;
}
</style>
