<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FloatingQuickNav from '@/components/common/FloatingQuickNav.vue'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import PublicZoneDesktopContent from './PublicZoneDesktopContent.vue'
import PublicZoneMobileContent from './PublicZoneMobileContent.vue'
import { uploadImage } from '@/api/file'
import {
  compressImageBeforeUpload,
  formatFileSize,
  IMAGE_INPUT_ACCEPT,
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
const MOBILE_BREAKPOINT = 768

const loading = ref(false)
const items = ref<PublicPostListItem[]>([])
const totalCount = ref(0)
const hasMore = ref(false)
const PAGE_SIZE = 30
const currentPage = ref(1)
const isMobile = ref(false)

const filterScope = ref<'all' | 'mine'>('all')
const filterDirection = ref<'all' | 'buy' | 'sell'>('all')
const filterChannel = ref<PublicPostChannel | 'all'>('all')
const filterCategory = ref<PublicPostCategory | 'all'>('all')
const filterKeyword = ref('')
const filterMinPrice = ref<number | null>(null)
const filterMaxPrice = ref<number | null>(null)

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

const loadData = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const params: Record<string, any> = { pageNo: page, pageSize: PAGE_SIZE }
    if (filterScope.value !== 'all') params.scope = filterScope.value
    if (filterDirection.value !== 'all') params.direction = filterDirection.value
    if (filterChannel.value !== 'all') params.channel = filterChannel.value
    if (filterCategory.value !== 'all') params.category = filterCategory.value
    if (filterKeyword.value.trim()) params.keyword = filterKeyword.value.trim()
    if (filterMinPrice.value != null) params.minPrice = String(filterMinPrice.value)
    if (filterMaxPrice.value != null) params.maxPrice = String(filterMaxPrice.value)

    const resp = await getPublicPostPage(params)
    items.value = resp.data.items
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

  uploadingImage.value = true
  try {
    compressionResult.value = await compressImageBeforeUpload(file)
    const resp = await uploadImage(compressionResult.value.file, 'public')
    form.imageFileId = resp.data.fileId
    uploadProgress.value = `上传成功，fileId: ${resp.data.fileId}`
    ElMessage.success('图片已上传成功')
  } catch (error: any) {
    compressionResult.value = null
    uploadProgress.value = ''
    ElMessage.error(error?.response?.data?.message || error?.message || '图片上传失败')
  } finally {
    uploadingImage.value = false
    input.value = ''
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

const channelLabel = (channel: PublicPostChannel) => channelOptions.find((option) => option.value === channel)?.label || channel
const categoryLabel = (category: PublicPostCategory) => categoryOptions.find((option) => option.value === category)?.label || category
const formatDate = (dateStr: string) => {
  if (!dateStr) return '-'
  return dateStr.replace('T', ' ').replace(/\.\d+$/, '').replace(/Z$/, '')
}
const directionLabel = (direction: 'buy' | 'sell') => (direction === 'buy' ? '买入' : '卖出')

const showBackToTop = ref(false)

const handleScroll = () => {
  showBackToTop.value = window.scrollY > 400
}

const handleResize = () => {
  isMobile.value = window.innerWidth <= MOBILE_BREAKPOINT
}

const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  handleResize()
  loadData(1)
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', handleResize, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="ah-page-shell public-zone-page">
    <PublicZoneMobileContent
      v-if="isMobile"
      v-model:filter-keyword="filterKeyword"
      v-model:filter-min-price="filterMinPrice"
      v-model:filter-max-price="filterMaxPrice"
      v-model:filter-scope="filterScope"
      v-model:filter-direction="filterDirection"
      v-model:filter-channel="filterChannel"
      v-model:filter-category="filterCategory"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :is-authenticated="authStore.isAuthenticated"
      :channel-options="channelOptions"
      :category-options="categoryOptions"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      :format-date="formatDate"
      :direction-label="directionLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @price-search="handlePriceSearch"
      @price-clear="handlePriceClear"
      @filter-change="handleFilterChange"
      @edit="openEditDialog"
      @delete="handleDelete"
      @toggle-untrusted="handleToggleUntrusted"
    />

    <PublicZoneDesktopContent
      v-else
      v-model:filter-keyword="filterKeyword"
      v-model:filter-min-price="filterMinPrice"
      v-model:filter-max-price="filterMaxPrice"
      v-model:filter-scope="filterScope"
      v-model:filter-direction="filterDirection"
      v-model:filter-channel="filterChannel"
      v-model:filter-category="filterCategory"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :is-authenticated="authStore.isAuthenticated"
      :channel-options="channelOptions"
      :category-options="categoryOptions"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      :format-date="formatDate"
      :direction-label="directionLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @price-search="handlePriceSearch"
      @price-clear="handlePriceClear"
      @filter-change="handleFilterChange"
      @edit="openEditDialog"
      @delete="handleDelete"
      @toggle-untrusted="handleToggleUntrusted"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="isMobile ? '100%' : '520px'"
      :fullscreen="isMobile"
      :close-on-click-modal="false"
    >
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
                v-for="option in channelOptions.filter((item) => item.value !== 'all')"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
        </div>

        <div class="public-zone-form__row">
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option
                v-for="option in categoryOptions.filter((item) => item.value !== 'all')"
                :key="option.value"
                :label="option.label"
                :value="option.value"
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
            :accept="IMAGE_INPUT_ACCEPT"
            style="display: none"
            @change="handleFileChange"
          />

          <div class="public-zone-form__upload">
            <div class="public-zone-form__upload-preview">
              <SquareImagePreview v-if="form.imageFileId" :file-id="form.imageFileId" empty-text="无图" />
              <div v-else class="public-zone-form__upload-placeholder">选择图片</div>
            </div>
            <div class="public-zone-form__upload-controls">
              <el-button :loading="uploadingImage" @click="handlePickImage">选择图片</el-button>
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
    <FloatingQuickNav />
  </div>
</template>

<style scoped>
.public-zone-page {
  padding: 0 0 40px;
}

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

@media (min-width: 769px) {
  .fab {
    right: max(28px, calc((100vw - var(--ah-shell-width)) / 2 + 16px));
  }

  .fab--refresh {
    bottom: 268px;
  }

  .fab--top {
    bottom: 332px;
  }
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

@media (max-width: 640px) {
  .public-zone-form__row {
    grid-template-columns: 1fr;
  }

  .public-zone-form__upload {
    gap: 12px;
  }

  .public-zone-form__upload-preview {
    --square-preview-size: 96px;
    --square-preview-radius: 16px;
  }

  .public-zone-form__upload-placeholder {
    width: 96px;
    height: 96px;
    border-radius: 16px;
    font-size: 12px;
  }

  .public-zone-form__upload-info,
  .public-zone-form__upload-progress {
    font-size: 12px;
  }

  .fab {
    right: 18px;
    width: 44px;
    height: 44px;
    font-size: 22px;
  }

  .fab--add {
    bottom: 94px;
  }

  .fab--refresh {
    bottom: 148px;
  }

  .fab--top {
    bottom: 202px;
  }
}
</style>
