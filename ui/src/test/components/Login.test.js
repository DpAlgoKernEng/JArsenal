import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Login from '../../views/Login.vue'
import { authApi } from '../../api'
import { useUserStore } from '../../stores/user'

// Mock dependencies
vi.mock('../../api', () => ({
  authApi: {
    login: vi.fn()
  }
}))

vi.mock('../../utils/passwordEncrypt', () => ({
  hashPassword: vi.fn((pwd) => `hashed-${pwd}`)
}))

vi.mock('../../stores/permission', () => ({
  usePermissionStore: vi.fn(() => ({
    loadPermissions: vi.fn().mockResolvedValue(undefined)
  }))
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

describe('Login.vue', () => {
  let wrapper

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  it('渲染登录表单', () => {
    wrapper = mount(Login, {
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
    expect(wrapper.find('.login-container').exists()).toBe(true)
  })

  it('表单初始状态为空', () => {
    wrapper = mount(Login, {
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
  })

  it('登录成功后调用 API 并跳转', async () => {
    const mockLoginResponse = {
      accessToken: 'test-access-token',
      refreshToken: 'test-refresh-token',
      userId: 1,
      username: 'testuser'
    }

    authApi.login.mockResolvedValueOnce(mockLoginResponse)

    wrapper = mount(Login, {
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
    wrapper.vm.form.username = 'testuser'
    wrapper.vm.form.password = 'password123'

    await wrapper.vm.handleLogin()

    // API 被调用，密码经过哈希
    expect(authApi.login).toHaveBeenCalledWith('testuser', 'hashed-password123')
  })
})