import { beforeEach, describe, expect, it, vi } from 'vitest'
import { mount } from '@vue/test-utils'
import { nextTick } from 'vue'
import RegisterView from '@/views/auth/RegisterView.vue'

const pushMock = vi.fn()
const warningMock = vi.fn()
const successMock = vi.fn()
const errorMock = vi.fn()
const sendRegisterCodeMock = vi.fn()
const registerMock = vi.fn()

vi.mock('element-plus', () => ({
  ElMessage: {
    warning: (...args: unknown[]) => warningMock(...args),
    success: (...args: unknown[]) => successMock(...args),
    error: (...args: unknown[]) => errorMock(...args),
  },
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: pushMock,
  }),
}))

vi.mock('@/stores/auth', () => ({
  useAuthStore: () => ({
    sendRegisterCode: sendRegisterCodeMock,
    register: registerMock,
  }),
}))

describe('RegisterView', () => {
  beforeEach(() => {
    pushMock.mockReset()
    warningMock.mockReset()
    successMock.mockReset()
    errorMock.mockReset()
    sendRegisterCodeMock.mockReset()
    registerMock.mockReset()
  })

  const mountView = () =>
    mount(RegisterView, {
      global: {
        stubs: {
          ElForm: { template: '<form><slot /></form>' },
          ElFormItem: { template: '<label><slot /></label>' },
          ElInput: {
            props: ['modelValue'],
            emits: ['update:modelValue'],
            template:
              '<input :value="modelValue" @input="$emit(\'update:modelValue\', $event.target.value)" />',
          },
          ElButton: {
            props: ['disabled', 'loading'],
            emits: ['click'],
            template:
              '<button :disabled="disabled" @click="$emit(\'click\', $event)"><slot /></button>',
          },
        },
      },
    })

  it('blocks submit when the verification code is not six digits', async () => {
    const wrapper = mountView()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('tester@example.com')
    await inputs[1].setValue('12ab')
    await inputs[2].setValue('abc12345')
    await inputs[3].setValue('abc12345')
    await wrapper.find('.auth-form__submit').trigger('click')
    await nextTick()

    expect(registerMock).not.toHaveBeenCalled()
    expect(warningMock).toHaveBeenCalledWith('验证码必须是 6 位数字')
  })

  it('blocks submit when the password does not contain both letters and numbers', async () => {
    const wrapper = mountView()
    const inputs = wrapper.findAll('input')

    await inputs[0].setValue('tester@example.com')
    await inputs[1].setValue('123456')
    await inputs[2].setValue('abcdefgh')
    await inputs[3].setValue('abcdefgh')
    await wrapper.find('.auth-form__submit').trigger('click')
    await nextTick()

    expect(registerMock).not.toHaveBeenCalled()
    expect(warningMock).toHaveBeenCalledWith('密码必须同时包含字母和数字')
  })
})
