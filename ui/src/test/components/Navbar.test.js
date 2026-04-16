import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Navbar from '../../components/Navbar.vue'
import { useUserStore } from '../../stores/user'

// Mock window.matchMedia for theme composable
Object.defineProperty(window, 'matchMedia', {
  writable: true,
  value: vi.fn().mockImplementation(query => ({
    matches: false,
    media: query,
    onchange: null,
    addListener: vi.fn(),
    removeListener: vi.fn(),
    addEventListener: vi.fn(),
    removeEventListener: vi.fn(),
    dispatchEvent: vi.fn(),
  })),
})

// Mock vue-router
const mockRouterPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockRouterPush
  }),
  createRouter: vi.fn(() => ({ push: vi.fn(), beforeEach: vi.fn() })),
  createWebHistory: vi.fn()
}))

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    success: vi.fn(),
    error: vi.fn()
  }
}))

describe('Navbar.vue', () => {
  let wrapper
  let userStore

  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
    userStore = useUserStore()
  })

  it('用户未登录时不显示用户信息', () => {
    wrapper = mount(Navbar, {
      global: {
        stubs: {
          'el-header': true,
          'el-button': true,
          'el-icon': true,
          'el-avatar': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true
        }
      }
    })

    // 用户未登录时，user-info 不存在
    const userInfo = wrapper.find('.user-info')
    expect(userInfo.exists()).toBe(false)
  })

  it('用户已登录时，store 中有用户数据', async () => {
    userStore.setUser({
      accessToken: 'test-token',
      refreshToken: 'refresh-token',
      userId: 1,
      username: 'testuser'
    })

    wrapper = mount(Navbar, {
      global: {
        stubs: {
          'el-header': true,
          'el-button': true,
          'el-icon': true,
          'el-avatar': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true
        }
      }
    })

    // 验证 store 中有用户名
    expect(userStore.username).toBe('testuser')
    expect(userStore.isLoggedIn()).toBe(true)
  })

  it('退出登录清空用户数据', async () => {
    userStore.setUser({
      accessToken: 'test-token',
      refreshToken: 'refresh-token',
      userId: 1,
      username: 'testuser'
    })

    wrapper = mount(Navbar, {
      global: {
        stubs: {
          'el-header': true,
          'el-button': true,
          'el-icon': true,
          'el-avatar': true,
          'el-dropdown': true,
          'el-dropdown-menu': true,
          'el-dropdown-item': true
        }
      }
    })

    // 直接调用 handleLogout 方法
    await wrapper.vm.handleLogout()

    expect(userStore.accessToken).toBe('')
    expect(userStore.username).toBe('')
    expect(mockRouterPush).toHaveBeenCalledWith('/login')
  })
})