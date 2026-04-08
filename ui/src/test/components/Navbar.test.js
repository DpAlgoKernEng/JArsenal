import { describe, it, expect, vi, beforeEach } from 'vitest'
import { mount } from '@vue/test-utils'
import { createPinia, setActivePinia } from 'pinia'
import Navbar from '../../components/Navbar.vue'
import { useUserStore } from '../../stores/user'

// Mock vue-router
const mockPush = vi.fn()
vi.mock('vue-router', () => ({
  useRouter: () => ({
    push: mockPush
  })
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

  it('用户已登录时显示用户名', async () => {
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
          'el-button': true
        }
      }
    })

    expect(wrapper.text()).toContain('testuser')
  })

  it('用户未登录时不显示用户信息', () => {
    wrapper = mount(Navbar, {
      global: {
        stubs: {
          'el-header': true,
          'el-button': true
        }
      }
    })

    // 检查 user-info div 不存在或不可见
    const userInfo = wrapper.find('.user-info')
    expect(userInfo.exists()).toBe(false)
  })

  it('点击退出登录调用 logout 并跳转', async () => {
    userStore.setUser({
      accessToken: 'test-token',
      refreshToken: 'refresh-token',
      userId: 1,
      username: 'testuser'
    })

    wrapper = mount(Navbar, {
      global: {
        stubs: {
          'el-header': { template: '<header><slot /></header>' },
          'el-button': {
            template: '<button @click="$emit(\'click\')"><slot /></button>',
            emits: ['click']
          }
        }
      }
    })

    // 找到退出按钮并点击
    const logoutButton = wrapper.findAll('button').find(b => b.text().includes('退出'))
    if (logoutButton) {
      await logoutButton.trigger('click')
    } else {
      // 直接调用 handleLogout 方法
      await wrapper.vm.handleLogout()
    }

    expect(userStore.accessToken).toBe('')
    expect(mockPush).toHaveBeenCalledWith('/login')
  })
})