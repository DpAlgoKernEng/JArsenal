<template>
  <el-table :data="users" v-loading="loading" class="user-table">
    <el-table-column prop="id" label="ID" width="80" align="center" />
    <el-table-column prop="username" label="用户名" min-width="120" />
    <el-table-column prop="email" label="邮箱" min-width="180" />
    <el-table-column prop="status" label="状态" width="100" align="center">
      <template #default="{ row }">
        <el-tag
          :type="row.status === 1 ? 'success' : 'danger'"
          effect="plain"
          class="status-tag"
        >
          {{ row.status === 1 ? '正常' : '禁用' }}
        </el-tag>
      </template>
    </el-table-column>
    <el-table-column prop="createTime" label="创建时间" width="180" />
    <el-table-column label="操作" width="160" align="center">
      <template #default="{ row }">
        <el-button class="edit-button" link @click="handleEdit(row.id)">
          <el-icon><Edit /></el-icon>
          编辑
        </el-button>
        <el-button class="delete-button" link @click="handleDelete(row)">
          <el-icon><Delete /></el-icon>
          删除
        </el-button>
      </template>
    </el-table-column>
  </el-table>
</template>

<script setup>
import { ElMessage, ElMessageBox } from 'element-plus'
import { Edit, Delete } from '@element-plus/icons-vue'
import { userApi } from '../api'

const props = defineProps({
  users: {
    type: Array,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

const emit = defineEmits(['edit', 'refresh'])

const handleEdit = (id) => {
  emit('edit', id)
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '提示', {
      type: 'warning',
      confirmButtonText: '确定',
      cancelButtonText: '取消'
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    emit('refresh')
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}
</script>

<style lang="scss" scoped>
.user-table {
  border-radius: 8px;

  :deep(.el-table__header-wrapper) {
    th {
      background: var(--color-bg-elevated);
      color: var(--color-text-primary);
      font-weight: 600;
    }
  }

  :deep(.el-table__row) {
    transition: all 0.2s ease;

    &:hover > td {
      background: var(--color-bg-glass) !important;
    }
  }

  :deep(td) {
    border-bottom: 1px solid var(--color-border-light);
  }

  .status-tag {
    border-radius: 4px;
    font-size: 12px;
  }

  .edit-button {
    color: var(--color-primary);
    transition: all 0.2s ease;

    &:hover {
      color: var(--color-accent);
    }

    .el-icon {
      margin-right: 4px;
    }
  }

  .delete-button {
    color: var(--el-color-danger);
    transition: all 0.2s ease;

    &:hover {
      color: #ef4444;
    }

    .el-icon {
      margin-right: 4px;
    }
  }
}
</style>
