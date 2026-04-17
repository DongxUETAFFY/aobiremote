<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { getImagePreviewObjectUrl, peekImagePreviewObjectUrl } from '@/utils/image-preview-cache'

const props = defineProps<{
  fileId?: string | null
  emptyText?: string
  previewUrl?: string | null
}>()

const loading = ref(false)
const errorText = ref('')
const objectUrl = ref('')
const activeFileId = ref('')

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

watch(
  () => [props.fileId, props.previewUrl],
  async ([nextFileId]) => {
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
    <el-image
      v-if="previewUrl"
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
    <el-image
      v-else
      class="square-preview__image"
      :src="objectUrl"
      :preview-src-list="[objectUrl]"
      preview-teleported
      fit="cover"
    />
  </div>
</template>

<style scoped>
.square-preview {
  width: 168px;
  height: 168px;
  border-radius: 24px;
  overflow: hidden;
  border: 1px solid rgba(216, 168, 183, 0.28);
  background: rgba(255, 255, 255, 0.82);
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
</style>
