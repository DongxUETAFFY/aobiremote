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

// --- State ---
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
const currentScope = ref<'all' | 'profit' | 'loss'>('all')
const currentPage = ref(1)
const currentCategory = ref<TradeCategory | ''>('')
const filterKeyword = ref('')

// Form dialog
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

const publicLoading = ref(false)

// --- Methods ---
const loadData = async (page = 1) => {
  loading.value = true
  currentPage.value = page
  try {
    const resp = await getTradePage({
      pageNo: page,
      pageSize: 20,
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

const handleScopeChange = () => {
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
    await loadData()
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
    await loadData()
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

const channelLabel = (ch: TradeChannel) => channelOptions.find((o) => o.value === ch)?.label || ch
const categoryLabel = (cat: TradeCategory) => categoryOptions.find((o) => o.value === cat)?.label || cat

const profitDisplay = computed(() => {
  const profit = parseFloat(summary.value.totalProfit || '0')
  const loss = parseFloat(summary.value.totalLoss || '0')
  const net = profit - loss
  return { profit, loss, net }
})

const summaryTotalCount = computed(() => summary.value.obiCount + summary.value.magicCount)

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
  <div class="ah-page-shell profit-page">
    <!-- Summary Header -->
    <section class="profit-summary ah-glass-card ah-page-section">
      <div class="profit-summary__stats">
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">总盈利</p>
          <p class="profit-summary__stat-value is-profit">+¥{{ profitDisplay.profit.toFixed(2) }}</p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">总亏损</p>
          <p class="profit-summary__stat-value is-loss">-¥{{ profitDisplay.loss.toFixed(2) }}</p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">净收益</p>
          <p class="profit-summary__stat-value" :class="profitDisplay.net >= 0 ? 'is-profit' : 'is-loss'">
            {{ profitDisplay.net >= 0 ? '+' : '' }}¥{{ profitDisplay.net.toFixed(2) }}
          </p>
        </div>
        <div class="profit-summary__stat">
          <p class="profit-summary__stat-label">已卖出</p>
          <p class="profit-summary__stat-value">{{ summaryTotalCount }} 件</p>
        </div>
      </div>
      <div class="profit-summary__actions">
        <el-radio-group v-model="currentScope" size="default" @change="handleScopeChange">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="profit">盈利</el-radio-button>
          <el-radio-button label="loss">亏损</el-radio-button>
        </el-radio-group>
        <el-button type="primary" @click="openAddDialog">+ 新增记录</el-button>
      </div>
    </section>

    <section class="profit-category-summary ah-glass-card ah-page-section">
      <div class="profit-category-summary__status">
        当前筛选：{{ currentCategory ? categoryLabel(currentCategory) : '全部分类' }}
        <span v-if="currentCategory">，列表显示 {{ totalCount }} 件</span>
      </div>
      <div class="profit-category-summary__grid">
        <div
          class="profit-category-summary__item"
          :class="{ 'is-active': currentCategory === 'obi' }"
        >
          <p class="profit-category-summary__label">奥比总价格</p>
          <button
            type="button"
            class="profit-category-summary__price"
            @click="handleCategoryFilter('obi')"
          >
            ¥{{ summary.obiBuyAmount }}
          </button>
          <p class="profit-category-summary__count">奥比总件数 {{ summary.obiCount }} 件</p>
        </div>
        <div
          class="profit-category-summary__item"
          :class="{ 'is-active': currentCategory === 'magic' }"
        >
          <p class="profit-category-summary__label">魔力总价格</p>
          <button
            type="button"
            class="profit-category-summary__price"
            @click="handleCategoryFilter('magic')"
          >
            ¥{{ summary.magicBuyAmount }}
          </button>
          <p class="profit-category-summary__count">魔力总件数 {{ summary.magicCount }} 件</p>
        </div>
      </div>
    </section>

    <section class="profit-search ah-glass-card ah-page-section">
      <div class="profit-search__group">
        <span class="profit-search__label">名称</span>
        <el-input
          v-model="filterKeyword"
          class="profit-search__input"
          clearable
          placeholder="按物品名称筛选"
          @keyup.enter="handleKeywordSearch"
          @clear="handleKeywordClear"
        />
        <el-button @click="handleKeywordSearch">搜索</el-button>
      </div>
    </section>

    <!-- Loading State -->
    <div v-if="loading" class="profit-loading">
      <span class="profit-loading__spinner" />
      <p>加载中...</p>
    </div>

    <!-- Empty State -->
    <div v-else-if="items.length === 0" class="profit-empty ah-glass-card ah-page-section">
      <p>还没有已卖出的记录</p>
      <p class="profit-empty__sub">从「我的仓库」标记卖出，或在此新增记录</p>
    </div>

    <!-- Item List -->
    <div v-else class="profit-list">
      <!-- Top Pagination -->
      <div class="profit-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="20"
          @change="handlePageChange"
        />
      </div>

      <div
        v-for="item in items"
        :key="item.id"
        class="profit-item ah-glass-card ah-page-section"
      >
        <div class="profit-item__thumb">
          <SquareImagePreview :file-id="item.imageFileId" empty-text="无图" />
        </div>

        <div class="profit-item__body">
          <div class="profit-item__header">
            <h3 class="profit-item__name">{{ item.itemName }}</h3>
            <span
              class="profit-item__profit"
              :class="item.profitAmount >= 0 ? 'is-profit' : 'is-loss'"
            >
              {{ item.profitAmount >= 0 ? '+' : '' }}¥{{ item.profitAmount }}
            </span>
          </div>

          <div class="profit-item__meta">
            <span>买入 ¥{{ item.buyPrice }} · {{ formatDate(item.buyTime) }}</span>
            <span class="profit-item__arrow">→</span>
            <span>卖出 ¥{{ item.sellPrice }} · {{ formatDate(item.sellTime) }}</span>
          </div>

          <div class="profit-item__meta">
            <span>{{ channelLabel(item.channel) }}</span>
            <span class="profit-item__dot">·</span>
            <span>{{ categoryLabel(item.category) }}</span>
            <span v-if="item.publicPosted" class="profit-item__dot">·</span>
            <span v-if="item.publicPosted" class="profit-item__public-badge">已公开</span>
          </div>

          <p v-if="item.remark" class="profit-item__remark">备注：{{ item.remark }}</p>

          <div class="profit-item__actions">
            <el-button size="small" @click="openEditDialog(item)">编辑</el-button>
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
      <div class="profit-pagination ah-glass-card">
        <PaginationBar
          :current="currentPage"
          :total="totalCount"
          :page-size="20"
          @change="handlePageChange"
        />
      </div>
    </div>

    <!-- Add/Edit Dialog -->
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
              <el-option v-for="opt in channelOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
            </el-select>
          </el-form-item>
          <el-form-item label="分类">
            <el-select v-model="form.category">
              <el-option v-for="opt in categoryOptions" :key="opt.value" :label="opt.label" :value="opt.value" />
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
.profit-page {
  display: flex;
  flex-direction: column;
  gap: 16px;
  padding: 0 0 40px;
}

/* Summary */
.profit-summary {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  gap: 20px;
  flex-wrap: wrap;
}

.profit-summary__stats {
  display: grid;
  grid-template-columns: repeat(4, auto);
  gap: 20px;
}

.profit-summary__stat {
  text-align: center;
}

.profit-summary__stat-label {
  margin: 0 0 4px;
  color: #b27f93;
  font-size: 12px;
  font-weight: 700;
  letter-spacing: 0.1em;
  text-transform: uppercase;
}

.profit-summary__stat-value {
  margin: 0;
  font-size: 20px;
  font-weight: 700;
}

.profit-summary__stat-value.is-profit { color: #4caf7d; }
.profit-summary__stat-value.is-loss { color: #cf5d75; }

.profit-summary__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profit-category-summary {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profit-category-summary__status {
  color: #8d7080;
  font-size: 13px;
}

.profit-category-summary__grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.profit-category-summary__item {
  padding: 16px;
  border-radius: 18px;
  background: rgba(255, 250, 247, 0.78);
  border: 1px solid rgba(216, 168, 183, 0.16);
  transition: border-color 0.2s ease, box-shadow 0.2s ease, transform 0.2s ease;
}

.profit-category-summary__item.is-active {
  border-color: rgba(207, 93, 117, 0.35);
  box-shadow: 0 10px 24px rgba(207, 93, 117, 0.12);
  transform: translateY(-1px);
}

.profit-category-summary__label {
  margin: 0;
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-category-summary__price {
  margin-top: 10px;
  padding: 0;
  border: 0;
  background: transparent;
  color: var(--ah-title);
  font-size: 26px;
  font-weight: 800;
  cursor: pointer;
}

.profit-category-summary__price:hover {
  color: #cf5d75;
}

.profit-category-summary__count {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 14px;
}

.profit-search {
  display: flex;
  align-items: center;
  gap: 12px;
}

.profit-search__group {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
  width: 100%;
}

.profit-search__label {
  color: #b27f93;
  font-size: 13px;
  font-weight: 700;
}

.profit-search__input {
  flex: 1;
  min-width: 220px;
}

/* Loading */
.profit-loading {
  display: grid;
  place-items: center;
  gap: 12px;
  padding: 60px;
  color: var(--ah-text);
}

.profit-loading__spinner {
  width: 36px;
  height: 36px;
  border: 3px solid rgba(216, 168, 183, 0.3);
  border-top-color: var(--ah-accent);
  border-radius: 50%;
  animation: profit-spin 0.8s linear infinite;
}

@keyframes profit-spin {
  to { transform: rotate(360deg); }
}

/* Empty */
.profit-empty {
  text-align: center;
  padding: 60px;
  color: var(--ah-title);
}

.profit-empty__sub {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
}

/* List */
.profit-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}

.profit-pagination {
  padding: 8px 0;
}

.profit-item {
  display: flex;
  gap: 20px;
  align-items: flex-start;
}

.profit-item__thumb {
  flex-shrink: 0;
}

.profit-item__body {
  flex: 1;
  min-width: 0;
}

.profit-item__header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.profit-item__name {
  margin: 0;
  color: var(--ah-title);
  font-size: 18px;
}

.profit-item__profit {
  padding: 4px 10px;
  border-radius: 999px;
  font-size: 14px;
  font-weight: 700;
}

.profit-item__profit.is-profit {
  background: rgba(76, 175, 125, 0.15);
  color: #3d8c5a;
}

.profit-item__profit.is-loss {
  background: rgba(207, 93, 117, 0.15);
  color: #c44d73;
}

.profit-item__meta {
  margin-top: 8px;
  color: var(--ah-text);
  font-size: 14px;
  display: flex;
  align-items: center;
  gap: 6px;
  flex-wrap: wrap;
}

.profit-item__arrow {
  color: var(--ah-accent);
  font-weight: 700;
}

.profit-item__dot {
  color: #c9a0b0;
}

.profit-item__public-badge {
  background: rgba(255, 143, 177, 0.2);
  color: #c44d73;
  padding: 2px 8px;
  border-radius: 999px;
  font-size: 12px;
}

.profit-item__remark {
  margin: 8px 0 0;
  color: var(--ah-text);
  font-size: 13px;
}

.profit-item__actions {
  margin-top: 12px;
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

/* Form */
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

@media (max-width: 600px) {
  .profit-summary__stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .profit-category-summary__grid {
    grid-template-columns: 1fr;
  }

  .profit-search__input {
    min-width: 0;
  }

  .profit-item {
    flex-direction: column;
  }

  .profit-form__row {
    grid-template-columns: 1fr;
  }

  .profit-form__upload {
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
</style>
