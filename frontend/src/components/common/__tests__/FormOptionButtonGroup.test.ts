import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import FormOptionButtonGroup from '@/components/common/FormOptionButtonGroup.vue'

describe('FormOptionButtonGroup', () => {
  it('emits the clicked option value', async () => {
    const wrapper = mount(FormOptionButtonGroup, {
      props: {
        modelValue: 'xianyu',
        label: '渠道',
        options: [
          { label: '闲鱼', value: 'xianyu' },
          { label: '贴吧', value: 'tieba' },
          { label: '其他', value: 'other' },
        ],
      },
    })

    const buttons = wrapper.findAll('button')

    expect(wrapper.attributes('role')).toBe('group')
    expect(wrapper.attributes('aria-label')).toBe('渠道')
    expect(buttons[0].attributes('aria-pressed')).toBe('true')

    await buttons[1].trigger('click')

    expect(wrapper.emitted('update:modelValue')).toEqual([['tieba']])
  })
})
