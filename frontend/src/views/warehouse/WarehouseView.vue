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

// --- State ---
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

// Form dialog
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

// Mark sold dialog
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

// --- Methods ---
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
      direction: 'sell',
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

const channelLabel = (ch: InventoryChannel) =>
  channelOptions.find((o) => o.value === ch)?.label || ch

const categoryLabel = (cat: InventoryCategory) =>
  categoryOptions.find((o) => o.value === cat)?.label || cat

// Back to top
const showBackToTop = ref(false)
const handleScroll = () => {
  showBackToTop.value = window.scrollY > 400
}
const scrollToTop = () => {
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

onMounted(() => {
  loadData()
  window.addEventListener('scroll', handleScroll, { passive: true })
})

onBeforeUnmount(() => {
  window.removeEventListener('scroll', handleScroll)
})
</script>

<template>
  <div class="ah-page-shell warehouse-page">
    <!-- Summary Header -->
    <section class="warehouse-summary ah-glass-card ah-page-section">
      <div class="warehouse-summary__info">
        <p class="warehouse-summary__label">当前仓库</p>
        <h2 class="warehouse-summary__count">{{ summaryTotalCount }} 件宝贝</h2>
        <p class="warehouse-summary__total">累计买入 ¥{{ summary.totalBuyPrice }}</p>
        <p class="warehouse-summary__filter">
          当前筛选：{{ currentCategory ? categoryLabel(currentCategory) : '全部分类' }}
          <span v-if="currentCategory">，列表显示 {{ totalCount }} 件</span>
        </p>
      </div>
      <div class="warehouse-summary__actions">
        <el-button type="primary" size="large" @click="openAddDialog">
          + 新增记录
        </el-button>
      </div>
    </section>

    <section class="warehouse-category-summary ah-glass-card ah-page-section">
      <div
        class="warehouse-category-summary__item"
        :class="{ 'is-active': currentCategory === 'obi' }"
      >
        <p class="warehouse-category-summary__label">奥比总价格</p>
        <button
          type="button"
          class="warehouse-category-summary__price"
          @click="handleCategoryFilter('obi')"
        >
          ¥{{ summary.obiBuyPrice }}
        </button>
        <p class="warehouse-category-summary__count">奥比总件数 {{ summary.obiCount }} 件</p>
      </div>
      <div
        class="warehouse-category-summary__item"
        :class="{ 'is-active': currentCategory === 'magic' }"
      >
        <p class="warehouse-category-summary__label">魔力总价格</p>
        <button
          type="button"
          class="warehouse-category-summary__price"
          @click="handleCategoryFilter('magic')"
        >
          ¥{{ summary.magicBuyPrice }}
        </button>
        <p class="warehouse-category-summary__count">魔力总件数 {{ summary.magicCount }} 件</p>
      </div>
    </section>

    <section class="warehouse-search ah-glass-card ah-page-section">
      <div class="warehouse-search__group">
        <span class="warehouse-search__label">名称</span>
        <el-input
          v-model="filterKeyword"
          class="warehouse-search__input"
          clearable
          placeholder="按物品名称筛选"
          @keyup.enter="handleKeywordSearch"
          @clear="handleKeywordClear"
        />
        <el-button @click="handleKeywordSearch">搜索</el-button>
      </div>
    </section>

    <!-- Loading State -->
    <div v-if="loading" class="warehouse-loading">
      <span class="warehouse-loading__spinner" />
      <p>加载中...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="items.length === 0" class="warehouse-empty ah-glass-card ah-page-section">
      <p>还没有任何记录</p>
      <p class="warehouse-empty__sub">点击上方「新增记录」添加你的第一件宝贝</p>
    </div>

    <!-- Item List -->
    <div v-else class="warehouse-list">
      <section class="warehouse-batch ah-glass-card ah-page-section">
        <div class="warehouse-batch__toolbar">
          <div class="warehouse-batch__main">
            <el-checkbox
              :model-value="allSelectableChecked"
              :disabled="!hasSelectableItems"
              @change="handleToggleSelectAll"
            >
              全选本页可公开物品
            </el-checkbox>
            <span class="warehouse-batch__count">已选 {{ selectedSelectableIds.length }} 件</span>
          </div>
          <el-button
            type="warning"
            :disabled="!selectedSelectableIds.length"
            :loading="batchPublicLoading"
            @click="handleBatchPublic"
          >
            批量公开 {{ selectedSelectableIds.length ? `(${selectedSelectableIds.length})` : '' }}
          </el-button>
        </div>
        <p class="warehouse-batch__hint">仅支持选择未卖出且未公开的物品，避免误触取消公开。</p>
      </section>

      <!-- Top Pagination -->
      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="PAGE_SIZE"
          @change="handlePageChange"
        />
      </div>

      <div
        v-for="item in items"
        :key="item.id"
        class="warehouse-item ah-glass-card ah-page-section"
      >
        <div class="warehouse-item__selection">
          <el-checkbox
            v-model="selectedIds"
            :label="item.id"
            :disabled="!isBatchPublicSelectable(item)"
          >
            {{ isBatchPublicSelectable(item) ? '加入批量公开' : item.publicPosted ? '已公开，不可批量公开' : '已卖出，不可批量公开' }}
          </el-checkbox>
        </div>

        <div class="warehouse-item__thumb">
          <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
        </div>

        <div class="warehouse-item__body">
          <div class="warehouse-item__header">
            <h3 class="warehouse-item__name">{{ item.itemName }}</h3>
            <span class="warehouse-item__status" :class="`is-${item.status}`">
              {{ item.status === 'unsold' ? '未卖出' : '已卖出' }}
            </span>
          </div>

          <div class="warehouse-item__meta">
            <span>买入价 ¥{{ item.buyPrice }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ formatDate(item.buyTime) }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="warehouse-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
          </div>

          <p v-if="item.remark" class="warehouse-item__remark">备注：{{ item.remark }}</p>

          <div class="warehouse-item__actions">
            <el-button size="small" @click="openEditDialog(item)">编辑</el-button>
            <el-button
              v-if="item.status === 'unsold'"
              size="small"
              type="success"
              @click="handleOpenSoldDialog(item)"
            >
              标记卖出
            </el-button>
            <el-button
              size="small"
              :type="item.publicPosted ? 'warning' : 'primary'"
              @click="handlePublicAction(item)"
            >
              {{ item.publicPosted ? '取消公开' : '公开' }}
            </el-button>
            <el-button size="small" type="danger" @click="handleDelete(item)">删除</el-button>
          </div>
        </div>
      </div>

      <!-- Bottom Pagination -->
      <div class="warehouse-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="PAGE_SIZE"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" :close-on-click-modal="false">
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
                v-for="opt in channelOptions"
                :key="opt.value"
                :label="opt.label"
                :value="opt.value"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option
                v-for="opt in categoryOptions"
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

          <div class="warehouse-form__upload">
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

    <!-- Mark Sold Dialog -->
    <el-dialog v-model="soldDialogVisible" title="标记卖出" width="420px">
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

    <!-- Floating Action Buttons -->
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
.warehouse-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 0 40px;
}

/* Summary Header */
.warehouse-summary {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 20px;
}

.warehouse-summary__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-summary__label {
  margin: 0 0 4px;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
  text-transform: uppercase;
}

.warehouse-summary__count {
  margin: 0;
  color: var(--ah-title);
  font-size: 28px;
}

.warehouse-summary__total {
  margin: 4px 0 0;
  color: var(--ah-text);
}

.warehouse-summary__filter {
  margin: 6px 0 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-category-summary {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.warehouse-category-summary__item {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 250, 247, 0.78);
  border: 1px solid rgba(216, 168, 183, 0.16);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.warehouse-category-summary__item.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.warehouse-category-summary__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-category-summary__price {
  margin-top: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ah-title);
  font-size: 26px;
  font-weight: 800;
  cursor: pointer;
}

.warehouse-category-summary__price:hover {
  color: #cf5d75;
}

.warehouse-category-summary__count {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 14px;
}

.warehouse-batch {
  display: flex;
  flex-direction: column;
  align-items: stretch;
  gap: 16px;
}

.warehouse-batch__toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 16px;
  flex-wrap: wrap;
}

.warehouse-batch__main {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-batch__count {
  color: var(--ah-title);
  font-size: 14px;
  font-weight: 700;
}

.warehouse-batch__hint {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
}

.warehouse-search {
  display: flex;
  align-items: center;
  gap: 12px;
}

.warehouse-search__group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.warehouse-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.warehouse-search__input {
  flex: 1;
  min-width: 220px;
}

/* Loading */
.warehouse-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.warehouse-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: warehouse-spin 0.8s linear infinite;
}

@keyframes warehouse-spin {
  to {
    transform: rotate(360deg);
  }
}

/* Empty */
.warehouse-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.warehouse-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

/* Item List */
.warehouse-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.warehouse-pagination {
  padding: 8px 0;
}

.warehouse-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
  flex-wrap: wrap;
}

.warehouse-item__selection {
  width: 100%;
}

.warehouse-item__thumb {
  flex-shrink: 0;
}

.warehouse-item__body {
  flex: 1;
  min-width: 0;
}

.warehouse-item__header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.warehouse-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
}

.warehouse-item__status {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.warehouse-item__status.is-unsold {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
}

.warehouse-item__status.is-sold {
  background: rgba(110, 200, 140, 0.2);
  color: #3d8c5a;
}

.warehouse-item__meta {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.warehouse-item__dot {
  color: #c9a0b0;
}

.warehouse-item__remark {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 13px;
}

.warehouse-item__actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Form */
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

@media (max-width: 600px) {
  .warehouse-category-summary {
    grid-template-columns: 1fr;
  }

  .warehouse-summary__actions {
    width: 100%;
  }

  .warehouse-summary__actions :deep(.el-button) {
    flex: 1;
    min-width: 0;
  }

  .warehouse-batch__toolbar {
    align-items: stretch;
  }

  .warehouse-search__input {
    min-width: 0;
  }

  .warehouse-item {
    flex-direction: column;
  }

  .warehouse-form__row {
    grid-template-columns: 1fr;
  }

  .warehouse-form__upload {
    flex-direction: column;
  }
}
</style>
