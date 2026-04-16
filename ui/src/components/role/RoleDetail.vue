<template>
  <div class="role-detail-container">
    <!-- 未选中状态 -->
    <div v-if="!role" class="empty-state">
      <el-icon class="empty-icon"><Document /></el-icon>
      <p class="empty-text">请在左侧选择一个角色查看详情</p>
    </div>

    <!-- 详情面板 -->
    <div v-else class="detail-panel">
      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button
          class="edit-button"
          size="small"
          @click="handleEdit"
          v-permission="{ resource: 'ROLE_MANAGE', action: 'UPDATE' }"
        >
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button
          class="delete-button"
          size="small"
          type="danger"
          @click="handleDelete"
          v-permission="{ resource: 'ROLE_MANAGE', action: 'DELETE' }"
        >
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
        <el-button
          class="permission-button"
          size="small"
          @click="handleConfigPermission"
          v-permission="{ resource: 'PERMISSION', action: 'UPDATE' }"
        >
          <el-icon><Setting /></el-icon>
          配置权限
        </el-button>
      </div>

      <!-- 基本信息 -->
      <el-descriptions :column="1" border class="info-descriptions">
        <el-descriptions-item label="角色编码">
          <span class="code-text">{{ role.code }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="角色名称">
          {{ role.name }}
        </el-descriptions-item>
        <el-descriptions-item label="父角色">
          {{ parentName || '无（根角色）' }}
        </el-descriptions-item>
        <el-descriptions-item label="继承模式">
          <el-tag :type="role.inheritMode === 0 ? 'success' : 'warning'" size="small">
            {{ role.inheritMode === 0 ? '合并继承' : '覆盖继承' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="排序">
          {{ role.sort }}
        </el-descriptions-item>
        <el-descriptions-item label="创建时间">
          {{ formatTime(role.createTime) }}
        </el-descriptions-item>
        <el-descriptions-item label="更新时间">
          {{ formatTime(role.updateTime) }}
        </el-descriptions-item>
      </el-descriptions>

      <!-- 统计信息 -->
      <div class="stats-section">
        <h4 class="stats-title">统计信息</h4>
        <div class="stats-grid">
          <div class="stat-item">
            <el-icon class="stat-icon"><User /></el-icon>
            <div class="stat-content">
              <span class="stat-value">{{ role.userCount || 0 }}</span>
              <span class="stat-label">关联用户</span>
            </div>
          </div>
          <div class="stat-item">
            <el-icon class="stat-icon"><Grid /></el-icon>
            <div class="stat-content">
              <span class="stat-value">{{ childCount }}</span>
              <span class="stat-label">子角色数</span>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { Document, Edit, Delete, Setting, User, Grid } from '@element-plus/icons-vue'
import { ElMessageBox } from 'element-plus'
import { useRoleStore } from '../../stores/role'

const emit = defineEmits(['edit', 'delete', 'configPermission'])

const roleStore = useRoleStore()

// 当前选中角色
const role = computed(() => roleStore.currentRole)

// 父角色名称
const parentName = computed(() => {
  if (!role.value?.parentId) return null
  const parent = roleStore.findRoleById(role.value.parentId)
  return parent?.name || null
})

// 子角色数量
const childCount = computed(() => {
  return role.value?.children?.length || 0
})

// 格式化时间
const formatTime = (time) => {
  if (!time) return '-'
  const date = new Date(time)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 编辑角色
const handleEdit = () => {
  emit('edit', role.value)
}

// 删除角色
const handleDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${role.value.name}" 吗？删除后该角色下的用户将失去该角色的权限。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('delete', role.value.id)
  } catch {
    // 用户取消
  }
}

// 配置权限
const handleConfigPermission = () => {
  emit('configPermission', role.value)
}
</script>

<style lang="scss" scoped>
.role-detail-container {
  height: 100%;
  @include glass-card;
  padding: 24px;

  .empty-state {
    height: 100%;
    display: flex;
    flex-direction: column;
    align-items: center;
    justify-content: center;
    gap: 16px;

    .empty-icon {
      font-size: 48px;
      color: var(--color-text-muted);
    }

    .empty-text {
      color: var(--color-text-muted);
      font-size: 14px;
    }
  }

  .detail-panel {
    height: 100%;
    display: flex;
    flex-direction: column;
    gap: 20px;

    .action-bar {
      display: flex;
      gap: 8px;
      flex-wrap: wrap;

      .edit-button {
        @include gradient-button;
        padding: 6px 12px;
        font-size: 12px;

        .el-icon {
          margin-right: 4px;
        }
      }

      .delete-button {
        padding: 6px 12px;
        font-size: 12px;

        .el-icon {
          margin-right: 4px;
        }
      }

      .permission-button {
        background: var(--color-bg-glass);
        border: 1px solid var(--color-primary);
        color: var(--color-primary);
        padding: 6px 12px;
        font-size: 12px;
        transition: all 0.3s ease;

        .el-icon {
          margin-right: 4px;
        }

        &:hover {
          background: var(--color-primary-light);
        }
      }
    }

    .info-descriptions {
      :deep(.el-descriptions__label) {
        width: 100px;
        background: var(--color-bg-glass);
        color: var(--color-text-secondary);
      }

      :deep(.el-descriptions__content) {
        background: var(--color-bg-base);
        color: var(--color-text-primary);
      }

      :deep(.el-descriptions__cell) {
        border-color: var(--color-border);
      }

      .code-text {
        font-family: monospace;
        color: var(--color-primary);
      }
    }

    .stats-section {
      .stats-title {
        color: var(--color-text-primary);
        font-size: 14px;
        font-weight: 600;
        margin-bottom: 12px;
        @include gradient-text;
      }

      .stats-grid {
        display: grid;
        grid-template-columns: repeat(2, 1fr);
        gap: 16px;

        .stat-item {
          @include glass-card;
          padding: 16px;
          display: flex;
          align-items: center;
          gap: 12px;

          .stat-icon {
            font-size: 24px;
            color: var(--color-primary);
          }

          .stat-content {
            display: flex;
            flex-direction: column;
            gap: 4px;

            .stat-value {
              font-size: 20px;
              font-weight: 600;
              color: var(--color-text-primary);
            }

            .stat-label {
              font-size: 12px;
              color: var(--color-text-muted);
            }
          }
        }
      }
    }
  }
}
</style>