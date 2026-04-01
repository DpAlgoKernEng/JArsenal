import { defineStore } from 'pinia'
import { ref } from 'vue'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userId = ref(localStorage.getItem('userId') || '')
  const username = ref(localStorage.getItem('username') || '')

  const setUser = (data) => {
    token.value = data.token
    userId.value = data.userId
    username.value = data.username
    localStorage.setItem('token', data.token)
    localStorage.setItem('userId', data.userId)
    localStorage.setItem('username', data.username)
  }

  const logout = () => {
    token.value = ''
    userId.value = ''
    username.value = ''
    localStorage.removeItem('token')
    localStorage.removeItem('userId')
    localStorage.removeItem('username')
  }

  const isLoggedIn = () => !!token.value

  return { token, userId, username, setUser, logout, isLoggedIn }
})