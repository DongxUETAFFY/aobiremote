<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import PaginationBar from '@/components/common/PaginationBar.vue'
import { uploadImage } from '@/api/file'
import {
  compressImageBeforeUpload,
  formatFileSize,
  type CompressionResult,
} from '@/utils/image-upload'
import {
  createPublicPost,
  deletePublicPost,
  getPublicPostPage,
  togglePublicPostUntrusted,
  updatePublicPost,
} from '@/api/public-post'
import { useAuthStore } from '@/stores/auth'
import type {
  PublicPostCategory,
  PublicPostChannel,
  PublicPostCreateRequest,
  PublicPostListItem,
} from '@/types/public-post'

const authStore = useAuthStore()

// --- State ---
const loading = ref(false)
const items = ref<PublicPostListItem[]>([])
const totalCount = ref(0)
const hasMore = ref(false)
const currentPage = ref(1)

// Filter
const filterScope = ref<'all' | 'mine'>('all')
const filterDirection = ref<'all' | 'buy' | 'sell'>('all')
const filterChannel = ref<PublicPostChannel | 'all'>('all' as PublicPostChannel | 'all')
const filterCategory = ref<PublicPostCategory | 'all'>('all' as PublicPostCategory | 'all')
const filterKeyword = ref('')
const filterMinPrice = ref<number | null>(null)
const filterMaxPrice = ref<number | null>(null)

// Form dialog
const dialogVisible = ref(false)
const dialogTitle = ref('发布交易')
const editingId = ref<number | null>(null)
const formLoading = ref(false)

const channelOptions: { label: string; value: PublicPostChannel | 'all' }[] = [
  { label: '全部渠道', value: 'all' },
  { label: '闲鱼', value: 'xianyu' },
  { label: '贴吧', value: 'tieba' },
  { label: '其他', value: 'other' },
]

const categoryOptions: { label: string; value: PublicPostCategory | 'all' }[] = [
  { label: '全部分类', value: 'all' },
  { label: '奥比时装', value: 'obi' },
  { label: '魔力时装', value: 'magic' },
]

const form = reactive<PublicPostCreateRequest>({
  itemName: '',
  price: 0,
  tradeTime: '',
  direction: 'sell',
  channel: 'xianyu',
  category: 'obi',
  remark: '',
  imageFileId: '',
})

// Image upload state
const fileInputRef = ref<HTMLInputElement | null>(null)
const compressionResult = ref<CompressionResult | null>(null)
const uploadingImage = ref(false)
const uploadProgress = ref('')

const compressionSummary = computed(() => {
  if (!compressionResult.value) return ''
  const { originalSize, compressedSize, compressed } = compressionResult.value
  if (!compressed) {
    return `原图已在 400KB 内，大小 ${formatFileSize(originalSize)}`
  }
  return `原图 ${formatFileSize(originalSize)}，压缩后 ${formatFileSize(compressedSize)}`
})

// --- Methods ---
const loadData = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const params: Record<string, any> = { pageNo: page, pageSize: 20 }
    if (filterScope.value !== 'all') params.scope = filterScope.value
    if (filterDirection.value !== 'all') params.direction = filterDirection.value
    if (filterChannel.value !== 'all') params.channel = filterChannel.value
    if (filterCategory.value !== 'all') params.category = filterCategory.value
    if (filterKeyword.value.trim()) params.keyword = filterKeyword.value.trim()
    if (filterMinPrice.value != null) params.minPrice = String(filterMinPrice.value)
    if (filterMaxPrice.value != null) params.maxPrice = String(filterMaxPrice.value)

    const resp = await getPublicPostPage(params)
    if (page === 1) {
      items.value = resp.data.items
    } else {
      items.value.push(...resp.data.items)
    }
    totalCount.value = resp.data.totalCount
    hasMore.value = resp.data.hasMore
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleFilterChange = () => {
  currentPage.value = 1
  loadData(1)
}

const handleKeywordSearch = () => {
  if (filterMinPrice.value != null && filterMaxPrice.value != null && filterMinPrice.value > filterMaxPrice.value) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }
  currentPage.value = 1
  loadData(1)
}

const handleKeywordClear = () => {
  filterKeyword.value = ''
  currentPage.value = 1
  loadData(1)
}

const handlePriceSearch = () => {
  if (filterMinPrice.value != null && filterMaxPrice.value != null && filterMinPrice.value > filterMaxPrice.value) {
    ElMessage.warning('最低价不能大于最高价')
    return
  }
  currentPage.value = 1
  loadData(1)
}

const handlePriceClear = () => {
  filterMinPrice.value = null
  filterMaxPrice.value = null
  currentPage.value = 1
  loadData(1)
}

const handlePageChange = (page: number) => {
  loadData(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const openAddDialog = () => {
  dialogTitle.value = '发布交易'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (item: PublicPostListItem) => {
  dialogTitle.value = '编辑记录'
  editingId.value = item.id
  form.itemName = item.itemName
  form.price = item.price
  form.tradeTime = item.tradeTime
  form.direction = item.direction
  form.channel = item.channel
  form.category = item.category
  form.remark = item.remark || ''
  form.imageFileId = item.imageFileId || ''
  compressionResult.value = null
  uploadProgress.value = ''
  dialogVisible.value = true
}

const resetForm = () => {
  form.itemName = ''
  form.price = 0
  form.tradeTime = new Date().toISOString().split('T')[0]
  form.direction = 'sell'
  form.channel = 'xianyu'
  form.category = 'obi'
  form.remark = ''
  form.imageFileId = ''
  compressionResult.value = null
  uploadProgress.value = ''
}

const handlePickImage = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  if (!file) return

  try {
    compressionResult.value = await compressImageBeforeUpload(file)
    ElMessage.success('图片已处理完成，可以保存了')
  } catch (error: any) {
    compressionResult.value = null
    ElMessage.error(error?.message || '图片处理失败')
  } finally {
    input.value = ''
  }
}

const handleUploadImage = async () => {
  if (!compressionResult.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  uploadingImage.value = true
  try {
    const resp = await uploadImage(compressionResult.value.file, 'public')
    form.imageFileId = resp.data.fileId
    uploadProgress.value = `上传成功，fileId: ${resp.data.fileId}`
    ElMessage.success('图片上传成功')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
  }
}

const handleSubmitForm = async () => {
  if (!form.itemName.trim()) {
    ElMessage.warning('请输入物品名称')
    return
  }
  if (!form.price || form.price <= 0) {
    ElMessage.warning('请输入有效的价格')
    return
  }
  if (!form.tradeTime) {
    ElMessage.warning('请选择交易时间')
    return
  }

  formLoading.value = true
  try {
    const payload = {
      ...form,
      requestId: `req_${Date.now()}`,
    }

    if (editingId.value) {
      await updatePublicPost(editingId.value, { ...payload })
      ElMessage.success('更新成功')
    } else {
      await createPublicPost(payload)
      ElMessage.success('发布成功')
    }
    dialogVisible.value = false
    await loadData(1)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (item: PublicPostListItem) => {
  try {
    await ElMessageBox.confirm(`确定删除「${item.itemName}」吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deletePublicPost(item.id, `req_${Date.now()}`)
    ElMessage.success('删除成功')
    await loadData(1)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const handleToggleUntrusted = async (item: PublicPostListItem) => {
  try {
    const resp = await togglePublicPostUntrusted(item.id, `req_${Date.now()}`)
    item.untrusted = resp.data.flagged
    item.untrustedCount = resp.data.untrustedCount
    ElMessage.success(item.untrusted ? '已标记不可信' : '已取消不可信')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  }
}

const channelLabel = (ch: PublicPostChannel) => channelOptions.find((o) => o.value === ch)?.label || ch
const categoryLabel = (cat: PublicPostCategory) => categoryOptions.find((o) => o.value === cat)?.label || cat
const formatDate = (dateStr: string) => dateStr || '-'
const directionLabel = (direction: 'buy' | 'sell') => (direction === 'buy' ? '买入' : '卖出')

// Back to top
const showBackToTop = ref(false)
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 400
}
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadData(1)
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="ah-page-shell public-zone-page">
    <!-- Header -->
    <section class="public-zone-header ah-glass-card ah-page-section">
      <div class="public-zone-header__info">
        <p class="public-zone-header__eyebrow">公开交易区</p>
        <h2>社区动态</h2>
        <p class="public-zone-header__desc">共 {{ totalCount }} 条交易记录</p>
      </div>
      <el-button v-if="authStore.isAuthenticated" type="primary" @click="openAddDialog">
        + 发布交易
      </el-button>
    </section>

    <!-- Filters -->
    <section class="public-zone-filters ah-glass-card ah-page-section">
      <div class="public-zone-filters__group public-zone-filters__search">
        <span class="public-zone-filters__label">名称</span>
        <el-input
          v-model="filterKeyword"
          class="public-zone-filters__search-input"
          clearable
          placeholder="按物品名称筛选"
          @keyup.enter="handleKeywordSearch"
          @clear="handleKeywordClear"
        />
        <el-button @click="handleKeywordSearch">搜索</el-button>
      </div>
      <div class="public-zone-filters__group public-zone-filters__price">
        <span class="public-zone-filters__label">价格</span>
        <el-input-number
          v-model="filterMinPrice"
          class="public-zone-filters__price-input"
          :min="0"
          :precision="2"
          :step="1"
          placeholder="最低价"
        />
        <span class="public-zone-filters__range-sep">-</span>
        <el-input-number
          v-model="filterMaxPrice"
          class="public-zone-filters__price-input"
          :min="0"
          :precision="2"
          :step="1"
          placeholder="最高价"
        />
        <el-button @click="handlePriceSearch">查询</el-button>
        <el-button @click="handlePriceClear">清空</el-button>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">范围</span>
        <el-radio-group v-model="filterScope" size="small" @change="handleFilterChange">
          <el-radio-button label="all">全部记录</el-radio-button>
          <el-radio-button label="mine">我的记录</el-radio-button>
        </el-radio-group>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">方向</span>
        <el-radio-group v-model="filterDirection" size="small" @change="handleFilterChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="buy">买入</el-radio-button>
          <el-radio-button label="sell">卖出</el-radio-button>
        </el-radio-group>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">渠道</span>
        <el-select v-model="filterChannel" size="small" @change="handleFilterChange">
          <el-option v-for="opt in channelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
      <div class="public-zone-filters__group">
        <span class="public-zone-filters__label">分类</span>
        <el-select v-model="filterCategory" size="small" @change="handleFilterChange">
          <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
        </el-select>
      </div>
    </section>

    <!-- Top Pagination -->
    <div class="public-zone-pagination ah-glass-card">
      <PaginationBar
        :current="currentPage"
        :total="totalCount"
        :page-size="20"
        @change="handlePageChange"
      />
    </div>

    <!-- Loading State -->
    <div v-if="loading && items.length === 0" class="public-zone-loading">
      <span class="public-zone-loading__spinner" />
      <p>加载中...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="items.length === 0" class="public-zone-empty ah-glass-card ah-page-section">
      <p>还没有任何公开交易</p>
      <p class="public-zone-empty__sub">成为第一个发布的人吧</p>
    </div>

    <!-- Post List -->
    <div v-else class="public-zone-list">
      <div
        v-for="item in items"
        :key="item.id"
        class="public-zone-item ah-glass-card ah-page-section"
      >
        <div class="public-zone-item__thumb">
          <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
        </div>

        <div class="public-zone-item__body">
          <div class="public-zone-item__header">
            <h3 class="public-zone-item__name">{{ item.itemName }}</h3>
            <span class="public-zone-item__direction" :class="`is-${item.direction}`">
              {{ directionLabel(item.direction) }}
            </span>
            <span class="public-zone-item__price">¥{{ item.price }}</span>
          </div>

          <div class="public-zone-item__meta">
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="public-zone-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
            <span class="public-zone-item__dot">·</span>
            <span>{{ formatDate(item.tradeTime) }}</span>
          </div>

          <div class="public-zone-item__footer">
            <div class="public-zone-item__left">
              <div class="public-zone-item__time">
                <span>{{ formatDate(item.createdAt) }}</span>
              </div>
              <div class="public-zone-item__untrusted-row">
                <el-button
                  v-if="authStore.isAuthenticated && !item.mine"
                  size="small"
                  type="warning"
                  @click="handleToggleUntrusted(item)"
                >
                  {{ item.untrusted ? '取消不可信' : '不可信' }}
                </el-button>
                <span class="public-zone-item__untrusted-badge">
                  不可信 × {{ item.untrustedCount }}
                </span>
              </div>
            </div>

            <div class="public-zone-item__actions">
              <template v-if="item.mine">
                <el-button size="small" @click="openEditDialog(item)">编辑</el-button>
                <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
              </template>
            </div>
          </div>
        </div>
      </div>

      <!-- Bottom Pagination -->
      <div class="public-zone-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="20"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
      <el-form label-position="top" class="public-zone-form">
        <el-form-item label="物品名称">
          <el-input v-model="form.itemName" placeholder="例如：龙娃惊讶" maxlength="40" />
        </el-form-item>

        <div class="public-zone-form__row">
          <el-form-item label="价格">
            <el-input-number v-model="form.price" :min="0.01" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="交易时间">
            <el-date-picker
              v-model="form.tradeTime"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>

        <div class="public-zone-form__row">
          <el-form-item label="交易方向">
            <el-radio-group v-model="form.direction">
              <el-radio-button label="buy">买入</el-radio-button>
              <el-radio-button label="sell">卖出</el-radio-button>
            </el-radio-group>
          </el-form-item>
          <el-form-item label="渠道">
            <el-select v-model="form.channel">
              <el-option
                v-for="opt in channelOptions.filter(o => o.value !== 'all')"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="public-zone-form__row">
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option
                v-for="opt in categoryOptions.filter(o => o.value !== 'all')"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <el-form-item label="备注">
          <el-input v-model="form.remark" placeholder="可选" maxlength="15" />
        </el-form-item>

        <el-form-item label="物品图片">
          <input
            ref="fileInputRef"
            type="file"
            accept="image/jpeg,image/png,image/webp"
            style="display:none"
            @change="handleFileChange"
          />

          <div class="public-zone-form__upload">
            <div class="public-zone-form__upload-preview">
              <SquareImagePreview v-if="form.imageFileId" :file-id="form.imageFileId" empty-text="无图" />
              <div v-else class="public-zone-form__upload-placeholder">选择图片</div>
            </div>
            <div class="public-zone-form__upload-controls">
              <el-button @click="handlePickImage">选择图片</el-button>
              <el-button :disabled="!compressionResult" :loading="uploadingImage" @click="handleUploadImage">
                上传图片
              </el-button>
              <p v-if="compressionSummary" class="public-zone-form__upload-info">{{ compressionSummary }}</p>
              <p v-if="uploadProgress" class="public-zone-form__upload-progress">{{ uploadProgress }}</p>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmitForm">
          {{ editingId ? '保存' : '发布' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- Floating Action Buttons -->
    <transition name="fab">
      <button v-if="showBackToTop" class="fab fab--top" type="button" title="回顶部" @click="scrollToTop">
        ↑
      </button>
    </transition>
    <button class="fab fab--refresh" type="button" title="刷新" @click="loadData(1)">
      ↻
    </button>
    <button v-if="authStore.isAuthenticated" class="fab fab--add" type="button" title="发布交易" @click="openAddDialog">
      +
    </button>
  </div>
</template>

<style scoped>
.public-zone-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 0 40px;
}

/* Header */
.public-zone-header {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
}

.public-zone-header__eyebrow {
  margin: 0 0 8px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.public-zone-header h2 {
  margin: 0;
  color: var(--ah-title);
  font-size: 28px;
}

.public-zone-header__desc {
  margin: 8px 0 0;
  color: var(--ah-text);
}

/* Filters */
.public-zone-filters {
  display: flex;
  align-items: center;
  gap: 20px;
  flex-wrap: wrap;
}

.public-zone-filters__group {
  display: flex;
  align-items: center;
  gap: 10px;
}

.public-zone-filters__search {
  flex: 1 1 320px;
}

.public-zone-filters__search-input {
  min-width: 220px;
  max-width: 360px;
}

.public-zone-filters__price {
  flex: 1 1 420px;
  flex-wrap: wrap;
}

.public-zone-filters__price-input {
  width: 140px;
}

.public-zone-filters__range-sep {
  color: #8d7080;
  font-size: 14px;
}

.public-zone-filters__label {
  color: #8d7080;
  font-size: 13px;
  font-weight: 700;
  white-space: nowrap;
}

/* Loading */
.public-zone-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.public-zone-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: public-zone-spin 0.8s linear infinite;
}

@keyframes public-zone-spin {
  to { transform: rotate(360deg); }
}

.public-zone-loading__more {
  text-align: center;
  padding: 20px;
  color: var(--ah-text);
}

/* Empty */
.public-zone-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.public-zone-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

/* List */
.public-zone-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.public-zone-pagination {
  padding: 8px 0;
}

.public-zone-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.public-zone-item__thumb {
  flex-shrink: 0;
}

.public-zone-item__body {
  flex: 1;
  min-width: 0;
}

.public-zone-item__header {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.public-zone-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
}

.public-zone-item__direction {
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.public-zone-item__direction.is-sell {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.public-zone-item__direction.is-buy {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.public-zone-item__price {
  font-size: 18px;
  font-weight: 700;
  color: var(--ah-accent-deep);
}

.public-zone-item__meta {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.public-zone-item__dot {
  color: #c9a0b0;
}

.public-zone-item__untrusted-badge {
  background: rgba(207, 93, 117, 0.12);
  color: #c44d73;
  padding: 4px 10px;
  border-radius: 8px;
  font-size: 13px;
}

.public-zone-item__footer {
  margin-top: 10px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  flex-wrap: wrap;
}

.public-zone-item__left {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.public-zone-item__time {
  font-size: 13px;
  color: #8d7080;
}

.public-zone-item__untrusted-row {
  display: flex;
  align-items: center;
  gap: 8px;
  flex-wrap: wrap;
}

.public-zone-item__actions {
  display: flex;
  gap: 8px;
}

.public-zone-load-more {
  text-align: center;
  padding: 20px;
}

/* Form */
.public-zone-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.public-zone-form__upload {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.public-zone-form__upload-preview {
  flex-shrink: 0;
}

.public-zone-form__upload-placeholder {
  width: 168px;
  height: 168px;
  border-radius: 24px;
  border: 1px dashed rgba(216, 168, 183, 0.4);
  display: grid;
  place-items: center;
  color: #b8a0ac;
  background: rgba(255, 250, 247, 0.8);
}

.public-zone-form__upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.public-zone-form__upload-info {
  margin: 0;
  font-size: 13px;
  color: var(--ah-text);
}

.public-zone-form__upload-progress {
  margin: 0;
  font-size: 13px;
  color: #4caf7d;
}

@media (max-width: 600px) {
  .public-zone-filters__search {
    width: 100%;
  }

  .public-zone-filters__search-input {
    min-width: 0;
    max-width: none;
    flex: 1;
  }

  .public-zone-filters__price {
    width: 100%;
  }

  .public-zone-filters__price-input {
    width: calc(50% - 26px);
    min-width: 0;
  }

  .public-zone-item {
    flex-direction: column;
  }

  .public-zone-form__row {
    grid-template-columns: 1fr;
  }

  .public-zone-form__upload {
    flex-direction: column;
  }
}

/* Floating Action Buttons */
.fab {
  position: fixed;
  right: 28px;
  width: 52px;
  height: 52px;
  border-radius: 50%;
  border: 0;
  cursor: pointer;
  font-size: 26px;
  line-height: 1;
  display: grid;
  place-items: center;
  box-shadow: 0 8px 24px rgba(240, 111, 154, 0.32);
  transition: transform 0.2s, box-shadow 0.2s;
  z-index: 100;
}

.fab--add {
  bottom: 32px;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
}

.fab--refresh {
  bottom: 96px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ah-accent-deep);
  border: 1px solid rgba(205, 145, 168, 0.3);
  font-size: 22px;
}

.fab--top {
  bottom: 160px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ah-accent-deep);
  border: 1px solid rgba(205, 145, 168, 0.3);
}

.fab--add:hover,
.fab--refresh:hover,
.fab--top:hover {
  transform: scale(1.08);
  background: #fff;
}

.fab-enter-active,
.fab-leave-active {
  transition: opacity 0.25s, transform 0.25s;
}

.fab-enter-from,
.fab-leave-to {
  opacity: 0;
  transform: scale(0.6);
}
</style>
