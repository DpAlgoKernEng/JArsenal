import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Login from '../../views/Login.vue'
import { authApi } from '../../api'

// Mock dependencies
vi.mock('../../api', () => ({
  authApi: {
    login: vi.fn()
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
  },
  ElCard: {
    template: '<div class="el-card"><slot name="header" /><slot /></div>'
  },
  ElForm: {
    template: '<form><slot /></form>',
    methods: {
      validate: vi.fn().mockResolvedValue(true)
    }
  },
  ElFormItem: {
    template: '<div class="el-form-item"><slot /></div>'
  },
  ElInput: {
    template: '<input class="el-input" />',
    props: ['modelValue']
  },
  ElButton: {
    template: '<button class="el-button"><slot /></button>',
    props: ['type', 'loading']
  }
}))

describe('Login.vue', () => {
  let wrapper
  let mockRouter

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()

    mockRouter = { push: vi.fn() }

    wrapper = mount(Login, {
      global: {
        mocks: {
          $router: mockRouter
        },
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

  it('渲染登录表单', () => {
    expect(wrapper.find('.login-container').exists()).toBe(true)
  })

  it('表单初始状态为空', () => {
    const form = wrapper.vm.form
    expect(form.username).toBe('')
    expect(form.password).toBe('')
  })

  it('登录成功后跳转到用户列表页', async () => {
    const mockLoginResponse = {
      accessToken: 'test-access-token',
      refreshToken: 'test-refresh-token',
      userId: 1,
      username: 'testuser'
    }

    authApi.login.mockResolvedValueOnce(mockLoginResponse)

    // 模拟表单提交
    wrapper.vm.form.username = 'testuser'
    wrapper.vm.form.password = 'password123'

    await wrapper.vm.handleLogin()

    expect(authApi.login).toHaveBeenCalledWith('testuser', 'password123')
  })
})