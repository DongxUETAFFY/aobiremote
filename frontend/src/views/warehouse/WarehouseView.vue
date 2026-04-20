<script setup lang="ts">
import { computed, onBeforeUnmount, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import FloatingQuickNav from '@/components/common/FloatingQuickNav.vue'
import DesktopImageUploadArea from '@/components/common/DesktopImageUploadArea.vue'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import WarehouseDesktopContent from './WarehouseDesktopContent.vue'
import WarehouseMobileContent from './WarehouseMobileContent.vue'
import { uploadImage } from '@/api/file'
import {
  compressImageBeforeUpload,
  formatFileSize,
  IMAGE_INPUT_ACCEPT,
  type CompressionResult,
} from '@/utils/image-upload'
import {
  batchToggleInventoryPublic,
  createInventoryItem,
  deleteInventoryItem,
  getInventoryPage,
  markInventorySold,
  toggleInventoryPublic,
  updateInventoryItem,
} from '@/api/inventory'
import type {
  InventoryCategory,
  InventoryChannel,
  InventoryListItem,
  InventorySummary,
  InventoryUpsertRequest,
} from '@/types/inventory'

const MOBILE_BREAKPOINT = 768

const loading = ref(false)
const items = ref<InventoryListItem[]>([])
const totalCount = ref(0)
const summary = ref<InventorySummary>({
  totalBuyPrice: '0.00',
  obiCount: 0,
  obiBuyPrice: '0.00',
  magicCount: 0,
  magicBuyPrice: '0.00',
})
const PAGE_SIZE = 30
const currentPage = ref(1)
const selectedIds = ref<number[]>([])
const currentCategory = ref<InventoryCategory | ''>('')
const filterKeyword = ref('')
const isMobile = ref(false)

const dialogVisible = ref(false)
const dialogTitle = ref('新增记录')
const editingId = ref<number | null>(null)
const formLoading = ref(false)

const channelOptions: { label: string; value: InventoryChannel }[] = [
  { label: '闲鱼', value: 'xianyu' },
  { label: '贴吧', value: 'tieba' },
  { label: '其他', value: 'other' },
]

const categoryOptions: { label: string; value: InventoryCategory }[] = [
  { label: '奥比时装', value: 'obi' },
  { label: '魔力时装', value: 'magic' },
]

const form = reactive<InventoryUpsertRequest>({
  itemName: '',
  buyPrice: 0,
  buyTime: '',
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

const soldDialogVisible = ref(false)
const soldForm = reactive({
  sellPrice: 0,
  sellTime: '',
})
const soldItemId = ref<number | null>(null)
const soldLoading = ref(false)

const publicLoading = ref(false)
const batchPublicLoading = ref(false)

const isBatchPublicSelectable = (item: InventoryListItem) =>
  item.status === 'unsold' && !item.publicPosted

const selectableItemIds = computed(() =>
  items.value.filter(isBatchPublicSelectable).map((item) => item.id),
)

const selectedSelectableIds = computed(() =>
  selectedIds.value.filter((id) => selectableItemIds.value.includes(id)),
)

const hasSelectableItems = computed(() => selectableItemIds.value.length > 0)

const allSelectableChecked = computed(
  () =>
    selectableItemIds.value.length > 0 &&
    selectableItemIds.value.every((id) => selectedSelectableIds.value.includes(id)),
)

const summaryTotalCount = computed(() => summary.value.obiCount + summary.value.magicCount)

const loadData = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const resp = await getInventoryPage({
      pageNo: page,
      pageSize: PAGE_SIZE,
      keyword: filterKeyword.value.trim() || undefined,
      category: currentCategory.value || undefined,
    })
    items.value = resp.data.items
    totalCount.value = resp.data.totalCount
    summary.value = resp.data.summary
    selectedIds.value = selectedIds.value.filter((id) =>
      items.value.some((item) => item.id === id && isBatchPublicSelectable(item)),
    )
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '加载失败')
  } finally {
    loading.value = false
  }
}

const handlePageChange = (page: number) => {
  loadData(page)
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

const handleCategoryFilter = (category: InventoryCategory) => {
  currentCategory.value = currentCategory.value === category ? '' : category
  selectedIds.value = []
  loadData(1)
}

const handleKeywordSearch = () => {
  loadData(1)
}

const handleKeywordClear = () => {
  filterKeyword.value = ''
  loadData(1)
}

const openAddDialog = () => {
  dialogTitle.value = '新增记录'
  editingId.value = null
  resetForm()
  dialogVisible.value = true
}

const openEditDialog = (item: InventoryListItem) => {
  dialogTitle.value = '编辑记录'
  editingId.value = item.id
  form.itemName = item.itemName
  form.buyPrice = item.buyPrice
  form.buyTime = item.buyTime
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
    const resp = await uploadImage(compressionResult.value.file, 'private')
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

const handleDesktopUploaded = (fileId: string) => {
  form.imageFileId = fileId
  compressionResult.value = null
  uploadProgress.value = ''
  ElMessage.success('图片已上传成功')
}

const handleDesktopUploadingChange = (value: boolean) => {
  uploadingImage.value = value
}

const handleDesktopUploadError = (message: string) => {
  compressionResult.value = null
  uploadProgress.value = ''
  ElMessage.error(message)
}

const confirmReplaceImage = async () => {
  try {
    await ElMessageBox.confirm('将使用新图片替换当前图片，是否继续？', '替换图片', {
      confirmButtonText: '确认替换',
      cancelButtonText: '取消',
      type: 'warning',
    })
    return true
  } catch {
    return false
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
  if (!form.buyTime) {
    ElMessage.warning('请选择买入日期')
    return
  }
  formLoading.value = true
  try {
    const payload: InventoryUpsertRequest = {
      ...form,
      requestId: `req_${Date.now()}`,
    }
    if (editingId.value) {
      await updateInventoryItem(editingId.value, payload)
      ElMessage.success('更新成功')
    } else {
      await createInventoryItem(payload)
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

const handleDelete = async (item: InventoryListItem) => {
  try {
    await ElMessageBox.confirm(`确定删除「${item.itemName}」吗？`, '确认删除', {
      confirmButtonText: '删除',
      cancelButtonText: '取消',
      type: 'warning',
    })
    await deleteInventoryItem(item.id, `req_${Date.now()}`)
    ElMessage.success('删除成功')
    await loadData(currentPage.value)
  } catch (error: any) {
    if (error !== 'cancel') {
      ElMessage.error(error?.response?.data?.message || '删除失败')
    }
  }
}

const handleOpenSoldDialog = (item: InventoryListItem) => {
  soldItemId.value = item.id
  soldForm.sellPrice = item.buyPrice
  soldForm.sellTime = new Date().toISOString().split('T')[0]
  soldDialogVisible.value = true
}

const handleMarkSold = async () => {
  if (!soldForm.sellPrice || soldForm.sellPrice <= 0) {
    ElMessage.warning('请输入有效的卖出价格')
    return
  }
  if (!soldForm.sellTime) {
    ElMessage.warning('请选择卖出日期')
    return
  }
  soldLoading.value = true
  try {
    await markInventorySold(soldItemId.value!, {
      sellPrice: soldForm.sellPrice,
      sellTime: soldForm.sellTime,
      requestId: `req_${Date.now()}`,
    })
    ElMessage.success('已标记卖出')
    soldDialogVisible.value = false
    await loadData(currentPage.value)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '操作失败')
  } finally {
    soldLoading.value = false
  }
}

const handleToggleSelectAll = (checked: boolean | string | number) => {
  if (!checked) {
    selectedIds.value = []
    return
  }
  selectedIds.value = [...selectableItemIds.value]
}

const handleBatchPublic = async () => {
  if (!selectedSelectableIds.value.length) {
    ElMessage.warning('请选择未卖出且未公开的物品')
    return
  }

  batchPublicLoading.value = true
  try {
    await batchToggleInventoryPublic({
      ids: selectedSelectableIds.value,
      requestId: `req_${Date.now()}`,
    })
    ElMessage.success(`已批量公开 ${selectedSelectableIds.value.length} 件物品`)
    selectedIds.value = []
    await loadData(currentPage.value)
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '批量公开失败')
  } finally {
    batchPublicLoading.value = false
  }
}

const handlePublicAction = async (item: InventoryListItem) => {
  publicLoading.value = true
  try {
    await toggleInventoryPublic(item.id, {
      price: item.buyPrice,
      tradeTime: item.buyTime,
      direction: 'buy',
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

const channelLabel = (channel: InventoryChannel) =>
  channelOptions.find((option) => option.value === channel)?.label || channel

const categoryLabel = (category: InventoryCategory) =>
  categoryOptions.find((option) => option.value === category)?.label || category

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
  <div class="ah-page-shell warehouse-page">
    <WarehouseMobileContent
      v-if="isMobile"
      v-model:filter-keyword="filterKeyword"
      v-model:selected-ids="selectedIds"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :summary="summary"
      :summary-total-count="summaryTotalCount"
      :current-category="currentCategory"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :selected-selectable-ids="selectedSelectableIds"
      :all-selectable-checked="allSelectableChecked"
      :has-selectable-items="hasSelectableItems"
      :batch-public-loading="batchPublicLoading"
      :is-batch-public-selectable="isBatchPublicSelectable"
      :format-date="formatDate"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @category-filter="handleCategoryFilter"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @toggle-select-all="handleToggleSelectAll"
      @batch-public="handleBatchPublic"
      @edit="openEditDialog"
      @open-sold="handleOpenSoldDialog"
      @public-action="handlePublicAction"
      @delete="handleDelete"
    />

    <WarehouseDesktopContent
      v-else
      v-model:filter-keyword="filterKeyword"
      v-model:selected-ids="selectedIds"
      :loading="loading"
      :items="items"
      :total-count="totalCount"
      :summary="summary"
      :summary-total-count="summaryTotalCount"
      :current-category="currentCategory"
      :current-page="currentPage"
      :page-size="PAGE_SIZE"
      :selected-selectable-ids="selectedSelectableIds"
      :all-selectable-checked="allSelectableChecked"
      :has-selectable-items="hasSelectableItems"
      :batch-public-loading="batchPublicLoading"
      :is-batch-public-selectable="isBatchPublicSelectable"
      :format-date="formatDate"
      :channel-label="channelLabel"
      :category-label="categoryLabel"
      @open-add="openAddDialog"
      @page-change="handlePageChange"
      @category-filter="handleCategoryFilter"
      @keyword-search="handleKeywordSearch"
      @keyword-clear="handleKeywordClear"
      @toggle-select-all="handleToggleSelectAll"
      @batch-public="handleBatchPublic"
      @edit="openEditDialog"
      @open-sold="handleOpenSoldDialog"
      @public-action="handlePublicAction"
      @delete="handleDelete"
    />

    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      :width="isMobile ? '100%' : '520px'"
      :fullscreen="isMobile"
      :close-on-click-modal="false"
    >
      <el-form label-position="top" class="warehouse-form">
        <el-form-item label="物品名称">
          <el-input v-model="form.itemName" placeholder="例如：龙娃惊讶" maxlength="40" />
        </el-form-item>

        <div class="warehouse-form__row">
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

        <div class="warehouse-form__row">
          <el-form-item label="渠道">
            <el-select v-model="form.channel">
              <el-option
                v-for="option in channelOptions"
                :key="option.value"
                :label="option.label"
                :value="option.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option
                v-for="option in categoryOptions"
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
          <DesktopImageUploadArea
            v-if="!isMobile"
            :model-value="form.imageFileId"
            scene="private"
            :uploading="uploadingImage"
            :confirm-replace="confirmReplaceImage"
            @uploaded="handleDesktopUploaded"
            @uploading-change="handleDesktopUploadingChange"
            @error="handleDesktopUploadError"
          >
            <template #preview>
              <SquareImagePreview
                v-if="form.imageFileId"
                :file-id="form.imageFileId"
                empty-text="无图"
              />
              <div v-else class="warehouse-form__upload-placeholder">选择图片</div>
            </template>
          </DesktopImageUploadArea>

          <input
            v-if="isMobile"
            ref="fileInputRef"
            type="file"
            :accept="IMAGE_INPUT_ACCEPT"
            style="display: none"
            @change="handleFileChange"
          />

          <div v-if="isMobile" class="warehouse-form__upload">
            <div class="warehouse-form__upload-preview">
              <SquareImagePreview
                v-if="form.imageFileId"
                :file-id="form.imageFileId"
                empty-text="无图"
              />
              <div v-else class="warehouse-form__upload-placeholder">选择图片</div>
            </div>

            <div class="warehouse-form__upload-controls">
              <el-button :loading="uploadingImage" @click="handlePickImage">选择图片</el-button>
              <p v-if="compressionSummary" class="warehouse-form__upload-info">
                {{ compressionSummary }}
              </p>
              <p v-if="uploadProgress" class="warehouse-form__upload-progress">
                {{ uploadProgress }}
              </p>
            </div>
          </div>
        </el-form-item>
      </el-form>

      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="formLoading" @click="handleSubmitForm">
          保存
        </el-button>
      </template>
    </el-dialog>

    <el-dialog
      v-model="soldDialogVisible"
      title="标记卖出"
      :width="isMobile ? '100%' : '420px'"
      :fullscreen="isMobile"
    >
      <el-form label-position="top" class="warehouse-form">
        <el-form-item label="卖出价格">
          <el-input-number v-model="soldForm.sellPrice" :min="0.01" :precision="2" :step="1" />
        </el-form-item>
        <el-form-item label="卖出日期">
          <el-date-picker
            v-model="soldForm.sellTime"
            type="date"
            placeholder="选择日期"
            value-format="YYYY-MM-DD"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="soldDialogVisible = false">取消</el-button>
        <el-button type="success" :loading="soldLoading" @click="handleMarkSold">确认卖出</el-button>
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
    <FloatingQuickNav />
  </div>
</template>

<style scoped>
.warehouse-page {
  padding: 0 0 40px;
}

.warehouse-form__row {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 12px;
}

.warehouse-form__upload {
  display: flex;
  gap: 16px;
  align-items: flex-start;
}

.warehouse-form__upload-preview {
  flex-shrink: 0;
}

.warehouse-form__upload-placeholder {
  width: 168px;
  height: 168px;
  border-radius: 24px;
  border: 1px dashed rgba(216, 168, 183, 0.4);
  display: grid;
  place-items: center;
  color: #b8a0ac;
  background: rgba(255, 250, 247, 0.8);
}

.warehouse-form__upload-controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.warehouse-form__upload-info {
  margin: 0;
  font-size: 13px;
  color: var(--ah-text);
}

.warehouse-form__upload-progress {
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

@media (min-width: 769px) {
  .fab {
    right: max(28px, calc((100vw - var(--ah-shell-width)) / 2 + 16px));
  }

  .fab--top {
    bottom: 268px;
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
  .warehouse-form__row {
    grid-template-columns: 1fr;
  }

  .warehouse-form__upload {
    gap: 12px;
  }

  .warehouse-form__upload-preview {
    --square-preview-size: 96px;
    --square-preview-radius: 16px;
  }

  .warehouse-form__upload-placeholder {
    width: 96px;
    height: 96px;
    border-radius: 16px;
    font-size: 12px;
  }

  .warehouse-form__upload-info,
  .warehouse-form__upload-progress {
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

  .fab--top {
    bottom: 148px;
  }
}
</style>
