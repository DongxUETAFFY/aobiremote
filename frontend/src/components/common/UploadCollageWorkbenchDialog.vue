<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { ElMessage } from 'element-plus'
import {
  renderCollageImage,
  type CollageCropMode,
  type CollageLayoutMode,
} from '@/utils/warehouse-collage'

interface LocalCollageImage {
  id: number
  file: File
  name: string
  objectUrl: string
}

const MAX_UPLOAD_IMAGES = 50
const AUTO_COLUMNS = 5
const MANUAL_MIN_COLUMNS = 1
const MANUAL_MAX_COLUMNS = 10
const COLLAGE_GAP = 4
const COLLAGE_PADDING = 12

const props = defineProps<{
  visible: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const fileInputRef = ref<HTMLInputElement | null>(null)
const localImages = ref<LocalCollageImage[]>([])
const layoutMode = ref<'auto' | 'manual'>('auto')
const cropMode = ref<CollageCropMode>('square')
const manualColumnCount = ref(AUTO_COLUMNS)
const generatedPreviewUrl = ref('')
const generating = ref(false)
const dragActive = ref(false)
const draggingImageId = ref<number | null>(null)

let nextLocalImageId = 1

const selectedCount = computed(() => localImages.value.length)
const canGenerate = computed(() => localImages.value.length > 0 && !generating.value)
const resolvedColumnCount = computed(() =>
  Math.min(MANUAL_MAX_COLUMNS, Math.max(MANUAL_MIN_COLUMNS, Math.floor(manualColumnCount.value || AUTO_COLUMNS))),
)
const resolvedMode = computed<CollageLayoutMode>(() =>
  layoutMode.value === 'auto' ? 'auto' : (String(resolvedColumnCount.value) as CollageLayoutMode),
)

const releaseGeneratedPreview = () => {
  if (generatedPreviewUrl.value) {
    URL.revokeObjectURL(generatedPreviewUrl.value)
    generatedPreviewUrl.value = ''
  }
}

const releaseLocalImages = () => {
  localImages.value.forEach((image) => URL.revokeObjectURL(image.objectUrl))
  localImages.value = []
}

const resetWorkbench = () => {
  releaseGeneratedPreview()
  releaseLocalImages()
  layoutMode.value = 'auto'
  cropMode.value = 'square'
  manualColumnCount.value = AUTO_COLUMNS
  generating.value = false
  dragActive.value = false
  draggingImageId.value = null
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const extractImageFiles = (files?: FileList | File[] | null) =>
  Array.from(files ?? []).filter((file) => file.type.startsWith('image/'))

const appendFiles = (files: File[]) => {
  if (!files.length) {
    ElMessage.warning('请选择图片文件')
    return
  }

  const remainingSlots = MAX_UPLOAD_IMAGES - localImages.value.length
  if (remainingSlots <= 0) {
    ElMessage.warning(`最多支持 ${MAX_UPLOAD_IMAGES} 张图片`)
    return
  }

  const acceptedFiles = files.slice(0, remainingSlots)
  const nextImages = acceptedFiles.map((file) => ({
    id: nextLocalImageId++,
    file,
    name: file.name,
    objectUrl: URL.createObjectURL(file),
  }))

  localImages.value = [...localImages.value, ...nextImages]

  if (acceptedFiles.length < files.length) {
    ElMessage.warning(`最多支持 ${MAX_UPLOAD_IMAGES} 张图片`)
  }
}

const handlePickImages = () => {
  fileInputRef.value?.click()
}

const handleFileChange = (event: Event) => {
  const input = event.target as HTMLInputElement
  appendFiles(extractImageFiles(input.files))
  input.value = ''
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  dragActive.value = true
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  dragActive.value = false
}

const handleDrop = (event: DragEvent) => {
  event.preventDefault()
  dragActive.value = false
  appendFiles(extractImageFiles(event.dataTransfer?.files))
}

const handlePaste = (event: ClipboardEvent) => {
  const files = Array.from(event.clipboardData?.items ?? [])
    .filter((item) => item.type.startsWith('image/'))
    .map((item) => item.getAsFile())
    .filter((file): file is File => Boolean(file))

  if (!files.length) {
    return
  }

  event.preventDefault()
  appendFiles(files)
}

const moveImageBefore = (draggedId: number, targetId: number) => {
  if (draggedId === targetId) {
    return
  }

  const nextImages = [...localImages.value]
  const draggedIndex = nextImages.findIndex((image) => image.id === draggedId)
  const targetIndex = nextImages.findIndex((image) => image.id === targetId)

  if (draggedIndex === -1 || targetIndex === -1) {
    return
  }

  const [draggedImage] = nextImages.splice(draggedIndex, 1)
  nextImages.splice(targetIndex, 0, draggedImage)
  localImages.value = nextImages
}

const handleSortDragStart = (imageId: number) => {
  draggingImageId.value = imageId
}

const handleSortDrop = (targetId: number) => {
  if (draggingImageId.value == null) {
    return
  }

  moveImageBefore(draggingImageId.value, targetId)
  draggingImageId.value = null
}

const handleSortDragEnd = () => {
  draggingImageId.value = null
}

const handleRemoveImage = (imageId: number) => {
  const image = localImages.value.find((item) => item.id === imageId)
  if (!image) {
    return
  }

  URL.revokeObjectURL(image.objectUrl)
  localImages.value = localImages.value.filter((item) => item.id !== imageId)
}

const handleGenerate = async () => {
  if (!localImages.value.length) {
    ElMessage.warning('请先导入至少 1 张图片')
    return
  }

  generating.value = true
  try {
    releaseGeneratedPreview()
    const result = await renderCollageImage({
      items: localImages.value.map((image) => ({ imageUrl: image.objectUrl })),
      mode: resolvedMode.value,
      cropMode: cropMode.value,
      gap: COLLAGE_GAP,
      padding: COLLAGE_PADDING,
      maxColumns: MANUAL_MAX_COLUMNS,
      autoColumns: AUTO_COLUMNS,
    })
    generatedPreviewUrl.value = result.objectUrl
  } catch (error: any) {
    ElMessage.error(error?.message || '生成拼图失败，请重试')
  } finally {
    generating.value = false
  }
}

const handleDownload = () => {
  if (!generatedPreviewUrl.value) {
    return
  }

  const link = document.createElement('a')
  link.href = generatedPreviewUrl.value
  link.download = `upload-collage-${selectedCount.value}.png`
  link.click()
}

const handleOpenPreview = () => {
  if (!generatedPreviewUrl.value) {
    return
  }

  window.open(generatedPreviewUrl.value, '_blank', 'noopener')
}

watch(
  () => props.visible,
  (visible) => {
    if (!visible) {
      resetWorkbench()
    }
  },
)

onBeforeUnmount(() => {
  resetWorkbench()
})
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="拼图"
    width="1180px"
    append-to-body
    :close-on-click-modal="false"
    :close-on-press-escape="false"
    class="upload-collage-dialog"
  >
    <div class="upload-collage-workbench" tabindex="0" @paste="handlePaste">
      <section class="upload-collage-panel upload-collage-panel--source">
        <div
          class="upload-collage-dropzone"
          :class="{ 'is-drag-active': dragActive }"
          data-testid="upload-collage-dropzone"
          @click="handlePickImages"
          @dragover="handleDragOver"
          @dragleave="handleDragLeave"
          @drop="handleDrop"
        >
          <input
            ref="fileInputRef"
            data-testid="upload-collage-file-input"
            class="upload-collage-dropzone__input"
            type="file"
            accept="image/*"
            multiple
            @change="handleFileChange"
          />
          <p class="upload-collage-dropzone__title">导入本地图片</p>
          <p class="upload-collage-dropzone__hint">支持点击、拖拽或 Ctrl+V 粘贴，最多 50 张</p>
        </div>

        <div class="upload-collage-toolbar">
          <span class="upload-collage-toolbar__count">已上传 {{ selectedCount }} / 50 张</span>
          <el-button plain @click="handlePickImages">继续添加</el-button>
        </div>

        <div v-if="localImages.length" class="upload-collage-list">
          <article
            v-for="(image, index) in localImages"
            :key="image.id"
            class="upload-collage-item"
            data-testid="upload-collage-item"
            draggable="true"
            @dragstart="handleSortDragStart(image.id)"
            @dragover.prevent
            @drop="handleSortDrop(image.id)"
            @dragend="handleSortDragEnd"
          >
            <img class="upload-collage-item__thumb" :src="image.objectUrl" :alt="image.name" />
            <div class="upload-collage-item__meta">
              <span class="upload-collage-item__index">#{{ index + 1 }}</span>
              <span class="upload-collage-item__name" data-testid="upload-collage-item-name">
                {{ image.name }}
              </span>
            </div>
            <button type="button" class="upload-collage-item__remove" @click="handleRemoveImage(image.id)">
              删除
            </button>
          </article>
        </div>
        <div v-else class="upload-collage-empty">暂无图片，先从本地拖几张图片进来试试</div>
      </section>

      <section class="upload-collage-panel upload-collage-panel--preview">
        <div class="upload-collage-settings">
          <div class="upload-collage-settings__head">
            <span class="upload-collage-settings__title">拼图策略</span>
            <span class="upload-collage-settings__count">当前 {{ selectedCount }} 张</span>
          </div>

          <div class="upload-collage-settings__modes">
            <button
              type="button"
              class="upload-collage-settings__mode"
              :class="{ 'is-active': layoutMode === 'auto' }"
              data-testid="upload-collage-auto-trigger"
              @click="layoutMode = 'auto'"
            >
              自动一行 5 张
            </button>
            <button
              type="button"
              class="upload-collage-settings__mode"
              :class="{ 'is-active': layoutMode === 'manual' }"
              data-testid="upload-collage-manual-trigger"
              @click="layoutMode = 'manual'"
            >
              手动输入列数
            </button>
          </div>

          <div class="upload-collage-settings__crop">
            <span class="upload-collage-settings__label">裁剪方式</span>
            <div class="upload-collage-settings__crop-options">
              <button
                type="button"
                class="upload-collage-settings__mode"
                :class="{ 'is-active': cropMode === 'square' }"
                data-testid="upload-collage-crop-square-trigger"
                @click="cropMode = 'square'"
              >
                方图 1:1
              </button>
              <button
                type="button"
                class="upload-collage-settings__mode"
                :class="{ 'is-active': cropMode === 'portrait43' }"
                data-testid="upload-collage-crop-long-trigger"
                @click="cropMode = 'portrait43'"
              >
                长图 4:3
              </button>
            </div>
          </div>

          <label class="upload-collage-settings__manual">
            <span>一行几张</span>
            <input
              data-testid="upload-collage-column-input"
              class="upload-collage-settings__input"
              type="number"
              min="1"
              max="10"
              :disabled="layoutMode !== 'manual'"
              :value="resolvedColumnCount"
              @input="manualColumnCount = Number(($event.target as HTMLInputElement).value || AUTO_COLUMNS)"
            />
          </label>

          <p class="upload-collage-settings__hint">
            每张图都会先按所选比例裁剪，再固定绘制在自己的格子里，不会覆盖到旁边图片。
          </p>

          <div class="upload-collage-settings__actions">
            <el-button
              type="primary"
              :disabled="!canGenerate"
              :loading="generating"
              data-testid="upload-collage-generate"
              @click="handleGenerate"
            >
              {{ generatedPreviewUrl ? '重新生成' : '生成拼图' }}
            </el-button>
            <el-button :disabled="!generatedPreviewUrl" @click="handleOpenPreview">查看大图</el-button>
            <el-button :disabled="!generatedPreviewUrl" @click="handleDownload">保存到本地</el-button>
          </div>
        </div>

        <div class="upload-collage-preview">
          <img
            v-if="generatedPreviewUrl"
            data-testid="upload-collage-preview-image"
            class="upload-collage-preview__image"
            :src="generatedPreviewUrl"
            alt="拼图预览"
          />
          <div v-else class="upload-collage-preview__empty">
            调整好顺序、裁剪方式和排版后，点击“生成拼图”即可在这里预览
          </div>
        </div>
      </section>
    </div>
  </el-dialog>
</template>

<style scoped>
.upload-collage-workbench {
  display: grid;
  grid-template-columns: minmax(0, 420px) minmax(0, 1fr);
  gap: 20px;
  min-height: 620px;
  outline: none;
}

.upload-collage-panel {
  display: flex;
  flex-direction: column;
  gap: 16px;
  min-height: 0;
}

.upload-collage-panel--source,
.upload-collage-panel--preview {
  min-width: 0;
}

.upload-collage-dropzone,
.upload-collage-settings,
.upload-collage-preview,
.upload-collage-empty {
  border: 1px solid rgba(216, 168, 183, 0.42);
  border-radius: 24px;
  background: rgba(255, 250, 247, 0.78);
}

.upload-collage-dropzone {
  position: relative;
  padding: 24px;
  cursor: pointer;
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
}

.upload-collage-dropzone.is-drag-active {
  border-color: rgba(240, 111, 154, 0.72);
  background: rgba(255, 245, 248, 0.96);
  box-shadow: 0 18px 38px rgba(240, 111, 154, 0.12);
}

.upload-collage-dropzone__input {
  display: none;
}

.upload-collage-dropzone__title {
  margin: 0 0 10px;
  font-size: 20px;
  font-weight: 800;
  color: #694e5d;
}

.upload-collage-dropzone__hint,
.upload-collage-settings__hint,
.upload-collage-empty,
.upload-collage-preview__empty {
  margin: 0;
  font-size: 14px;
  line-height: 1.7;
  color: #8b6376;
}

.upload-collage-toolbar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
}

.upload-collage-toolbar__count,
.upload-collage-settings__count,
.upload-collage-settings__label {
  font-size: 14px;
  font-weight: 700;
  color: #8b6376;
}

.upload-collage-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
  max-height: 400px;
  overflow: auto;
  padding-right: 6px;
}

.upload-collage-item {
  display: grid;
  grid-template-columns: 76px minmax(0, 1fr) auto;
  gap: 12px;
  align-items: center;
  padding: 12px;
  border: 1px solid rgba(216, 168, 183, 0.28);
  border-radius: 18px;
  background: rgba(255, 255, 255, 0.74);
}

.upload-collage-item__thumb {
  width: 76px;
  height: 76px;
  object-fit: cover;
  border-radius: 16px;
  background: rgba(249, 239, 243, 0.9);
}

.upload-collage-item__meta {
  display: flex;
  flex-direction: column;
  gap: 6px;
  min-width: 0;
}

.upload-collage-item__index {
  font-size: 12px;
  font-weight: 700;
  color: #b88499;
}

.upload-collage-item__name {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  font-size: 14px;
  font-weight: 700;
  color: #694e5d;
}

.upload-collage-item__remove {
  border: 0;
  background: transparent;
  color: #d96f8e;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.upload-collage-empty,
.upload-collage-settings,
.upload-collage-preview {
  padding: 20px;
}

.upload-collage-settings {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.upload-collage-settings__head,
.upload-collage-settings__crop-options,
.upload-collage-settings__actions {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.upload-collage-settings__head {
  justify-content: space-between;
}

.upload-collage-settings__title {
  font-size: 18px;
  font-weight: 800;
  color: #694e5d;
}

.upload-collage-settings__modes,
.upload-collage-settings__crop {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.upload-collage-settings__mode {
  min-height: 40px;
  padding: 0 16px;
  border: 1px solid rgba(216, 168, 183, 0.42);
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.9);
  color: #8b6376;
  font-size: 14px;
  font-weight: 700;
  cursor: pointer;
  transition: all 0.2s ease;
}

.upload-collage-settings__mode.is-active {
  border-color: rgba(240, 111, 154, 0.72);
  background: linear-gradient(135deg, rgba(255, 143, 177, 0.16), rgba(255, 216, 107, 0.24));
  color: #694e5d;
  box-shadow: 0 10px 22px rgba(240, 111, 154, 0.14);
}

.upload-collage-settings__manual {
  display: flex;
  align-items: center;
  gap: 12px;
  font-size: 14px;
  font-weight: 700;
  color: #694e5d;
}

.upload-collage-settings__input {
  width: 120px;
  min-height: 40px;
  padding: 0 12px;
  border: 1px solid rgba(216, 168, 183, 0.42);
  border-radius: 14px;
  background: rgba(255, 255, 255, 0.92);
  color: #694e5d;
  font-size: 14px;
}

.upload-collage-preview {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 360px;
}

.upload-collage-preview__image {
  display: block;
  max-width: 100%;
  max-height: 680px;
  border-radius: 18px;
  box-shadow: 0 18px 40px rgba(105, 78, 93, 0.12);
}

@media (max-width: 1100px) {
  .upload-collage-workbench {
    grid-template-columns: 1fr;
  }
}
</style>
