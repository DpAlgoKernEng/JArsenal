import { describe, it, expect, vi, beforeEach } from 'vitest'

// Mock axios
vi.mock('axios', () => {
  const mockAxios = vi.fn()
  mockAxios.create = vi.fn(() => mockAxios)
  mockAxios.interceptors = {
    request: { use: vi.fn() },
    response: { use: vi.fn() }
  }
  mockAxios.get = vi.fn()
  mockAxios.post = vi.fn()
  mockAxios.put = vi.fn()
  mockAxios.delete = vi.fn()
  return { default: mockAxios }
})

// Mock element-plus
vi.mock('element-plus', () => ({
  ElMessage: {
    error: vi.fn(),
    success: vi.fn()
  }
}))

// Mock vue-router - 需要导出 createRouter
vi.mock('vue-router', () => ({
  createRouter: vi.fn(() => ({
    beforeEach: vi.fn(),
    push: vi.fn()
  })),
  createWebHistory: vi.fn()
}))

// Mock pinia store
vi.mock('../stores/user', () => ({
  useUserStore: vi.fn(() => ({
    accessToken: '',
    isLoggedIn: vi.fn(() => false),
    setAccessToken: vi.fn(),
    logout: vi.fn()
  }))
}))

describe('API Module', () => {
  let axios

  beforeEach(async () => {
    vi.clearAllMocks()
    axios = (await import('axios')).default
    vi.resetModules()
  })

  describe('authApi', () => {
    it('login 应该发送 POST 请求到 /auth/login', async () => {
      const mockResponse = {
        data: {
          code: 200,
          data: {
            accessToken: 'test-token'
          }
        }
      }
      axios.post.mockResolvedValueOnce(mockResponse)

      const { authApi } = await import('../../api/index.js')

      await authApi.login('testuser', 'password123')

      expect(axios.post).toHaveBeenCalledWith('/auth/login', {
        username: 'testuser',
        password: 'password123'
      })
    })

    it('register 应该发送 POST 请求到 /auth/register', async () => {
      axios.post.mockResolvedValueOnce({ data: { code: 200, data: null } })

      const { authApi } = await import('../../api/index.js')

      await authApi.register('newuser', 'password123', 'test@example.com')

      expect(axios.post).toHaveBeenCalledWith('/auth/register', {
        username: 'newuser',
        password: 'password123',
        email: 'test@example.com'
      })
    })

    it('refresh 应该发送 POST 请求到 /auth/refresh', async () => {
      axios.post.mockResolvedValueOnce({
        data: {
          code: 200,
          data: {
            accessToken: 'new-access'
          }
        }
      })

      const { authApi } = await import('../../api/index.js')

      await authApi.refresh()

      expect(axios.post).toHaveBeenCalledWith('/auth/refresh', {})
    })

    it('logout 应该发送 POST 请求到 /auth/logout', async () => {
      axios.post.mockResolvedValueOnce({ data: { code: 200, data: null } })

      const { authApi } = await import('../../api/index.js')

      await authApi.logout()

      expect(axios.post).toHaveBeenCalledWith('/auth/logout', {})
    })
  })

  describe('userApi', () => {
    it('list 应该发送 GET 请求到 /users', async () => {
      axios.get.mockResolvedValueOnce({
        data: {
          code: 200,
          data: { list: [], total: 0 }
        }
      })

      const { userApi } = await import('../../api/index.js')

      await userApi.list({ pageNum: 1, pageSize: 10 })

      expect(axios.get).toHaveBeenCalledWith('/users', {
        params: { pageNum: 1, pageSize: 10 }
      })
    })

    it('get 应该发送 GET 请求到 /users/:id', async () => {
      axios.get.mockResolvedValueOnce({
        data: { code: 200, data: { id: 1, username: 'test' } }
      })

      const { userApi } = await import('../../api/index.js')

      await userApi.get(1)

      expect(axios.get).toHaveBeenCalledWith('/users/1')
    })

    it('create 应该发送 POST 请求到 /users', async () => {
      axios.post.mockResolvedValueOnce({
        data: { code: 200, data: { id: 1 } }
      })

      const { userApi } = await import('../../api/index.js')

      await userApi.create({ username: 'newuser', password: 'pass', email: 'test@example.com' })

      expect(axios.post).toHaveBeenCalledWith('/users', {
        username: 'newuser',
        password: 'pass',
        email: 'test@example.com'
      })
    })

    it('update 应该发送 PUT 请求到 /users/:id', async () => {
      axios.put.mockResolvedValueOnce({ data: { code: 200, data: null } })

      const { userApi } = await import('../../api/index.js')

      await userApi.update(1, { username: 'updated' })

      expect(axios.put).toHaveBeenCalledWith('/users/1', { username: 'updated' })
    })

    it('delete 应该发送 DELETE 请求到 /users/:id', async () => {
      axios.delete.mockResolvedValueOnce({ data: { code: 200, data: null } })

      const { userApi } = await import('../../api/index.js')

      await userApi.delete(1)

      expect(axios.delete).toHaveBeenCalledWith('/users/1')
    })
  })
})