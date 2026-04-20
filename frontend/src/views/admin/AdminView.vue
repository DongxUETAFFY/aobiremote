<script setup lang="ts">
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import PaginationBar from '@/components/common/PaginationBar.vue'
import AdminPublicPostContent from '@/views/admin/AdminPublicPostContent.vue'
import {
  deleteAdminPublicPost,
  getAdminOverview,
  getAdminPublicPosts,
  getAdminUsers,
  updateAdminUserStatus,
} from '@/api/admin'
import type { AdminOverview, AdminPublicPost, AdminUser } from '@/types/admin'

const PAGE_SIZE = 30

const activeTab = ref<'users' | 'posts'>('users')
const loading = ref(false)
const overviewLoading = ref(false)
const overview = ref<AdminOverview | null>(null)

const users = ref<AdminUser[]>([])
const userTotal = ref(0)
const userPage = ref(1)
const userFilters = reactive({
  keyword: '',
  status: '',
})

const posts = ref<AdminPublicPost[]>([])
const postTotal = ref(0)
const postPage = ref(1)
const postKeyword = ref('')

const formatBytes = (bytes: number | undefined) => {
  const value = bytes ?? 0
  if (value <= 0) {
    return '0 B'
  }

  const units = ['B', 'KB', 'MB', 'GB', 'TB']
  const unitIndex = Math.min(Math.floor(Math.log(value) / Math.log(1024)), units.length - 1)
  const size = value / 1024 ** unitIndex
  return `${size >= 100 ? size.toFixed(0) : size.toFixed(1)} ${units[unitIndex]}`
}

const overviewCards = computed(() => {
  const data = overview.value
  return [
    { label: '当前 QPS', value: (data?.qps ?? 0).toFixed(2) },
    { label: '用户总数', value: data?.totalUsers ?? 0 },
    { label: '活跃用户', value: data?.activeUsers ?? 0 },
    { label: '禁用用户', value: data?.disabledUsers ?? 0 },
    { label: '今日新增用户', value: data?.todayNewUsers ?? 0 },
    { label: '库存物品总数', value: data?.totalInventoryItems ?? 0 },
    { label: '公开交易总数', value: data?.totalPublicPosts ?? 0 },
    { label: '今日公开交易', value: data?.todayPublicPosts ?? 0 },
    { label: '不可信标记总数', value: data?.totalUntrustedCount ?? 0 },
    { label: '图片总数', value: data?.totalImageCount ?? data?.totalFiles ?? 0 },
    { label: '图片占用空间', value: formatBytes(data?.totalImageBytes) },
    { label: '总存储空间', value: formatBytes(data?.totalStorageBytes) },
    { label: '可用存储空间', value: formatBytes(data?.usableStorageBytes) },
  ]
})

const loadOverview = async () => {
  overviewLoading.value = true
  try {
    const resp = await getAdminOverview()
    overview.value = resp.data
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '管理员概览加载失败')
  } finally {
    overviewLoading.value = false
  }
}

const loadUsers = async (page = 1) => {
  loading.value = true
  userPage.value = page
  try {
    const resp = await getAdminUsers({
      pageNo: page,
      pageSize: PAGE_SIZE,
      keyword: userFilters.keyword.trim() || undefined,
      status: userFilters.status || undefined,
    })
    users.value = resp.data.items
    userTotal.value = resp.data.totalCount
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '用户列表加载失败')
  } finally {
    loading.value = false
  }
}

const loadPosts = async (page = 1) => {
  loading.value = true
  postPage.value = page
  try {
    const resp = await getAdminPublicPosts({
      pageNo: page,
      pageSize: PAGE_SIZE,
      keyword: postKeyword.value.trim() || undefined,
    })
    posts.value = resp.data.items
    postTotal.value = resp.data.totalCount
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '公开交易列表加载失败')
  } finally {
    loading.value = false
  }
}

const handleTabChange = async () => {
  if (activeTab.value === 'users') {
    await loadUsers(1)
    return
  }

  await loadPosts(1)
}

const handleToggleUserStatus = async (user: AdminUser) => {
  const nextStatus = user.status === 'active' ? 'disabled' : 'active'
  const actionText = nextStatus === 'disabled' ? '禁用' : '启用'

  try {
    await ElMessageBox.confirm(
      `确认要${actionText}用户 ${user.email} 吗？`,
      `${actionText}用户`,
      {
        type: 'warning',
        confirmButtonText: actionText,
        cancelButtonText: '取消',
      },
    )

    await updateAdminUserStatus(user.id, nextStatus)
    ElMessage.success(`${actionText}成功`)
    await Promise.all([loadOverview(), loadUsers(userPage.value)])
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || `${actionText}失败`)
    }
  }
}

const handleDeletePost = async (post: AdminPublicPost) => {
  try {
    await ElMessageBox.confirm(
      `确认要删除公开交易 ${post.itemName} 吗？`,
      '删除公开交易',
      {
        type: 'warning',
        confirmButtonText: '删除',
        cancelButtonText: '取消',
      },
    )

    await deleteAdminPublicPost(post.id)
    ElMessage.success('删除成功')
    await Promise.all([loadOverview(), loadPosts(postPage.value)])
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const formatDateTime = (value: string | null) => {
  if (!value) return '-'
  return value.replace('T', ' ').replace(/\.\d+$/, '')
}

const channelLabel = (value: string) => {
  const map: Record<string, string> = {
    xianyu: '闲鱼',
    tieba: '贴吧',
    other: '其他',
  }
  return map[value] || value
}

const categoryLabel = (value: string) => {
  const map: Record<string, string> = {
    obi: '奥比岛',
    magic: '魔力时装',
  }
  return map[value] || value
}

const directionLabel = (value: string) => (value === 'buy' ? '收' : '出')

onMounted(async () => {
  await loadOverview()
  await loadUsers()
})
</script>

<template>
  <div class="admin-page">
    <section class="admin-hero ah-glass-card ah-page-section">
      <div>
        <p class="admin-hero__eyebrow">Admin Console</p>
        <h2>管理员后台</h2>
        <p class="admin-hero__desc">用于查看全站数据、管理用户状态，以及处理公开交易内容。</p>
      </div>
      <el-button :loading="overviewLoading" @click="loadOverview">刷新概览</el-button>
    </section>

    <section class="admin-overview">
      <article v-for="card in overviewCards" :key="card.label" class="admin-stat ah-glass-card">
        <span>{{ card.label }}</span>
        <strong>{{ card.value }}</strong>
      </article>
    </section>

    <section class="admin-panel ah-glass-card ah-page-section">
      <el-tabs v-model="activeTab" @tab-change="handleTabChange">
        <el-tab-pane label="用户管理" name="users">
          <div class="admin-toolbar">
            <el-input
              v-model="userFilters.keyword"
              clearable
              placeholder="按邮箱或昵称搜索"
              @keyup.enter="loadUsers(1)"
              @clear="loadUsers(1)"
            />
            <el-select
              v-model="userFilters.status"
              clearable
              placeholder="用户状态"
              @change="loadUsers(1)"
            >
              <el-option label="启用" value="active" />
              <el-option label="禁用" value="disabled" />
            </el-select>
            <el-button type="primary" @click="loadUsers(1)">搜索</el-button>
          </div>

          <el-table v-loading="loading" :data="users" class="admin-table">
            <el-table-column prop="id" label="ID" width="82" />
            <el-table-column prop="email" label="邮箱" min-width="210" />
            <el-table-column prop="nickname" label="昵称" min-width="140" />
            <el-table-column label="角色" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.admin" type="warning">管理员</el-tag>
                <el-tag v-else>用户</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag :type="row.status === 'active' ? 'success' : 'danger'">
                  {{ row.status === 'active' ? '启用' : '禁用' }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column label="最近登录" min-width="180">
              <template #default="{ row }">{{ formatDateTime(row.lastLoginAt) }}</template>
            </el-table-column>
            <el-table-column label="创建时间" min-width="180">
              <template #default="{ row }">{{ formatDateTime(row.createdAt) }}</template>
            </el-table-column>
            <el-table-column label="操作" width="120" fixed="right">
              <template #default="{ row }">
                <el-button
                  size="small"
                  :type="row.status === 'active' ? 'danger' : 'success'"
                  :disabled="row.admin"
                  @click="handleToggleUserStatus(row)"
                >
                  {{ row.status === 'active' ? '禁用' : '启用' }}
                </el-button>
              </template>
            </el-table-column>
          </el-table>

          <PaginationBar :current="userPage" :total="userTotal" :page-size="PAGE_SIZE" @change="loadUsers" />
        </el-tab-pane>

        <el-tab-pane label="公开交易管理" name="posts">
          <div class="admin-toolbar">
            <el-input
              v-model="postKeyword"
              clearable
              placeholder="按物品名或发布者搜索"
              @keyup.enter="loadPosts(1)"
              @clear="loadPosts(1)"
            />
            <el-button type="primary" @click="loadPosts(1)">搜索</el-button>
          </div>

          <AdminPublicPostContent
            :loading="loading"
            :items="posts"
            :format-date-time="formatDateTime"
            :channel-label="channelLabel"
            :category-label="categoryLabel"
            :direction-label="directionLabel"
            @delete="handleDeletePost"
          />

          <PaginationBar :current="postPage" :total="postTotal" :page-size="PAGE_SIZE" @change="loadPosts" />
        </el-tab-pane>
      </el-tabs>
    </section>
  </div>
</template>

<style scoped>
.admin-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding-bottom: 40px;
}

.admin-hero {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 16px;
}

.admin-hero__eyebrow {
  margin: 0 0 8px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.14em;
  text-transform: uppercase;
}

.admin-hero h2 {
  margin: 0;
  color: var(--ah-title);
  font-size: 30px;
}

.admin-hero__desc {
  margin: 10px 0 0;
  color: var(--ah-text);
}

.admin-overview {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 12px;
}

.admin-stat {
  padding: 18px;
  border-radius: 22px;
}

.admin-stat span {
  display: block;
  color: #8d7080;
  font-size: 13px;
  font-weight: 700;
}

.admin-stat strong {
  display: block;
  margin-top: 8px;
  color: var(--ah-title);
  font-size: 28px;
  line-height: 1;
}

.admin-panel {
  overflow: hidden;
}

.admin-toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 14px;
  flex-wrap: wrap;
}

.admin-toolbar :deep(.el-input) {
  width: 260px;
}

.admin-toolbar :deep(.el-select) {
  width: 140px;
}

.admin-table {
  width: 100%;
}

@media (max-width: 768px) {
  .admin-hero {
    flex-direction: column;
  }

  .admin-toolbar :deep(.el-input),
  .admin-toolbar :deep(.el-select),
  .admin-toolbar :deep(.el-button) {
    width: 100%;
  }
}
</style>
