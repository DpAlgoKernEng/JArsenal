import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'
import { useUserStore } from '../stores/user'

const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || '/api',
  timeout: 10000,
  withCredentials: true  // 确保发送 Cookie（用于 Refresh Token）
})

// 是否正在刷新 Token
let isRefreshing = false
// 等待刷新的请求队列
let requestQueue = []

// 请求拦截器：自动添加 Token
api.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.accessToken) {
    config.headers.Authorization = `Bearer ${userStore.accessToken}`
  }
  return config
})

// 响应拦截器：处理错误和自动刷新 Token
api.interceptors.response.use(
  response => {
    const { code, message, data } = response.data
    if (code === 200) {
      return data
    } else {
      ElMessage.error(message || '请求失败')
      return Promise.reject(new Error(message))
    }
  },
  async error => {
    const originalRequest = error.config

    // 401 错误且未重试过，尝试刷新 Token
    if (error.response?.status === 401 && !originalRequest._retry) {
      const userStore = useUserStore()

      // 尝试刷新 Token（Refresh Token 通过 Cookie 自动发送）
      if (userStore.accessToken) {
        if (isRefreshing) {
          // 正在刷新，将请求加入队列等待
          return new Promise((resolve, reject) => {
            requestQueue.push({ resolve, reject, config: originalRequest })
          })
        }

        originalRequest._retry = true
        isRefreshing = true

        try {
          // 调用刷新 Token 接口（Refresh Token 通过 Cookie 自动发送）
          const response = await axios.post(
            (import.meta.env.VITE_API_BASE_URL || '/api') + '/auth/refresh',
            {},
            { withCredentials: true }  // 确保发送 Cookie
          )

          const { accessToken } = response.data

          // 更新 Access Token（Refresh Token 已通过 HttpOnly Cookie 更新）
          userStore.setAccessToken(accessToken)

          // 重试队列中的请求
          requestQueue.forEach(({ resolve, reject, config }) => {
            config.headers.Authorization = `Bearer ${accessToken}`
            api(config).then(resolve).catch(reject)
          })
          requestQueue = []

          // 重试原始请求
          originalRequest.headers.Authorization = `Bearer ${accessToken}`
          return api(originalRequest)
        } catch (refreshError) {
          // 刷新失败，清空队列并登出
          requestQueue.forEach(({ reject }) => reject(refreshError))
          requestQueue = []
          userStore.logout()
          router.push('/login')
          ElMessage.error('登录已过期，请重新登录')
          return Promise.reject(refreshError)
        } finally {
          isRefreshing = false
        }
      } else {
        // 没有 Access Token，直接登出
        userStore.logout()
        router.push('/login')
        ElMessage.error('登录已过期，请重新登录')
      }
    } else if (error.response?.status === 429) {
      ElMessage.error('请求过于频繁，请稍后再试')
    } else if (error.response?.status === 401) {
      // 已重试过仍然 401
      const userStore = useUserStore()
      userStore.logout()
      router.push('/login')
      ElMessage.error('登录已过期，请重新登录')
    } else {
      ElMessage.error(error.response?.data?.message || '网络错误')
    }

    return Promise.reject(error)
  }
)

// 认证 API
export const authApi = {
  login: (username, password) => api.post('/auth/login', { username, password }),
  register: (username, password, email) => api.post('/auth/register', { username, password, email }),
  // Refresh Token 通过 HttpOnly Cookie 自动发送
  refresh: () => api.post('/auth/refresh', {}),
  // Refresh Token 通过 HttpOnly Cookie 自动发送
  logout: () => api.post('/auth/logout', {})
}

// 用户 API
export const userApi = {
  list: (params) => api.get('/users', { params }),
  get: (id) => api.get(`/users/${id}`),
  create: (data) => api.post('/users', data),
  update: (id, data) => api.put(`/users/${id}`, data),
  delete: (id) => api.delete(`/users/${id}`)
}

export default api