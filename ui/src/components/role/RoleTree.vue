<template>
  <div class="role-tree-container">
    <!-- 搜索框 -->
    <el-input
      v-model="searchText"
      placeholder="搜索角色名称"
      clearable
      class="search-input"
    >
      <template #prefix>
        <el-icon><Search /></el-icon>
      </template>
    </el-input>

    <!-- 操作按钮 -->
    <div class="tree-actions">
      <el-button
        class="create-button"
        size="small"
        @click="handleCreateRoot"
        v-permission="{ resource: 'ROLE_MANAGE', action: 'CREATE' }"
      >
        <el-icon><Plus /></el-icon>
        新增根角色
      </el-button>
    </div>

    <!-- 角色树 -->
    <el-tree
      ref="treeRef"
      :data="filteredRoles"
      :props="treeProps"
      node-key="id"
      highlight-current
      default-expand-all
      :expand-on-click-node="false"
      class="role-tree"
      @current-change="handleSelect"
    >
      <template #default="{ node, data }">
        <div class="tree-node">
          <span class="node-label">{{ data.name }}</span>
          <span class="node-code">{{ data.code }}</span>
          <div class="node-actions">
            <el-button
              type="primary"
              size="small"
              link
              @click.stop="handleCreateChild(data)"
              v-permission="{ resource: 'ROLE_MANAGE', action: 'CREATE' }"
            >
              <el-icon><Plus /></el-icon>
            </el-button>
            <el-button
              type="danger"
              size="small"
              link
              @click.stop="handleDelete(data)"
              v-permission="{ resource: 'ROLE_MANAGE', action: 'DELETE' }"
            >
              <el-icon><Delete /></el-icon>
            </el-button>
          </div>
        </div>
      </template>
    </el-tree>
  </div>
</template>

<script setup>
import { ref, computed, watch, onMounted } from 'vue'
import { Search, Plus, Delete } from '@element-plus/icons-vue'
import { ElMessageBox, ElMessage } from 'element-plus'
import { useRoleStore } from '../../stores/role'

const emit = defineEmits(['select', 'create', 'delete'])

const roleStore = useRoleStore()

// 树组件引用
const treeRef = ref()

// 搜索文本
const searchText = ref('')

// 树节点属性配置
const treeProps = {
  children: 'children',
  label: 'name'
}

// 过滤后的角色列表
const filteredRoles = computed(() => {
  if (!searchText.value) {
    return roleStore.roles
  }

  const search = searchText.value.toLowerCase()
  return filterTree(roleStore.roles, search)
})

// 递归过滤树
const filterTree = (tree, search) => {
  const result = []
  for (const node of tree) {
    const nameMatch = node.name?.toLowerCase().includes(search)
    const codeMatch = node.code?.toLowerCase().includes(search)

    if (nameMatch || codeMatch) {
      result.push(node)
    } else if (node.children && node.children.length > 0) {
      const filteredChildren = filterTree(node.children, search)
      if (filteredChildren.length > 0) {
        result.push({ ...node, children: filteredChildren })
      }
    }
  }
  return result
}

// 选择角色
const handleSelect = (data) => {
  roleStore.selectRole(data)
  emit('select', data)
}

// 新增根角色
const handleCreateRoot = () => {
  emit('create', null) // parentId 为 null 表示根角色
}

// 新增子角色
const handleCreateChild = (parent) => {
  emit('create', parent.id)
}

// 删除角色
const handleDelete = async (role) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除角色 "${role.name}" 吗？删除后该角色下的用户将失去该角色的权限。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    emit('delete', role.id)
  } catch {
    // 用户取消
  }
}

// 加载角色树
onMounted(async () => {
  if (!roleStore.loaded) {
    await roleStore.loadRoles()
  }
})

// 暴露方法
defineExpose({
  refresh: roleStore.refreshRoles
})
</script>

<style lang="scss" scoped>
.role-tree-container {
  height: 100%;
  display: flex;
  flex-direction: column;
  gap: 12px;

  .search-input {
    --el-input-bg-color: var(--color-bg-glass);
    --el-input-border-color: var(--color-border);
    --el-input-text-color: var(--color-text-primary);
    --el-input-placeholder-color: var(--color-text-muted);
  }

  .tree-actions {
    display: flex;
    justify-content: flex-start;

    .create-button {
      @include gradient-button;
      padding: 6px 12px;
      font-size: 12px;

      .el-icon {
        margin-right: 4px;
      }
    }
  }

  .role-tree {
    flex: 1;
    overflow: auto;
    background: transparent;

    :deep(.el-tree-node__content) {
      height: 36px;
      background: transparent;

      &:hover {
        background: var(--color-bg-glass-hover);
      }
    }

    :deep(.el-tree-node.is-current > .el-tree-node__content) {
      background: var(--color-primary-light);
      color: var(--color-primary);
    }

    .tree-node {
      display: flex;
      align-items: center;
      flex: 1;
      font-size: 14px;

      .node-label {
        flex: 1;
        overflow: hidden;
        text-overflow: ellipsis;
        white-space: nowrap;
      }

      .node-code {
        margin-left: 8px;
        color: var(--color-text-muted);
        font-size: 12px;
        font-family: monospace;
      }

      .node-actions {
        margin-left: 8px;
        opacity: 0;
        transition: opacity 0.2s ease;
        display: flex;
        gap: 4px;
      }

      &:hover .node-actions {
        opacity: 1;
      }
    }
  }
}
</style>