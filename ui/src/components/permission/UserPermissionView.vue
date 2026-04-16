<template>
  <div class="user-permission-view">
    <!-- 用户搜索 -->
    <div class="search-section">
      <el-input
        v-model="searchKeyword"
        placeholder="输入用户名搜索"
        clearable
        @keyup.enter="handleSearch"
        class="search-input"
      >
        <template #prefix>
          <el-icon><Search /></el-icon>
        </template>
      </el-input>
      <el-button class="search-button" @click="handleSearch">
        搜索
      </el-button>
    </div>

    <!-- 搜索结果 -->
    <div class="search-result" v-if="searchResults.length > 0">
      <el-table :data="searchResults" highlight-current-row @row-click="handleSelectUser">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 选中用户详情 -->
    <div class="user-detail" v-if="selectedUser">
      <div class="info-card">
        <h3 class="card-title">用户信息</h3>
        <el-descriptions :column="2" border>
          <el-descriptions-item label="ID">{{ selectedUser.id }}</el-descriptions-item>
          <el-descriptions-item label="用户名">{{ selectedUser.username }}</el-descriptions-item>
          <el-descriptions-item label="邮箱">{{ selectedUser.email }}</el-descriptions-item>
          <el-descriptions-item label="状态">
            <el-tag :type="selectedUser.status === 1 ? 'success' : 'danger'" effect="plain" size="small">
              {{ selectedUser.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>

      <!-- 关联角色 -->
      <div class="roles-card">
        <div class="card-header">
          <h3 class="card-title">关联角色</h3>
          <el-button
            v-if="canAssignRole"
            size="small"
            @click="showAddRoleDialog"
          >
            添加角色
          </el-button>
        </div>

        <el-table :data="userRoles" v-loading="rolesLoading">
          <el-table-column prop="name" label="角色名称" />
          <el-table-column prop="code" label="编码" />
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="row.status === 'ENABLED' ? 'success' : 'danger'" effect="plain" size="small">
                {{ row.status === 'ENABLED' ? '启用' : '禁用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button
                v-if="canAssignRole"
                link
                size="small"
                class="remove-btn"
                @click="handleRemoveRole(row)"
              >
                移除
              </el-button>
            </template>
          </el-table-column>
        </el-table>
      </div>

      <!-- 操作权限详情 -->
      <div class="permissions-card">
        <div class="card-header">
          <h3 class="card-title">操作权限</h3>
          <el-button size="small" @click="loadUserPermissions" :loading="permissionsLoading">
            查看详情
          </el-button>
        </div>

        <el-table :data="userActions" v-if="userActions.length > 0">
          <el-table-column prop="resourceCode" label="资源" min-width="150" />
          <el-table-column prop="actions" label="操作权限" min-width="200">
            <template #default="{ row }">
              <el-tag v-for="action in row.actions" :key="action" effect="plain" size="small" class="action-tag">
                {{ action }}
              </el-tag>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-else-if="!permissionsLoading" description="点击"查看详情"加载权限" :image-size="60" />
      </div>
    </div>

    <!-- 添加角色弹窗 -->
    <el-dialog v-model="addRoleVisible" title="添加角色" width="400px">
      <el-tree
        ref="roleTreeRef"
        :data="availableRoles"
        :props="{ children: 'children', label: 'name' }"
        node-key="id"
        show-checkbox
        check-strictly
      />
      <template #footer>
        <el-button @click="addRoleVisible = false">取消</el-button>
        <el-button type="primary" @click="handleAddRoles" :loading="addLoading">
          确定
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search } from '@element-plus/icons-vue'
import { usePermission } from '../../composables/usePermission'
import { useRoleStore } from '../../stores/role'
import { userApi, roleApi } from '../../api'

const { hasPermission } = usePermission()
const canAssignRole = computed(() => hasPermission('ROLE_MANAGE', 'UPDATE').value)

const roleStore = useRoleStore()

const searchKeyword = ref('')
const searchResults = ref([])
const selectedUser = ref(null)
const userRoles = ref([])
const rolesLoading = ref(false)

// 权限详情
const userActions = ref([])
const permissionsLoading = ref(false)

const addRoleVisible = ref(false)
const roleTreeRef = ref()
const addLoading = ref(false)

// 可选角色
const availableRoles = computed(() => roleStore.roles)

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) return
  try {
    const data = await userApi.list({ username: searchKeyword.value, pageNum: 1, pageSize: 10 })
    searchResults.value = data.list || []
  } catch (error) {
    // error handled
  }
}

// 选择用户
const handleSelectUser = async (row) => {
  selectedUser.value = row
  userActions.value = [] // 清空权限详情
  await loadUserRoles()
}

// 加载用户角色
const loadUserRoles = async () => {
  if (!selectedUser.value) return
  rolesLoading.value = true
  try {
    userRoles.value = await roleApi.getUserRoles(selectedUser.value.id)
  } finally {
    rolesLoading.value = false
  }
}

// 加载用户权限详情
const loadUserPermissions = async () => {
  if (!selectedUser.value) return
  permissionsLoading.value = true
  try {
    const data = await userApi.getPermissions(selectedUser.value.id)
    userActions.value = data.actions || []
  } catch (error) {
    // error handled
  } finally {
    permissionsLoading.value = false
  }
}

// 移除角色
const handleRemoveRole = async (role) => {
  try {
    await ElMessageBox.confirm(
      `确定移除角色 "${role.name}"？`,
      '提示',
      { type: 'warning' }
    )
    await roleApi.removeFromUser(selectedUser.value.id, role.id)
    ElMessage.success('移除成功')
    await loadUserRoles()
  } catch (error) {
    if (error !== 'cancel') {
      // error handled
    }
  }
}

// 显示添加角色弹窗
const showAddRoleDialog = () => {
  addRoleVisible.value = true
}

// 添加角色
const handleAddRoles = async () => {
  const checkedIds = roleTreeRef.value?.getCheckedKeys() || []
  if (checkedIds.length === 0) {
    ElMessage.warning('请选择角色')
    return
  }

  // 合并现有角色
  const currentIds = userRoles.value.map(r => r.id)
  const newIds = [...currentIds, ...checkedIds]

  addLoading.value = true
  try {
    await roleApi.assignToUser(selectedUser.value.id, newIds)
    ElMessage.success('添加成功')
    addRoleVisible.value = false
    await loadUserRoles()
  } catch (error) {
    // error handled
  } finally {
    addLoading.value = false
  }
}

onMounted(() => {
  if (!roleStore.loaded) {
    roleStore.loadRoles()
  }
})
</script>

<style lang="scss" scoped>
.user-permission-view {
  padding: 24px;

  .search-section {
    display: flex;
    gap: 12px;
    margin-bottom: 24px;

    .search-input {
      max-width: 300px;
    }

    .search-button {
      @include gradient-button;
    }
  }

  .search-result {
    margin-bottom: 24px;

    :deep(.el-table__row) {
      cursor: pointer;
    }
  }

  .user-detail {
    .info-card, .roles-card, .permissions-card {
      @include glass-card;
      padding: 20px;
      margin-bottom: 16px;
    }

    .card-header {
      display: flex;
      justify-content: space-between;
      align-items: center;
      margin-bottom: 16px;

      .card-title {
        @include gradient-text;
        font-size: 18px;
        font-weight: 600;
      }
    }

    .remove-btn {
      color: var(--el-color-danger);
    }

    .action-tag {
      margin-right: 4px;
      margin-bottom: 4px;
    }
  }
}
</style>