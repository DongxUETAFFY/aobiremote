import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import FormOptionButtonGroup from '@/components/common/FormOptionButtonGroup.vue'

describe('FormOptionButtonGroup', () => {
  it('emits the clicked option value', async () => {
    const wrapper = mount(FormOptionButtonGroup, {
      props: {
        modelValue: 'xianyu',
        options: [
          { label: '闲鱼', value: 'xianyu' },
          { label: '贴吧', value: 'tieba' },
          { label: '其他', value: 'other' },
        ],
      },
    })

    await wrapper.findAll('button')[1].trigger('click')

    expect(wrapper.emitted('update:modelValue')).toEqual([['tieba']])
  })
})
