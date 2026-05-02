import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import UploadCollageWorkbenchDialog from '@/components/common/UploadCollageWorkbenchDialog.vue'

const renderCollageImageMock = vi.fn()

vi.mock('@/utils/warehouse-collage', () => ({
  renderCollageImage: (...args: unknown[]) => renderCollageImageMock(...args),
}))

describe('UploadCollageWorkbenchDialog', () => {
  const createObjectURLMock = vi.fn()
  const revokeObjectURLMock = vi.fn()

  beforeEach(() => {
    renderCollageImageMock.mockReset()
    createObjectURLMock.mockReset()
    revokeObjectURLMock.mockReset()

    let objectUrlIndex = 0
    createObjectURLMock.mockImplementation(() => `blob:mock-${++objectUrlIndex}`)

    vi.stubGlobal('URL', {
      createObjectURL: createObjectURLMock,
      revokeObjectURL: revokeObjectURLMock,
    })
  })

  const mountDialog = () =>
    mount(UploadCollageWorkbenchDialog, {
      props: {
        visible: true,
      },
      global: {
        stubs: {
          ElDialog: {
            props: ['modelValue', 'closeOnClickModal', 'closeOnPressEscape'],
            template:
              '<div v-if="modelValue" data-testid="dialog-shell" :data-close-on-click-modal="String(closeOnClickModal)" :data-close-on-press-escape="String(closeOnPressEscape)"><slot /><slot name="footer" /></div>',
          },
          ElButton: {
            template: '<button><slot /></button>',
          },
          ElRadioGroup: {
            props: ['modelValue'],
            template: '<div><slot /></div>',
          },
          ElRadioButton: {
            template: '<button><slot /></button>',
          },
          ElInputNumber: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template: '<input type="number" :value="modelValue" @input="$emit(\'update:modelValue\', Number(($event.target as HTMLInputElement).value))" />',
          },
        },
      },
    })

  it('imports local images, reorders them, and resets when closed', async () => {
    const wrapper = mountDialog()
    const files = [
      new File(['a'], 'a.png', { type: 'image/png' }),
      new File(['b'], 'b.png', { type: 'image/png' }),
      new File(['c'], 'c.png', { type: 'image/png' }),
    ]

    const fileInput = wrapper.get('[data-testid="upload-collage-file-input"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: files,
      configurable: true,
    })
    await fileInput.trigger('change')

    expect(wrapper.findAll('[data-testid="upload-collage-item-name"]')).toHaveLength(3)
    expect(wrapper.findAll('[data-testid="upload-collage-item-name"]')[0].text()).toContain('a.png')
    expect(createObjectURLMock).toHaveBeenCalledTimes(3)

    const items = wrapper.findAll('[data-testid="upload-collage-item"]')
    await items[2].trigger('dragstart')
    await items[0].trigger('drop')

    expect(wrapper.findAll('[data-testid="upload-collage-item-name"]')[0].text()).toContain('c.png')

    await wrapper.setProps({ visible: false })

    expect(revokeObjectURLMock).toHaveBeenCalled()
    expect(wrapper.findAll('[data-testid="upload-collage-item-name"]')).toHaveLength(0)
  })

  it('uses the manual column count when generating a collage', async () => {
    renderCollageImageMock.mockResolvedValue({
      blob: new Blob(['preview'], { type: 'image/png' }),
      objectUrl: 'blob:generated-preview',
      layout: {
        columns: 8,
        rows: 1,
        cellSize: 100,
        gap: 16,
        padding: 24,
        canvasWidth: 1000,
        canvasHeight: 148,
      },
    })

    const wrapper = mountDialog()
    const files = [
      new File(['a'], 'a.png', { type: 'image/png' }),
      new File(['b'], 'b.png', { type: 'image/png' }),
    ]

    const fileInput = wrapper.get('[data-testid="upload-collage-file-input"]')
    Object.defineProperty(fileInput.element, 'files', {
      value: files,
      configurable: true,
    })
    await fileInput.trigger('change')

    await wrapper.get('[data-testid="upload-collage-manual-trigger"]').trigger('click')
    await wrapper.get('[data-testid="upload-collage-crop-long-trigger"]').trigger('click')
    await wrapper.get('[data-testid="upload-collage-column-input"]').setValue('8')
    await wrapper.get('[data-testid="upload-collage-generate"]').trigger('click')

    expect(renderCollageImageMock).toHaveBeenCalledWith({
      items: [
        { imageUrl: 'blob:mock-1' },
        { imageUrl: 'blob:mock-2' },
      ],
      mode: '8',
      cropMode: 'portrait43',
      gap: 4,
      padding: 12,
      maxColumns: 10,
      autoColumns: 5,
    })
    expect(wrapper.get('[data-testid="upload-collage-preview-image"]').attributes('src')).toBe(
      'blob:generated-preview',
    )
  })

  it('disables closing the workbench by clicking the overlay or pressing escape', () => {
    const wrapper = mountDialog()
    const dialogShell = wrapper.get('[data-testid="dialog-shell"]')

    expect(dialogShell.attributes('data-close-on-click-modal')).toBe('false')
    expect(dialogShell.attributes('data-close-on-press-escape')).toBe('false')
  })
})
