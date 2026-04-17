<script setup lang="ts">
import { computed, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { uploadImage } from '@/api/file'
import SquareImagePreview from '@/components/common/SquareImagePreview.vue'
import {
  compressImageBeforeUpload,
  formatFileSize,
  IMAGE_INPUT_ACCEPT,
  type CompressionResult,
} from '@/utils/image-upload'

const fileInputRef = ref<HTMLInputElement | null>(null)
const selectedFileName = ref('')
const compressionResult = ref<CompressionResult | null>(null)
const uploading = ref(false)
const uploadedFileId = ref('')
const uploadedPreviewUrl = ref('')
const scene = ref<'private' | 'public'>('private')

const selectedSummary = computed(() => {
  if (!compressionResult.value) {
    return ''
  }

  const { originalSize, compressedSize, compressed } = compressionResult.value
  if (!compressed) {
    return `原图已在 400KB 内，大小 ${formatFileSize(originalSize)}`
  }

  return `原图 ${formatFileSize(originalSize)}，压缩后 ${formatFileSize(compressedSize)}`
})

const handlePickClick = () => {
  fileInputRef.value?.click()
}

const handleFileChange = async (event: Event) => {
  const input = event.target as HTMLInputElement
  const file = input.files?.[0]
  uploadedFileId.value = ''
  uploadedPreviewUrl.value = ''

  if (!file) {
    compressionResult.value = null
    selectedFileName.value = ''
    return
  }

  try {
    selectedFileName.value = file.name
    compressionResult.value = await compressImageBeforeUpload(file)
    ElMessage.success('图片已处理完成，可以上传')
  } catch (error: any) {
    compressionResult.value = null
    selectedFileName.value = ''
    ElMessage.error(error?.message || '图片处理失败')
  } finally {
    input.value = ''
  }
}

const handleUpload = async () => {
  if (!compressionResult.value) {
    ElMessage.warning('请先选择图片')
    return
  }

  uploading.value = true
  try {
    const response = await uploadImage(compressionResult.value.file, scene.value)
    uploadedFileId.value = response.data.fileId
    uploadedPreviewUrl.value = response.data.previewUrl
    ElMessage.success('图片上传成功，点击缩略图可查看全貌')
  } catch (error: any) {
    ElMessage.error(error?.response?.data?.message || '图片上传失败')
  } finally {
    uploading.value = false
  }
}
</script>

<template>
  <section class="upload-demo ah-glass-card ah-page-section">
    <div class="upload-demo__header">
      <div>
        <p class="upload-demo__eyebrow">图片上传最小闭环</p>
        <h2>先把上传、压缩、预览跑通</h2>
        <p class="upload-demo__desc">
          列表里仍然保持方形缩略图，点击缩略图后可以查看压缩后的完整图片。当前前端会尽量压到 400KB 内，若图片本身复杂，则允许落在 500KB 内。
        </p>
      </div>
      <SquareImagePreview :file-id="uploadedFileId" empty-text="上传后会显示缩略图" />
    </div>

    <div class="upload-demo__controls">
      <input
        ref="fileInputRef"
        class="upload-demo__native-input"
        type="file"
        :accept="IMAGE_INPUT_ACCEPT"
        @change="handleFileChange"
      />

      <div class="upload-demo__control-card">
        <span class="upload-demo__label">1. 选择图片</span>
        <div class="upload-demo__actions">
          <el-button type="primary" plain @click="handlePickClick">选择图片</el-button>
          <span class="upload-demo__filename">{{ selectedFileName || '暂未选择文件' }}</span>
        </div>
      </div>

      <div class="upload-demo__control-card">
        <span class="upload-demo__label">2. 选择上传场景</span>
        <el-radio-group v-model="scene">
          <el-radio-button label="private">私有页图片</el-radio-button>
          <el-radio-button label="public">公开区图片</el-radio-button>
        </el-radio-group>
      </div>

      <div class="upload-demo__control-card">
        <span class="upload-demo__label">3. 上传并返回 fileId</span>
        <div class="upload-demo__actions">
          <el-button type="primary" :loading="uploading" @click="handleUpload">上传图片</el-button>
          <span class="upload-demo__summary">{{ selectedSummary || '选择图片后这里会显示压缩结果' }}</span>
        </div>
      </div>
    </div>

    <div class="upload-demo__result">
      <div class="upload-demo__result-card">
        <span class="upload-demo__result-label">返回的 fileId</span>
        <strong>{{ uploadedFileId || '暂未上传' }}</strong>
      </div>
      <div class="upload-demo__result-card">
        <span class="upload-demo__result-label">预览地址</span>
        <strong class="upload-demo__url">{{ uploadedPreviewUrl || '暂未生成' }}</strong>
      </div>
    </div>
  </section>
</template>

<style scoped>
.upload-demo__header {
  display: grid;
  grid-template-columns: minmax(0, 1fr) 168px;
  gap: 24px;
  align-items: start;
}

.upload-demo__eyebrow {
  margin: 0 0 10px;
  color: #bb8d54;
  font-size: 13px;
  font-weight: 700;
  letter-spacing: 0.12em;
}

.upload-demo h2 {
  margin: 0;
  color: var(--ah-title);
}

.upload-demo__desc {
  margin: 12px 0 0;
  color: var(--ah-text);
}

.upload-demo__controls {
  margin-top: 24px;
  display: grid;
  gap: 14px;
}

.upload-demo__control-card,
.upload-demo__result-card {
  padding: 18px 20px;
  border-radius: 22px;
  border: 1px solid rgba(216, 168, 183, 0.18);
  background: rgba(255, 255, 255, 0.72);
}

.upload-demo__label,
.upload-demo__result-label {
  display: block;
  margin-bottom: 10px;
  color: #8d7080;
  font-size: 13px;
  font-weight: 700;
}

.upload-demo__actions {
  display: flex;
  align-items: center;
  gap: 14px;
  flex-wrap: wrap;
}

.upload-demo__filename,
.upload-demo__summary,
.upload-demo__url {
  color: var(--ah-text);
  word-break: break-all;
}

.upload-demo__result {
  margin-top: 18px;
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 14px;
}

.upload-demo__native-input {
  display: none;
}

@media (max-width: 768px) {
  .upload-demo__header {
    grid-template-columns: 1fr;
  }

  .upload-demo__result {
    grid-template-columns: 1fr;
  }
}
</style>
