import { computed } from 'vue'
import { usePermissionStore } from '../stores/permission'

/**
 * 权限组合式函数
 * 提供便捷的权限检查方法
 */
export const usePermission = () => {
  const permissionStore = usePermissionStore()

  /**
   * 检查是否有操作权限
   * @param {string} resourceCode 资源编码
   * @param {string} action 操作类型
   * @returns {boolean}
   */
  const hasPermission = (resourceCode, action) => {
    return computed(() => permissionStore.hasAction(resourceCode, action))
  }

  /**
   * 检查是否有多个操作权限之一
   * @param {string} resourceCode 资源编码
   * @param {Array<string>} actions 操作类型数组
   * @returns {boolean}
   */
  const hasAnyPermission = (resourceCode, actions) => {
    return computed(() => {
      return actions.some(action => permissionStore.hasAction(resourceCode, action))
    })
  }

  /**
   * 检查是否有所有操作权限
   * @param {string} resourceCode 资源编码
   * @param {Array<string>} actions 操作类型数组
   * @returns {boolean}
   */
  const hasAllPermissions = (resourceCode, actions) => {
    return computed(() => {
      return actions.every(action => permissionStore.hasAction(resourceCode, action))
    })
  }

  /**
   * 检查是否可以查看字段
   * @param {string} resourceCode 资源编码
   * @param {string} fieldCode 字段编码
   * @returns {boolean}
   */
  const canViewField = (resourceCode, fieldCode) => {
    return computed(() => permissionStore.canViewField(resourceCode, fieldCode))
  }

  /**
   * 检查是否可以编辑字段
   * @param {string} resourceCode 资源编码
   * @param {string} fieldCode 字段编码
   * @returns {boolean}
   */
  const canEditField = (resourceCode, fieldCode) => {
    return computed(() => permissionStore.canEditField(resourceCode, fieldCode))
  }

  /**
   * 检查是否有菜单权限
   * @param {string} menuCode 菜单编码
   * @returns {boolean}
   */
  const hasMenu = (menuCode) => {
    return computed(() => permissionStore.hasMenu(menuCode))
  }

  /**
   * 获取用户菜单列表
   * @returns {Array}
   */
  const menus = computed(() => permissionStore.menus)

  /**
   * 权限是否已加载
   * @returns {boolean}
   */
  const isLoaded = computed(() => permissionStore.isLoaded)

  /**
   * 刷新权限
   */
  const refreshPermissions = async () => {
    await permissionStore.loadPermissions()
  }

  return {
    hasPermission,
    hasAnyPermission,
    hasAllPermissions,
    canViewField,
    canEditField,
    hasMenu,
    menus,
    isLoaded,
    refreshPermissions
  }
}