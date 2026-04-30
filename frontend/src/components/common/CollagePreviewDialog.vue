<script setup lang="ts">
import { computed } from 'vue'

const props = defineProps<{
  visible: boolean
  imageUrl: string
  layoutMode: 'auto' | '3' | '4' | '5'
  selectedCount: number
  mobile: boolean
  generating: boolean
}>()

const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
}>()

const dialogVisible = computed({
  get: () => props.visible,
  set: (value: boolean) => emit('update:visible', value),
})

const layoutLabel = computed(() => {
  switch (props.layoutMode) {
    case '3':
      return '每行 3 张'
    case '4':
      return '每行 4 张'
    case '5':
      return '每行 5 张'
    default:
      return '自动排布'
  }
})

const fileName = computed(() => `aobi-collage-${props.selectedCount}.png`)

const handleDownload = () => {
  if (!props.imageUrl) {
    return
  }
  const link = document.createElement('a')
  link.href = props.imageUrl
  link.download = fileName.value
  link.click()
}

const handleMobileSave = () => {
  if (!props.imageUrl) {
    return
  }
  window.open(props.imageUrl, '_blank', 'noopener')
}
</script>

<template>
  <el-dialog
    v-model="dialogVisible"
    title="拼图预览"
    :width="mobile ? '100%' : '760px'"
    :fullscreen="mobile"
    append-to-body
  >
    <div class="collage-preview">
      <div class="collage-preview__meta">
        <span>已选 {{ selectedCount }} 张</span>
        <span>{{ layoutLabel }}</span>
      </div>
      <div class="collage-preview__canvas-frame">
        <img
          v-if="imageUrl"
          class="collage-preview__image"
          :src="imageUrl"
          alt="拼图预览"
        />
        <div v-else class="collage-preview__empty">暂无可预览的拼图</div>
      </div>
      <p v-if="mobile" class="collage-preview__hint">长按图片可保存</p>
    </div>

    <template #footer>
      <el-button @click="dialogVisible = false">关闭</el-button>
      <el-button
        v-if="mobile"
        type="primary"
        :disabled="!imageUrl || generating"
        @click="handleMobileSave"
      >
        保存图片
      </el-button>
      <el-button
        v-else
        type="primary"
        :disabled="!imageUrl || generating"
        @click="handleDownload"
      >
        下载图片
      </el-button>
    </template>
  </el-dialog>
</template>

<style scoped>
.collage-preview {
  display: grid;
  gap: 16px;
}

.collage-preview__meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 12px;
  color: var(--ah-text);
  font-size: 14px;
  flex-wrap: wrap;
}

.collage-preview__canvas-frame {
  padding: 16px;
  border-radius: 24px;
  background: linear-gradient(135deg, rgba(255, 250, 247, 0.95), rgba(255, 255, 255, 0.98));
  border: 1px solid rgba(216, 168, 183, 0.2);
  display: grid;
  place-items: center;
  min-height: 260px;
}

.collage-preview__image {
  display: block;
  width: 100%;
  max-height: 70vh;
  object-fit: contain;
  border-radius: 18px;
  background: #fff;
}

.collage-preview__empty {
  color: #8d7080;
  font-size: 14px;
}

.collage-preview__hint {
  margin: 0;
  color: #8d7080;
  font-size: 13px;
  text-align: center;
}
</style>
