<template>
  <el-container class="layout-container">
    <!-- 侧边栏 -->
    <el-aside width="200px" class="sidebar">
      <div class="logo">
        <h2>JArsenal</h2>
      </div>
      <el-menu
        :default-active="activeMenu"
        router
        class="sidebar-menu"
      >
        <template v-for="menu in menus" :key="menu.code">
          <!-- 有子菜单 -->
          <el-sub-menu v-if="menu.children && menu.children.length > 0" :index="menu.code">
            <template #title>
              <el-icon v-if="menu.icon">
                <component :is="menu.icon" />
              </el-icon>
              <span>{{ menu.name }}</span>
            </template>
            <el-menu-item
              v-for="child in menu.children"
              :key="child.code"
              :index="child.path"
            >
              <el-icon v-if="child.icon">
                <component :is="child.icon" />
              </el-icon>
              <span>{{ child.name }}</span>
            </el-menu-item>
          </el-sub-menu>
          <!-- 无子菜单 -->
          <el-menu-item v-else :index="menu.path">
            <el-icon v-if="menu.icon">
              <component :is="menu.icon" />
            </el-icon>
            <span>{{ menu.name }}</span>
          </el-menu-item>
        </template>
      </el-menu>
    </el-aside>

    <!-- 主内容区 -->
    <el-container>
      <!-- 顶部导航 -->
      <el-header class="header">
        <Navbar />
      </el-header>

      <!-- 内容区 -->
      <el-main class="main-content">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, watch } from 'vue'
import { useRoute } from 'vue-router'
import { usePermissionStore } from '../stores/permission'
import Navbar from '../components/Navbar.vue'

const route = useRoute()
const permissionStore = usePermissionStore()

// 当前激活的菜单
const activeMenu = computed(() => {
  return route.path
})

// 用户菜单列表
const menus = computed(() => {
  return permissionStore.menus || []
})
</script>

<style lang="scss" scoped>
.layout-container {
  min-height: 100vh;
}

.sidebar {
  background: var(--color-bg-glass);
  border-right: 1px solid var(--color-border);
  position: fixed;
  left: 0;
  top: 0;
  bottom: 0;
  z-index: 100;

  .logo {
    height: 60px;
    display: flex;
    align-items: center;
    justify-content: center;
    border-bottom: 1px solid var(--color-border);

    h2 {
      font-size: 20px;
      font-weight: 600;
      @include gradient-text;
    }
  }

  .sidebar-menu {
    border-right: none;
    background: transparent;

    .el-menu-item,
    .el-sub-menu__title {
      color: var(--color-text-primary);

      &:hover {
        background: var(--color-bg-glass-hover);
      }

      &.is-active {
        background: var(--color-primary-light);
        color: var(--color-primary);
      }
    }
  }
}

.header {
  position: fixed;
  right: 0;
  left: 200px;
  top: 0;
  z-index: 99;
  background: var(--color-bg-glass);
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 24px;
  height: 60px;
}

.main-content {
  margin-left: 200px;
  margin-top: 60px;
  padding: 24px;
  background: var(--color-bg-base);
  min-height: calc(100vh - 60px);
}
</style>