<script setup lang="ts">
import { computed, onBeforeUnmount, ref, watch } from 'vue'
import { getImagePreviewObjectUrl, peekImagePreviewObjectUrl } from '@/utils/image-preview-cache'

const props = defineProps<{
  fileId?: string | null
  emptyText?: string
  previewUrl?: string | null
  enableMobileLongPressSave?: boolean
}>()

const loading = ref(false)
const errorText = ref('')
const objectUrl = ref('')
const activeFileId = ref('')
const nativePreviewVisible = ref(false)

const resolvedPreviewUrl = computed(() => {
  const normalizedPreviewUrl = props.previewUrl?.trim()
  if (normalizedPreviewUrl) {
    return normalizedPreviewUrl
  }
  return objectUrl.value
})

const isTouchDevice = computed(() => {
  if (typeof window === 'undefined') {
    return false
  }
  return window.matchMedia('(pointer: coarse)').matches || navigator.maxTouchPoints > 0
})

const useNativeMobilePreview = computed(() => Boolean(
  props.enableMobileLongPressSave
  && isTouchDevice.value
  && resolvedPreviewUrl.value,
))

const loadPreview = async (fileId?: string | null) => {
  if (props.previewUrl?.trim()) {
    loading.value = false
    errorText.value = ''
    objectUrl.value = ''
    return
  }

  const normalizedFileId = fileId?.trim() || ''
  activeFileId.value = normalizedFileId
  errorText.value = ''
  objectUrl.value = ''

  if (!normalizedFileId) {
    loading.value = false
    return
  }

  const cachedObjectUrl = peekImagePreviewObjectUrl(normalizedFileId)
  if (cachedObjectUrl) {
    objectUrl.value = cachedObjectUrl
    loading.value = false
    return
  }

  loading.value = true
  try {
    const nextObjectUrl = await getImagePreviewObjectUrl(normalizedFileId)
    if (activeFileId.value === normalizedFileId) {
      objectUrl.value = nextObjectUrl
    }
  } catch (error: any) {
    if (activeFileId.value === normalizedFileId) {
      errorText.value = error?.response?.data?.message || '图片加载失败'
    }
  } finally {
    if (activeFileId.value === normalizedFileId) {
      loading.value = false
    }
  }
}

const openNativePreview = () => {
  if (!useNativeMobilePreview.value) {
    return
  }
  nativePreviewVisible.value = true
}

watch(
  () => [props.fileId, props.previewUrl],
  async ([nextFileId]) => {
    nativePreviewVisible.value = false
    await loadPreview(nextFileId)
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  activeFileId.value = ''
})
</script>

<template>
  <div class="square-preview">
    <button
      v-if="useNativeMobilePreview"
      class="square-preview__button"
      type="button"
      @click="openNativePreview"
    >
      <el-image
        class="square-preview__image"
        :src="resolvedPreviewUrl"
        fit="cover"
      >
        <template #placeholder>
          <div class="square-preview__placeholder">加载中...</div>
        </template>
        <template #error>
          <div class="square-preview__placeholder is-error">图片加载失败</div>
        </template>
      </el-image>
    </button>
    <el-image
      v-else-if="previewUrl"
      class="square-preview__image"
      :src="previewUrl"
      :preview-src-list="[previewUrl]"
      preview-teleported
      fit="cover"
    >
      <template #placeholder>
        <div class="square-preview__placeholder">加载中...</div>
      </template>
      <template #error>
        <div class="square-preview__placeholder is-error">图片加载失败</div>
      </template>
    </el-image>
    <div v-else-if="loading" class="square-preview__placeholder">加载中...</div>
    <div v-else-if="errorText" class="square-preview__placeholder is-error">{{ errorText }}</div>
    <div v-else-if="!objectUrl" class="square-preview__placeholder">{{ emptyText || '暂无图片' }}</div>
    <button
      v-else-if="useNativeMobilePreview"
      class="square-preview__button"
      type="button"
      @click="openNativePreview"
    >
      <el-image
        class="square-preview__image"
        :src="objectUrl"
        fit="cover"
      />
    </button>
    <el-image
      v-else
      class="square-preview__image"
      :src="objectUrl"
      :preview-src-list="[objectUrl]"
      preview-teleported
      fit="cover"
    />
  </div>
  <el-dialog
    v-model="nativePreviewVisible"
    class="square-preview-dialog"
    :show-close="false"
    append-to-body
    fullscreen
    destroy-on-close
  >
    <div class="square-preview-dialog__content">
      <button
        class="square-preview-dialog__close"
        type="button"
        @click="nativePreviewVisible = false"
      >
        关闭
      </button>
      <p class="square-preview-dialog__hint">长按图片可保存</p>
      <img
        v-if="resolvedPreviewUrl"
        class="square-preview-dialog__image"
        :src="resolvedPreviewUrl"
        alt="预览图片"
      >
    </div>
  </el-dialog>
</template>

<style scoped>
.square-preview {
  width: var(--square-preview-size, 168px);
  height: var(--square-preview-size, 168px);
  border-radius: var(--square-preview-radius, 24px);
  overflow: hidden;
  border: 1px solid rgba(216, 168, 183, 0.28);
  background: rgba(255, 255, 255, 0.82);
}

.square-preview__button {
  width: 100%;
  height: 100%;
  padding: 0;
  border: 0;
  background: transparent;
  cursor: pointer;
}

.square-preview__placeholder {
  width: 100%;
  height: 100%;
  display: grid;
  place-items: center;
  padding: 18px;
  color: #8d7080;
  font-size: 14px;
  text-align: center;
  background: linear-gradient(180deg, rgba(255, 250, 247, 0.95), rgba(255, 238, 244, 0.95));
}

.square-preview__placeholder.is-error {
  color: #cf5d75;
}

.square-preview__image {
  width: 100%;
  height: 100%;
  display: block;
}

.square-preview-dialog__content {
  position: relative;
  width: 100%;
  height: 100%;
  display: grid;
  grid-template-rows: auto auto 1fr;
  justify-items: center;
  align-items: center;
  padding: 18px 18px max(28px, env(safe-area-inset-bottom));
  background: rgba(17, 10, 14, 0.96);
}

.square-preview-dialog__close {
  justify-self: end;
  padding: 8px 14px;
  border: 0;
  border-radius: 999px;
  background: rgba(255, 255, 255, 0.16);
  color: #fff;
  font-size: 14px;
}

.square-preview-dialog__hint {
  margin: 10px 0 14px;
  color: rgba(255, 255, 255, 0.82);
  font-size: 13px;
}

.square-preview-dialog__image {
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  user-select: none;
  -webkit-user-drag: none;
}
</style>

<style>
.square-preview-dialog {
  padding: 0;
  background: transparent;
}

.square-preview-dialog .el-dialog__header,
.square-preview-dialog .el-dialog__body {
  margin: 0;
  padding: 0;
}
</style>
