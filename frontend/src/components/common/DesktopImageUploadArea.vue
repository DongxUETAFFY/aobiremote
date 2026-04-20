<script setup lang="ts">
import { computed, ref } from 'vue'
import { uploadImage } from '@/api/file'
import {
  compressImageBeforeUpload,
  formatFileSize,
  IMAGE_INPUT_ACCEPT,
  type CompressionResult,
} from '@/utils/image-upload'

const props = defineProps<{
  modelValue?: string
  scene: 'private' | 'public'
  uploading?: boolean
  confirmReplace: () => Promise<boolean>
}>()

const emit = defineEmits<{
  (event: 'uploaded', fileId: string): void
  (event: 'uploading-change', value: boolean): void
  (event: 'error', message: string): void
}>()

const fileInputRef = ref<HTMLInputElement | null>(null)
const dragActive = ref(false)
const compressionResult = ref<CompressionResult | null>(null)
const uploadProgress = ref('')
const busy = ref(false)

const hasCurrentImage = computed(() => Boolean(props.modelValue?.trim()))

const summaryText = computed(() => {
  if (!compressionResult.value) {
    return ''
  }

  const { originalSize, compressedSize, compressed } = compressionResult.value
  if (!compressed) {
    return `原图已在 400KB 内，大小 ${formatFileSize(originalSize)}`
  }

  return `原图 ${formatFileSize(originalSize)}，压缩后 ${formatFileSize(compressedSize)}`
})

const isUploading = computed(() => busy.value || Boolean(props.uploading))

const clearInput = () => {
  if (fileInputRef.value) {
    fileInputRef.value.value = ''
  }
}

const extractFirstImage = (files?: FileList | File[] | null) => {
  if (!files?.length) {
    return null
  }

  return Array.from(files).find((file) => file.type.startsWith('image/')) ?? null
}

const emitError = (error: unknown) => {
  const message =
    (error as any)?.response?.data?.message ||
    (error as Error | undefined)?.message ||
    '图片上传失败'
  emit('error', message)
}

const uploadSelectedFile = async (file: File | null) => {
  if (!file || isUploading.value) {
    clearInput()
    return
  }

  if (hasCurrentImage.value) {
    const confirmed = await props.confirmReplace()
    if (!confirmed) {
      clearInput()
      return
    }
  }

  busy.value = true
  emit('uploading-change', true)

  try {
    compressionResult.value = await compressImageBeforeUpload(file)
    const response = await uploadImage(compressionResult.value.file, props.scene)
    uploadProgress.value = `上传成功，fileId: ${response.data.fileId}`
    emit('uploaded', response.data.fileId)
  } catch (error) {
    compressionResult.value = null
    uploadProgress.value = ''
    emitError(error)
  } finally {
    busy.value = false
    emit('uploading-change', false)
    clearInput()
  }
}

const handlePick = () => {
  if (isUploading.value) {
    return
  }

  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  await uploadSelectedFile(extractFirstImage(input.files))
}

const handleDrop = async (event: DragEvent) => {
  event.preventDefault()
  dragActive.value = false
  await uploadSelectedFile(extractFirstImage(event.dataTransfer?.files))
}

const handleDragOver = (event: DragEvent) => {
  event.preventDefault()
  if (!isUploading.value) {
    dragActive.value = true
  }
}

const handleDragLeave = (event: DragEvent) => {
  event.preventDefault()
  dragActive.value = false
}

const handlePaste = async (event: ClipboardEvent) => {
  const file = Array.from(event.clipboardData?.items ?? [])
    .filter((item) => item.type.startsWith('image/'))
    .map((item) => item.getAsFile())
    .find((item): item is File => Boolean(item))

  if (!file) {
    return
  }

  event.preventDefault()
  await uploadSelectedFile(file)
}
</script>

<template>
  <div
    class="desktop-upload-area"
    :class="{ 'is-drag-active': dragActive, 'is-uploading': isUploading }"
    data-testid="desktop-upload-area"
    tabindex="0"
    @dragover="handleDragOver"
    @dragleave="handleDragLeave"
    @drop="handleDrop"
    @paste="handlePaste"
  >
    <input
      ref="fileInputRef"
      type="file"
      class="desktop-upload-area__input"
      :accept="IMAGE_INPUT_ACCEPT"
      :disabled="isUploading"
      @change="handleFileChange"
    />

    <div class="desktop-upload-area__preview">
      <slot name="preview">
        <div class="desktop-upload-area__placeholder">选择图片</div>
      </slot>
    </div>

    <div class="desktop-upload-area__controls">
      <div class="desktop-upload-area__notice" role="note">
        <strong class="desktop-upload-area__notice-title">桌面端上传已升级</strong>
        <p class="desktop-upload-area__notice-text">电脑端支持点击、拖拽或粘贴上传图片</p>
      </div>

      <button
        type="button"
        class="desktop-upload-area__button"
        :disabled="isUploading"
        @click.stop="handlePick"
      >
        {{ isUploading ? '上传中...' : '选择图片' }}
      </button>

      <p class="desktop-upload-area__hint">支持选择后自动压缩上传，已有图片会先确认再替换</p>
      <p v-if="summaryText" class="desktop-upload-area__summary">{{ summaryText }}</p>
      <p v-if="uploadProgress" class="desktop-upload-area__progress">{{ uploadProgress }}</p>
    </div>
  </div>
</template>

<style scoped>
.desktop-upload-area {
  display: flex;
  gap: 16px;
  align-items: flex-start;
  padding: 14px;
  border: 1px dashed rgba(216, 168, 183, 0.4);
  border-radius: 24px;
  background: rgba(255, 250, 247, 0.66);
  transition: border-color 0.2s ease, background 0.2s ease, box-shadow 0.2s ease;
  outline: none;
}

.desktop-upload-area:focus-visible {
  border-color: #cf5d75;
  box-shadow: 0 0 0 3px rgba(207, 93, 117, 0.16);
}

.desktop-upload-area.is-drag-active {
  border-color: #cf5d75;
  background: rgba(255, 244, 247, 0.95);
}

.desktop-upload-area.is-uploading {
  opacity: 0.85;
}

.desktop-upload-area__input {
  display: none;
}

.desktop-upload-area__preview {
  flex-shrink: 0;
}

.desktop-upload-area__placeholder {
  width: 168px;
  height: 168px;
  border-radius: 24px;
  border: 1px dashed rgba(216, 168, 183, 0.4);
  display: grid;
  place-items: center;
  color: #b8a0ac;
  background: rgba(255, 255, 255, 0.82);
}

.desktop-upload-area__controls {
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-height: 168px;
  justify-content: center;
}

.desktop-upload-area__notice {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 10px 12px;
  border-radius: 14px;
  background: linear-gradient(135deg, rgba(255, 244, 247, 0.98), rgba(255, 236, 241, 0.92));
  border: 1px solid rgba(207, 93, 117, 0.18);
}

.desktop-upload-area__notice-title,
.desktop-upload-area__notice-text {
  margin: 0;
}

.desktop-upload-area__notice-title {
  font-size: 13px;
  color: #b14462;
}

.desktop-upload-area__notice-text {
  font-size: 13px;
  line-height: 1.5;
  color: #7a4f5e;
}

.desktop-upload-area__button {
  align-self: flex-start;
  min-width: 112px;
  height: 36px;
  padding: 0 16px;
  border: 0;
  border-radius: 999px;
  background: linear-gradient(135deg, var(--ah-accent) 0%, var(--ah-accent-deep) 100%);
  color: #fff;
  cursor: pointer;
  font-size: 14px;
}

.desktop-upload-area__button:disabled {
  cursor: not-allowed;
  opacity: 0.72;
}

.desktop-upload-area__hint,
.desktop-upload-area__summary,
.desktop-upload-area__progress {
  margin: 0;
  font-size: 13px;
  line-height: 1.5;
}

.desktop-upload-area__hint,
.desktop-upload-area__summary {
  color: var(--ah-text);
}

.desktop-upload-area__progress {
  color: #4caf7d;
}

@media (max-width: 640px) {
  .desktop-upload-area {
    gap: 12px;
    padding: 12px;
  }

  .desktop-upload-area__preview {
    --square-preview-size: 96px;
    --square-preview-radius: 16px;
  }

  .desktop-upload-area__placeholder {
    width: 96px;
    height: 96px;
    border-radius: 16px;
    font-size: 12px;
  }

  .desktop-upload-area__controls {
    min-height: 96px;
  }

  .desktop-upload-area__notice {
    padding: 8px 10px;
  }

  .desktop-upload-area__notice-title,
  .desktop-upload-area__notice-text,
  .desktop-upload-area__hint,
  .desktop-upload-area__summary,
  .desktop-upload-area__progress {
    font-size: 12px;
  }
}
</style>
