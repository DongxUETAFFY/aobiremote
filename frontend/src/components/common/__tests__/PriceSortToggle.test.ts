import { describe, expect, it } from 'vitest'
import { mount } from '@vue/test-utils'
import PriceSortToggle from '@/components/common/PriceSortToggle.vue'

describe('PriceSortToggle', () => {
  it('emits ascending, descending, and default when active sort is clicked again', async () => {
    const wrapper = mount(PriceSortToggle, {
      props: {
        modelValue: 'default',
        ascValue: 'priceAsc',
        descValue: 'priceDesc',
      },
    })

    const buttons = wrapper.findAll('button')

    expect(buttons).toHaveLength(2)

    await buttons[0].trigger('click')
    await buttons[1].trigger('click')
    await wrapper.setProps({ modelValue: 'priceDesc' })
    await buttons[1].trigger('click')

    expect(wrapper.emitted('update:modelValue')).toEqual([
      ['priceAsc'],
      ['priceDesc'],
      ['default'],
    ])
    expect(wrapper.emitted('change')).toEqual([
      ['priceAsc'],
      ['priceDesc'],
      ['default'],
    ])
  })
})
