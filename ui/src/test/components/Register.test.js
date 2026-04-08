import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Register from '../../views/Register.vue'
import { authApi } from '../../api'

// Mock dependencies
vi.mock('../../api', () => ({
  authApi: {
    register: vi.fn()
  }
}))

vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: vi.fn()
  })
}))

vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('Register.vue', () => {
  let wrapper

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    wrapper = mount(Register, {
      global: {
        stubs: {
          'el-card': true,
          'el-form': true,
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })
  })

  it('渲染注册表单', () => {
    expect(wrapper.find('.register-container').exists()).toBe(true)
  })

  it('表单初始状态为空', () => {
    const form = wrapper.vm.form
    expect(form.username).toBe('')
    expect(form.password).toBe('')
    expect(form.email).toBe('')
  })

  it('注册成功后跳转到登录页', async () => {
    authApi.register.mockResolvedValueOnce({})

    wrapper.vm.form.username = 'newuser'
    wrapper.vm.form.password = 'password123'
    wrapper.vm.form.email = 'test@example.com'

    await wrapper.vm.handleRegister()

    expect(authApi.register).toHaveBeenCalledWith(
      'newuser',
      'password123',
      'test@example.com'
    )
  })
})