<script setup lang="ts">
import { onBeforeUnmount, ref, watch } from 'vue'
import { fetchImagePreviewBlob } from '@/api/file'

const props = defineProps<{
  fileId?: string | null
  emptyText?: string
}>()

const loading = ref(false)
const errorText = ref('')
const objectUrl = ref('')

const cleanupObjectUrl = () => {
  if (objectUrl.value) {
    URL.revokeObjectURL(objectUrl.value)
    objectUrl.value = ''
  }
}

const loadPreview = async (fileId?: string | null) => {
  cleanupObjectUrl()
  errorText.value = ''

  if (!fileId) {
    return
  }

  loading.value = true
  try {
    const blob = await fetchImagePreviewBlob(fileId)
    objectUrl.value = URL.createObjectURL(blob)
  } catch (error: any) {
    errorText.value = error?.response?.data?.message || '图片加载失败'
  } finally {
    loading.value = false
  }
}

watch(
  () => props.fileId,
  async (nextFileId) => {
    await loadPreview(nextFileId)
  },
  { immediate: true },
)

onBeforeUnmount(() => {
  cleanupObjectUrl()
})
</script>

<template>
  <div class="square-preview">
    <div v-if="loading" class="square-preview__placeholder">加载中...</div>
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
