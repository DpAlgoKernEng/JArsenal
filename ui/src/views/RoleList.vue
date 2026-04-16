<template>
  <div class="role-list-container">
    <!-- 左侧角色树 -->
    <div class="left-panel">
      <RoleTree
        ref="roleTreeRef"
        @select="handleSelect"
        @create="handleCreate"
        @delete="handleDelete"
      />
    </div>

    <!-- 右侧详情面板 -->
    <div class="right-panel">
      <RoleDetail
        @edit="handleEdit"
        @delete="handleDelete"
        @configPermission="handleConfigPermission"
      />
    </div>

    <!-- 编辑弹窗 -->
    <RoleForm
      v-model="formVisible"
      :parent-id="formParentId"
      :role-data="editRoleData"
      :is-edit="isEdit"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useRoleStore } from '../stores/role'
import { roleApi } from '../api/role'
import RoleTree from '../components/role/RoleTree.vue'
import RoleDetail from '../components/role/RoleDetail.vue'
import RoleForm from '../components/role/RoleForm.vue'

const roleStore = useRoleStore()
const router = useRouter()

// 角色树引用
const roleTreeRef = ref()

// 弹窗状态
const formVisible = ref(false)
const isEdit = ref(false)
const formParentId = ref(null)
const editRoleData = ref(null)

// 选择角色
const handleSelect = (role) => {
  // RoleDetail 通过 roleStore.currentRole 自动更新
}

// 创建角色
const handleCreate = (parentId) => {
  isEdit.value = false
  formParentId.value = parentId
  editRoleData.value = null
  formVisible.value = true
}

// 编辑角色
const handleEdit = (role) => {
  isEdit.value = true
  formParentId.value = null
  editRoleData.value = role
  formVisible.value = true
}

// 删除角色
const handleDelete = async (roleId) => {
  try {
    await roleApi.delete(roleId)
    ElMessage.success('删除成功')

    // 刷新角色树
    await roleStore.refreshRoles()

    // 如果删除的是当前选中的角色，清空选中
    if (roleStore.currentRole?.id === roleId) {
      roleStore.selectRole(null)
    }
  } catch (error) {
    // error handled by interceptor
  }
}

// 配置权限
const handleConfigPermission = (role) => {
  // 导航到权限管理页面，自动选中角色
  router.push(`/system/permissions?roleId=${role.id}`)
}

// 表单提交成功
const handleFormSuccess = async () => {
  // 刷新角色树
  await roleStore.refreshRoles()
}

onMounted(async () => {
  // 加载角色数据
  if (!roleStore.loaded) {
    await roleStore.loadRoles()
  }
})
</script>

<style lang="scss" scoped>
.role-list-container {
  height: calc(100vh - 60px - 48px); // 减去 header 和 main-content padding
  display: flex;
  gap: 24px;
  padding: 0;

  .left-panel {
    width: 300px;
    flex-shrink: 0;
    @include glass-card;
    padding: 16px;
    overflow: hidden;
  }

  .right-panel {
    flex: 1;
    min-width: 0;
  }
}
</style>