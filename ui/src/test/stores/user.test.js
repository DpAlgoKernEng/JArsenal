import { describe, it, expect, beforeEach, vi } from 'vitest'
import { setActivePinia, createPinia } from 'pinia'
import { useUserStore } from '../stores/user'

describe('User Store', () => {
  beforeEach(() => {
    setActivePinia(createPinia())
    localStorage.clear()
    vi.clearAllMocks()
  })

  describe('初始状态', () => {
    it('初始化时所有状态为空', () => {
      const store = useUserStore()

      expect(store.accessToken).toBe('')
      expect(store.refreshToken).toBe('')
      expect(store.userId).toBe('')
      expect(store.username).toBe('')
    })
  })

  describe('setUser', () => {
    it('设置用户信息并保存到 localStorage', () => {
      const store = useUserStore()

      store.setUser({
        accessToken: 'access-token-123',
        refreshToken: 'refresh-token-456',
        userId: 1,
        username: 'testuser'
      })

      expect(store.accessToken).toBe('access-token-123')
      expect(store.refreshToken).toBe('refresh-token-456')
      expect(store.userId).toBe('1')
      expect(store.username).toBe('testuser')
      expect(store.token).toBe('access-token-123')

      expect(localStorage.setItem).toHaveBeenCalledWith('accessToken', 'access-token-123')
      expect(localStorage.setItem).toHaveBeenCalledWith('refreshToken', 'refresh-token-456')
      expect(localStorage.setItem).toHaveBeenCalledWith('userId', 1)
      expect(localStorage.setItem).toHaveBeenCalledWith('username', 'testuser')
    })
  })

  describe('setTokens', () => {
    it('只更新 tokens', () => {
      const store = useUserStore()

      store.setTokens('new-access-token', 'new-refresh-token')

      expect(store.accessToken).toBe('new-access-token')
      expect(store.refreshToken).toBe('new-refresh-token')
      expect(store.token).toBe('new-access-token')

      expect(localStorage.setItem).toHaveBeenCalledWith('accessToken', 'new-access-token')
      expect(localStorage.setItem).toHaveBeenCalledWith('refreshToken', 'new-refresh-token')
    })
  })

  describe('logout', () => {
    it('清空所有用户信息', () => {
      const store = useUserStore()

      // 先设置用户信息
      store.setUser({
        accessToken: 'access-token',
        refreshToken: 'refresh-token',
        userId: 1,
        username: 'testuser'
      })

      // 登出
      store.logout()

      expect(store.accessToken).toBe('')
      expect(store.refreshToken).toBe('')
      expect(store.userId).toBe('')
      expect(store.username).toBe('')
      expect(store.token).toBe('')

      expect(localStorage.removeItem).toHaveBeenCalledWith('accessToken')
      expect(localStorage.removeItem).toHaveBeenCalledWith('refreshToken')
      expect(localStorage.removeItem).toHaveBeenCalledWith('userId')
      expect(localStorage.removeItem).toHaveBeenCalledWith('username')
      expect(localStorage.removeItem).toHaveBeenCalledWith('token')
    })
  })

  describe('isLoggedIn', () => {
    it('有 token 时返回 true', () => {
      const store = useUserStore()

      store.setTokens('valid-token', 'refresh-token')

      expect(store.isLoggedIn()).toBe(true)
    })

    it('无 token 时返回 false', () => {
      const store = useUserStore()

      expect(store.isLoggedIn()).toBe(false)
    })

    it('空字符串 token 返回 false', () => {
      const store = useUserStore()

      store.setTokens('', '')

      expect(store.isLoggedIn()).toBe(false)
    })
  })
})