import { defineStore } from 'pinia'
import { ref } from 'vue'
import { roleApi } from '../api/role'

/**
 * 角色 Pinia Store
 * 管理角色树数据、当前选中角色、加载状态
 * 供 RoleTree、RoleDetail、RolePermissionPanel 等组件共享
 */
export const useRoleStore = defineStore('role', () => {
  // 角色树数据
  const roles = ref([])

  // 加载状态
  const loading = ref(false)

  // 当前选中角色
  const currentRole = ref(null)

  // 是否已加载
  const loaded = ref(false)

  /**
   * 加载角色树
   */
  const loadRoles = async () => {
    loading.value = true
    try {
      roles.value = await roleApi.tree()
      loaded.value = true
    } finally {
      loading.value = false
    }
  }

  /**
   * 选择角色
   * @param {Object} role 角色对象
   */
  const selectRole = (role) => {
    currentRole.value = role
  }

  /**
   * 刷新角色树
   */
  const refreshRoles = async () => {
    await loadRoles()
  }

  /**
   * 清空角色数据
   */
  const clearRoles = () => {
    roles.value = []
    currentRole.value = null
    loaded.value = false
  }

  /**
   * 根据 ID 查找角色（递归）
   * @param {number} id 角色 ID
   * @returns {Object|null}
   */
  const findRoleById = (id) => {
    return findRoleInTree(roles.value, id)
  }

  /**
   * 递归查找角色
   */
  const findRoleInTree = (tree, id) => {
    for (const role of tree) {
      if (role.id === id) return role
      if (role.children && role.children.length > 0) {
        const found = findRoleInTree(role.children, id)
        if (found) return found
      }
    }
    return null
  }

  /**
   * 构建树形选择器选项
   * 用于父角色选择下拉框
   * @returns {Array}
   */
  const buildTreeOptions = () => {
    return buildOptionsFromTree(roles.value)
  }

  /**
   * 递归构建选项
   */
  const buildOptionsFromTree = (tree, options = []) => {
    for (const role of tree) {
      options.push({
        value: role.id,
        label: role.name,
        children: role.children && role.children.length > 0
          ? buildOptionsFromTree(role.children)
          : undefined
      })
    }
    return options
  }

  return {
    roles,
    loading,
    currentRole,
    loaded,
    loadRoles,
    selectRole,
    refreshRoles,
    clearRoles,
    findRoleById,
    buildTreeOptions
  }
})
