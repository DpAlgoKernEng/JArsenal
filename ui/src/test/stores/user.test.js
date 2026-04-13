import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../../stores/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    vi.clearAllMocks()
  })

  describe('初始状态', () => {
    it('初始化时所有状态为空', () => {
      const store = useUserStore()

      expect(store.accessToken).toBe('')
      expect(store.userId).toBe('')
      expect(store.username).toBe('')
    })
  })

  describe('setUser', () => {
    it('设置用户信息', () => {
      const store = useUserStore()

      store.setUser({
        accessToken: 'access-token-123',
        userId: 1,
        username: 'testuser'
      })

      expect(store.accessToken).toBe('access-token-123')
      expect(store.userId).toBe('1')
      expect(store.username).toBe('testuser')
      expect(store.token).toBe('access-token-123')
    })
  })

  describe('setAccessToken', () => {
    it('只更新 accessToken', () => {
      const store = useUserStore()

      store.setAccessToken('new-access-token')

      expect(store.accessToken).toBe('new-access-token')
      expect(store.token).toBe('new-access-token')
    })
  })

  describe('logout', () => {
    it('清空所有用户信息', () => {
      const store = useUserStore()

      // 先设置用户信息
      store.setUser({
        accessToken: 'access-token',
        userId: 1,
        username: 'testuser'
      })

      // 登出
      store.logout()

      expect(store.accessToken).toBe('')
      expect(store.userId).toBe('')
      expect(store.username).toBe('')
      expect(store.token).toBe('')
    })
  })

  describe('isLoggedIn', () => {
    it('有 token 时返回 true', () => {
      const store = useUserStore()

      store.setAccessToken('valid-token')

      expect(store.isLoggedIn()).toBe(true)
    })

    it('无 token 时返回 false', () => {
      const store = useUserStore()

      expect(store.isLoggedIn()).toBe(false)
    })

    it('空字符串 token 返回 false', () => {
      const store = useUserStore()

      store.setAccessToken('')

      expect(store.isLoggedIn()).toBe(false)
    })
  })
})