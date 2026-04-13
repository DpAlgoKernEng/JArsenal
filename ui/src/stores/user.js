import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  // Access Token 存储在内存中（不持久化到 localStorage 以避免 XSS 风险）
  const accessToken = ref('')
  const userId = ref('')
  const username = ref('')

  // 兼容旧的 token 字段名
  const token = ref(accessToken.value)

  const setUser = (data) => {
    accessToken.value = data.accessToken
    userId.value = String(data.userId)
    username.value = data.username
    token.value = data.accessToken

    // 不再存储 Refresh Token 到 localStorage
    // Refresh Token 已通过 HttpOnly Cookie 安全存储
  }

  const setAccessToken = (token) => {
    accessToken.value = token
  }

  const logout = () => {
    accessToken.value = ''
    userId.value = ''
    username.value = ''
    token.value = ''
  }

  const isLoggedIn = () => !!accessToken.value

  return {
    accessToken,
    userId,
    username,
    token, // 兼容
    setUser,
    setAccessToken,
    logout,
    isLoggedIn
  }
})