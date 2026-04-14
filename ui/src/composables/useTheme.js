import { ref, onMounted } from 'vue'

/**
 * 主题切换 composable
 * 支持深色/浅色模式切换，自动检测系统偏好，持久化到 localStorage
 */
export function useTheme() {
  const theme = ref('light')

  /**
   * 应用主题到 DOM
   */
  const applyTheme = (newTheme) => {
    document.documentElement.classList.toggle('dark', newTheme === 'dark')
    localStorage.setItem('theme-preference', newTheme)
    theme.value = newTheme
  }

  /**
   * 切换主题
   */
  const toggleTheme = () => {
    applyTheme(theme.value === 'light' ? 'dark' : 'light')
  }

  /**
   * 初始化：检测系统偏好 + localStorage
   */
  const initTheme = () => {
    const saved = localStorage.getItem('theme-preference')
    if (saved) {
      applyTheme(saved)
    } else {
      const prefersDark = window.matchMedia('(prefers-color-scheme: dark)').matches
      applyTheme(prefersDark ? 'dark' : 'light')
    }
  }

  // 监听系统偏好变化
  const watchSystemPreference = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    mediaQuery.addEventListener('change', (e) => {
      // 只有用户没有手动设置偏好时才跟随系统
      if (!localStorage.getItem('theme-preference')) {
        applyTheme(e.matches ? 'dark' : 'light')
      }
    })
  }

  onMounted(() => {
    initTheme()
    watchSystemPreference()
  })

  return {
    theme,
    applyTheme,
    toggleTheme,
    isDark: () => theme.value === 'dark'
  }
}
