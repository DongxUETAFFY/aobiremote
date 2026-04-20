import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import DesktopImageUploadArea from '@/components/common/DesktopImageUploadArea.vue'

const { compressImageBeforeUpload, uploadImage } = vi.hoisted(() => ({
  compressImageBeforeUpload: vi.fn(async (file: File) => ({
    file,
    originalSize: file.size,
    compressedSize: file.size,
    compressed: false,
  })),
  uploadImage: vi.fn(async (file: File, scene: 'private' | 'public') => ({
    data: {
      fileId: `${scene}-file-id`,
      previewUrl: `/api/files/${scene}-file-id/preview`,
      fileSize: file.size,
      contentType: file.type,
    },
  })),
}))

vi.mock('@/utils/image-upload', () => ({
  IMAGE_INPUT_ACCEPT: 'image/png,image/jpeg',
  compressImageBeforeUpload,
  formatFileSize: (bytes: number) => `${bytes} B`,
}))

vi.mock('@/api/file', () => ({
  uploadImage,
}))

describe('DesktopImageUploadArea', () => {
  const buildFile = (name = 'test.png') =>
    new File(['hello'], name, { type: 'image/png' })

  const setInputFiles = async (input: ReturnType<typeof mount>['element'], files: File[]) => {
    Object.defineProperty(input, 'files', {
      configurable: true,
      value: files,
    })
    await input.dispatchEvent(new Event('change'))
  }

  beforeEach(() => {
    vi.clearAllMocks()
  })

  it('uploads immediately after file selection when there is no current image', async () => {
    const wrapper = mount(DesktopImageUploadArea, {
      props: {
        modelValue: '',
        scene: 'private',
        confirmReplace: async () => true,
      },
      slots: {
        preview: '<div data-testid="preview-slot">preview</div>',
      },
    })

    const input = wrapper.get('input[type="file"]')
    await setInputFiles(input.element, [buildFile()])

    await nextTick()
    await Promise.resolve()

    expect(compressImageBeforeUpload).toHaveBeenCalledTimes(1)
    expect(uploadImage).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('uploaded')?.[0]).toEqual(['private-file-id'])
  })

  it('shows a desktop-only announcement for click, drag, and paste upload support', () => {
    const wrapper = mount(DesktopImageUploadArea, {
      props: {
        modelValue: '',
        scene: 'private',
        confirmReplace: async () => true,
      },
    })

    expect(wrapper.text()).toContain('电脑端支持点击、拖拽或粘贴上传图片')
  })

  it('asks for confirmation before replacing an existing image', async () => {
    const confirmReplace = vi.fn(async () => false)
    const wrapper = mount(DesktopImageUploadArea, {
      props: {
        modelValue: 'existing-file-id',
        scene: 'public',
        confirmReplace,
      },
      slots: {
        preview: '<div data-testid="preview-slot">preview</div>',
      },
    })

    const input = wrapper.get('input[type="file"]')
    await setInputFiles(input.element, [buildFile('replace.png')])

    await nextTick()

    expect(confirmReplace).toHaveBeenCalledTimes(1)
    expect(uploadImage).not.toHaveBeenCalled()
    expect(wrapper.emitted('uploaded')).toBeFalsy()
  })

  it('emits uploaded file id after drop confirmation succeeds', async () => {
    const wrapper = mount(DesktopImageUploadArea, {
      props: {
        modelValue: 'existing-file-id',
        scene: 'public',
        confirmReplace: async () => true,
      },
      slots: {
        preview: '<div data-testid="preview-slot">preview</div>',
      },
    })

    await wrapper.get('[data-testid="desktop-upload-area"]').trigger('drop', {
      dataTransfer: {
        files: [buildFile('drop.png')],
      },
    })

    await nextTick()
    await Promise.resolve()

    expect(uploadImage).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('uploaded')?.[0]).toEqual(['public-file-id'])
  })

  it('uploads pasted image after the desktop upload area receives focus', async () => {
    const getAsFile = vi.fn(() => buildFile('paste.png'))
    const wrapper = mount(DesktopImageUploadArea, {
      attachTo: document.body,
      props: {
        modelValue: '',
        scene: 'public',
        confirmReplace: async () => true,
      },
      slots: {
        preview: '<div data-testid="preview-slot">preview</div>',
      },
    })

    const root = wrapper.get('[data-testid="desktop-upload-area"]')
    await root.trigger('focus')
    ;(root.element as HTMLElement).focus()

    await root.trigger('paste', {
      clipboardData: {
        items: [
          {
            type: 'image/png',
            getAsFile,
          },
        ],
      },
    })

    await nextTick()
    await Promise.resolve()

    expect(getAsFile).toHaveBeenCalledTimes(1)
    expect(uploadImage).toHaveBeenCalledTimes(1)
    expect(wrapper.emitted('uploaded')?.[0]).toEqual(['public-file-id'])
  })
})
