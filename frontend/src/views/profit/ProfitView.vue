<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import ProfitDesktopContent from './ProfitDesktopContent.vue'
import ProfitMobileContent from './ProfitMobileContent.vue'
import { uploadImage } from '@/api/file'
import {
  compressImageBeforeUpload,
  formatFileSize,
  type CompressionResult,
} from '@/utils/image-upload'
import {
  createTradeItem,
  deleteTradeItem,
  getTradePage,
  toggleTradePublic,
  updateTradeItem,
} from '@/api/trade'
import type {
  TradeCategory,
  TradeChannel,
  TradeListItem,
  TradeSummary,
  TradeUpsertRequest,
} from '@/types/trade'

const MOBILE_BREAKPOINT = 768

const loading = ref(false)
const items = ref<TradeListItem[]>([])
const totalCount = ref(0)
const summary = ref<TradeSummary>({
  totalProfit: '0.00',
  totalLoss: '0.00',
  totalBuyAmount: '0.00',
  totalSellAmount: '0.00',
  obiCount: 0,
  obiBuyAmount: '0.00',
  magicCount: 0,
  magicBuyAmount: '0.00',
})
const PAGE_SIZE = 30
const currentScope = ref<'all' | 'profit' | 'loss'>('all')
const currentPage = ref(1)
const currentCategory = ref<TradeCategory | ''>('')
const filterKeyword = ref('')
const isMobile = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const editingId = ref<number | null>(null)
const formLoading = ref(false)

const channelOptions: { label: string; value: TradeChannel }[] = [
  { label: '闲鱼', value: 'xianyu' },
  { label: '贴吧', value: 'tieba' },
  { label: '其他', value: 'other' },
]

const categoryOptions: { label: string; value: TradeCategory }[] = [
  { label: '奥比时装', value: 'obi' },
  { label: '魔力时装', value: 'magic' },
]

const form = reactive<TradeUpsertRequest>({
  itemName: '',
  buyPrice: 0,
  buyTime: '',
  sellPrice: 0,
  sellTime: '',
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

const publicLoading = ref(false)

const loadData = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const resp = await getTradePage({
      pageNo: page,
      pageSize: PAGE_SIZE,
      keyword: filterKeyword.value.trim() || undefined,
      scope: currentScope.value,
      category: currentCategory.value || undefined,
    })
    items.value = resp.data.items
    totalCount.value = resp.data.totalCount
    summary.value = resp.data.summary
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handleScopeChange = (scope?: 'all' | 'profit' | 'loss') => {
  if (scope) {
    currentScope.value = scope
  }
  currentPage.value = 1
  loadData(1)
}

const handleCategoryFilter = (category: TradeCategory) => {
  currentCategory.value = currentCategory.value === category ? '' : category
  loadData(1)
}

const handleKeywordSearch = () => {
  loadData(1)
}

const handleKeywordClear = () => {
  filterKeyword.value = ''
  loadData(1)
}

const handlePageChange = (page: number) => {
  loadData(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const openAddDialog = () => {
  dialogTitle.value = '新增记录'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (item: TradeListItem) => {
  dialogTitle.value = '编辑记录'
  editingId.value = item.id
  form.itemName = item.itemName
  form.buyPrice = item.buyPrice
  form.buyTime = item.buyTime
  form.sellPrice = item.sellPrice
  form.sellTime = item.sellTime
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
  form.buyPrice = 0
  form.buyTime = ''
  form.sellPrice = 0
  form.sellTime = ''
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
  if (!form.buyPrice || form.buyPrice <= 0) {
    ElMessage.warning('请输入有效的买入价格')
    return
  }
  if (!form.sellPrice || form.sellPrice <= 0) {
    ElMessage.warning('请输入有效的卖出价格')
    return
  }
  if (!form.buyTime || !form.sellTime) {
    ElMessage.warning('请选择买入和卖出日期')
    return
  }

  formLoading.value = true
  try {
    const payload: TradeUpsertRequest = {
      ...form,
      requestId: `req_${Date.now()}`,
    }

    if (editingId.value) {
      await updateTradeItem(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createTradeItem(payload)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    await loadData(currentPage.value)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    formLoading.value = false
  }
}

const handleDelete = async (item: TradeListItem) => {
  try {
    await ElMessageBox.confirm(`确定删除「${item.itemName}」吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteTradeItem(item.id, `req_${Date.now()}`)
    ElMessage.success('删除成功')
    await loadData(currentPage.value)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const handlePublicAction = async (item: TradeListItem) => {
  publicLoading.value = true
  try {
    await toggleTradePublic(item.id, {
      price: item.sellPrice || item.buyPrice,
      tradeTime: item.sellTime || item.buyTime,
      direction: (item.sellPrice || 0) >= (item.buyPrice || 0) ? 'sell' : 'buy',
      remark: item.remark || undefined,
      imageFileId: item.imageFileId || undefined,
      requestId: `req_${Date.now()}`,
    })
    ElMessage.success(item.publicPosted ? '已取消公开' : '已公开')
    await loadData(currentPage.value)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    publicLoading.value = false
  }
}

const formatDate = (dateStr: string) => dateStr || '-'

const channelLabel = (channel: TradeChannel) => channelOptions.find((option) => option.value === channel)?.label || channel
const categoryLabel = (category: TradeCategory) => categoryOptions.find((option) => option.value === category)?.label || category

const profitDisplay = computed(() => {
  const profit = parseFloat(summary.value.totalProfit || '0')
  const loss = parseFloat(summary.value.totalLoss || '0')
  const net = profit - loss
  return { profit, loss, net }
})

const summaryTotalCount = computed(() => summary.value.obiCount + summary.value.magicCount)

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
  loadData()
  window.addEventListener('scroll', handleScroll, { passive: true })
  window.addEventListener('resize', handleResize, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
  window.removeEventListener('resize', handleResize)
})
</script>

<template>
  <div class="ah-page-shell profit-page">
    <ProfitMobileContent
      v-if="isMobile"
      v-model:filter-keyword="filterKeyword"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :summary="summary"
      :profit-display="profitDisplay"
      :summary-total-count="summaryTotalCount"
      :current-scope="currentScope"
      :current-category="currentCategory"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :format-date="formatDate"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @scope-change="handleScopeChange"
      @category-filter="handleCategoryFilter"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @edit="openEditDialog"
      @public-action="handlePublicAction"
      @delete="handleDelete"
    />

    <ProfitDesktopContent
      v-else
      v-model:filter-keyword="filterKeyword"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :summary="summary"
      :profit-display="profitDisplay"
      :summary-total-count="summaryTotalCount"
      :current-scope="currentScope"
      :current-category="currentCategory"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :format-date="formatDate"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @scope-change="handleScopeChange"
      @category-filter="handleCategoryFilter"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @edit="openEditDialog"
      @public-action="handlePublicAction"
      @delete="handleDelete"
    />

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="540px" :close-on-click-modal="false">
      <el-form label-position="top" class="profit-form">
        <el-form-item label="物品名称">
          <el-input v-model="form.itemName" placeholder="例如：龙娃惊讶" maxlength="40" />
        </el-form-item>

        <div class="profit-form__row">
          <el-form-item label="买入价格">
            <el-input-number v-model="form.buyPrice" :min="0.01" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="买入日期">
            <el-date-picker
              v-model="form.buyTime"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>

        <div class="profit-form__row">
          <el-form-item label="卖出价格">
            <el-input-number v-model="form.sellPrice" :min="0.01" :precision="2" :step="1" />
          </el-form-item>
          <el-form-item label="卖出日期">
            <el-date-picker
              v-model="form.sellTime"
              type="date"
              placeholder="选择日期"
              value-format="YYYY-MM-DD"
            />
          </el-form-item>
        </div>

        <div class="profit-form__row">
          <el-form-item label="渠道">
            <el-select v-model="form.channel">
              <el-option v-for="option in channelOptions" :key="option.value" :label="option.label" :value="option.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option v-for="option in categoryOptions" :key="option.value" :label="option.label" :value="option.value" />
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

          <div class="profit-form__upload">
            <div class="profit-form__upload-preview">
              <SquareImagePreview v-if="form.imageFileId" :file-id="form.imageFileId" empty-text="无图" />
              <div v-else class="profit-form__upload-placeholder">选择图片</div>
            </div>
            <div class="profit-form__upload-controls">
              <el-button :loading="uploadingImage" @click="handlePickImage">选择图片</el-button>
              <p v-if="compressionSummary" class="profit-form__upload-info">{{ compressionSummary }}</p>
              <p v-if="uploadProgress" class="profit-form__upload-progress">{{ uploadProgress }}</p>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmitForm">保存</el-button>
      </template>
    </el-dialog>

    <transition name="fab">
      <button v-if="showBackToTop" class="fab fab--top" type="button" title="回顶部" @click="scrollToTop">
        ↑
      </button>
    </transition>
    <button class="fab fab--add" type="button" title="新增记录" @click="openAddDialog">
      +
    </button>
  </div>
</template>

<style scoped>
.profit-page {
  padding: 0 0 40px;
}

.profit-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.profit-form__upload {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.profit-form__upload-preview {
  flex-shrink: 0;
}

.profit-form__upload-placeholder {
  width: 168px;
  height: 168px;
  border-radius: 24px;
  border: 1px dashed rgba(216, 168, 183, 0.4);
  display: grid;
  place-items: center;
  color: #b8a0ac;
  background: rgba(255, 250, 247, 0.8);
}

.profit-form__upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.profit-form__upload-info {
  margin: 0;
  font-size: 13px;
  color: var(--ah-text);
}

.profit-form__upload-progress {
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

.fab--add:hover {
  transform: scale(1.08);
  box-shadow: 0 12px 32px rgba(240, 111, 154, 0.44);
}

.fab--top {
  bottom: 96px;
  background: rgba(255, 255, 255, 0.92);
  color: var(--ah-accent-deep);
  border: 1px solid rgba(205, 145, 168, 0.3);
}

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

@media (max-width: 640px) {
  .profit-form__row {
    grid-template-columns: 1fr;
  }

  .profit-form__upload {
    flex-direction: column;
  }
}
</style>
