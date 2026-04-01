import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const accessToken = ref(localStorage.getItem('accessToken') || '')
  const refreshToken = ref(localStorage.getItem('refreshToken') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')

  // 兼容旧的 token 字段名
  const token = ref(accessToken.value)

  const setUser = (data) => {
    accessToken.value = data.accessToken
    refreshToken.value = data.refreshToken
    userId.value = String(data.userId)
    username.value = data.username
    token.value = data.accessToken

    localStorage.setItem('accessToken', data.accessToken)
    localStorage.setItem('refreshToken', data.refreshToken)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
    // 兼容旧字段
    localStorage.setItem('token', data.accessToken)
  }

  const setTokens = (access, refresh) => {
    accessToken.value = access
    refreshToken.value = refresh
    token.value = access
    localStorage.setItem('accessToken', access)
    localStorage.setItem('refreshToken', refresh)
    localStorage.setItem('token', access)
  }

  const logout = () => {
    accessToken.value = ''
    refreshToken.value = ''
    userId.value = ''
    username.value = ''
    token.value = ''

    localStorage.removeItem('accessToken')
    localStorage.removeItem('refreshToken')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
    localStorage.removeItem('token')
  }

  const isLoggedIn = () => !!accessToken.value

  return {
    accessToken,
    refreshToken,
    userId,
    username,
    token, // 兼容
    setUser,
    setTokens,
    logout,
    isLoggedIn
  }
})