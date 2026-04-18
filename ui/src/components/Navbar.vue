<template>
  <el-header class="navbar">
    <div class="navbar-content">
      <div class="logo">
        <span class="logo-text">JGuard</span>
      </div>
      <div class="navbar-actions">
        <el-button class="theme-toggle" circle @click="toggleTheme">
          <el-icon :size="18">
            <component :is="theme === 'light' ? 'Moon' : 'Sunny'" />
          </el-icon>
        </el-button>
        <div class="user-info" v-if="userStore.isLoggedIn()">
          <el-dropdown trigger="click">
            <span class="user-dropdown-trigger">
              <el-avatar :size="32" class="user-avatar">
                {{ userStore.username?.charAt(0) || 'U' }}
              </el-avatar>
              <span class="user-name">{{ userStore.username }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleLogout">
                  <el-icon><SwitchButton /></el-icon>
                  退出登录
                </el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </div>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Moon, Sunny, SwitchButton } from '@element-plus/icons-vue'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import { useTheme } from '../composables/useTheme'
import { cleanupPermissionOnLogout } from '../router'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const { theme, toggleTheme } = useTheme()

const handleLogout = async () => {
  try {
    await userStore.logout()
    // 清理权限数据和动态路由
    cleanupPermissionOnLogout()
    ElMessage.success('已退出登录')
    router.push('/login')
  } catch (error) {
    // error handled by interceptor
  }
}
</script>

<style lang="scss" scoped>
.navbar {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  height: 64px;
  z-index: 1000;
  @include glass-card;
  border-radius: 0 0 16px 16px;
  padding: 0;

  .navbar-content {
    display: flex;
    justify-content: space-between;
    align-items: center;
    height: 100%;
    padding: 0 24px;
  }

  .logo {
    .logo-text {
      font-size: 22px;
      font-weight: 700;
      @include gradient-text;
    }
  }

  .navbar-actions {
    display: flex;
    align-items: center;
    gap: 16px;
  }

  .theme-toggle {
    background: var(--color-bg-glass);
    border: 1px solid var(--color-border);
    color: var(--color-text-primary);
    transition: all 0.3s ease;

    &:hover {
      background: var(--gradient-primary);
      border: none;
      color: white;
      box-shadow: var(--shadow-glow);
    }
  }

  .user-info {
    .user-dropdown-trigger {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      padding: 4px 12px;
      border-radius: 20px;
      transition: all 0.2s ease;

      &:hover {
        background: var(--color-bg-glass);
      }
    }

    .user-avatar {
      background: var(--gradient-primary);
      color: white;
      font-weight: 600;
    }

    .user-name {
      color: var(--color-text-primary);
      font-size: 14px;
    }
  }
}
</style>