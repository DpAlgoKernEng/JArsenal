<template>
  <Navbar />
  <div class="permission-list-container">
    <div class="content-card">
      <!-- Tab切换 -->
      <el-tabs v-model="activeTab" class="permission-tabs">
        <el-tab-pane label="角色权限配置" name="role">
          <RolePermissionPanel />
        </el-tab-pane>
        <el-tab-pane label="用户权限查看" name="user">
          <UserPermissionView />
        </el-tab-pane>
      </el-tabs>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import Navbar from '../components/Navbar.vue'
import RolePermissionPanel from '../components/permission/RolePermissionPanel.vue'
import UserPermissionView from '../components/permission/UserPermissionView.vue'

const activeTab = ref('role')
</script>

<style lang="scss" scoped>
.permission-list-container {
  padding-top: 60px;
  min-height: 100vh;
  background: var(--color-bg-base);

  .content-card {
    @include glass-card;
    margin: 24px;
    padding: 24px;
  }

  .permission-tabs {
    :deep(.el-tabs__item) {
      color: var(--color-text-secondary);
      font-size: 16px;

      &.is-active {
        color: var(--color-primary);
      }
    }

    :deep(.el-tabs__content) {
      padding-top: 16px;
    }
  }
}
</style>