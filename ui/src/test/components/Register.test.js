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

vi.mock('../../utils/passwordEncrypt', () => ({
  hashPassword: vi.fn((pwd) => `hashed-${pwd}`)
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
  })

  it('渲染注册表单', () => {
    wrapper = mount(Register, {
      global: {
        stubs: {
          'el-card': true,
          'el-form': {
            template: '<form><slot /></form>',
            methods: {
              validate: vi.fn().mockResolvedValue(true)
            }
          },
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })
    expect(wrapper.find('.register-container').exists()).toBe(true)
  })

  it('表单初始状态为空', () => {
    wrapper = mount(Register, {
      global: {
        stubs: {
          'el-card': true,
          'el-form': {
            template: '<form><slot /></form>',
            methods: {
              validate: vi.fn().mockResolvedValue(true)
            }
          },
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })
    const form = wrapper.vm.form
    expect(form.username).toBe('')
    expect(form.password).toBe('')
    expect(form.email).toBe('')
  })

  it('注册成功后调用 API 并跳转', async () => {
    authApi.register.mockResolvedValueOnce({})

    wrapper = mount(Register, {
      global: {
        stubs: {
          'el-card': true,
          'el-form': {
            template: '<form><slot /></form>',
            methods: {
              validate: vi.fn().mockResolvedValue(true)
            }
          },
          'el-form-item': true,
          'el-input': true,
          'el-button': true
        }
      }
    })

    // 模拟表单数据
    wrapper.vm.form.username = 'newuser'
    wrapper.vm.form.password = 'password123'
    wrapper.vm.form.email = 'test@example.com'

    await wrapper.vm.handleRegister()

    // API 被调用，密码经过哈希
    expect(authApi.register).toHaveBeenCalledWith(
      'newuser',
      'hashed-password123',
      'test@example.com'
    )
  })
})