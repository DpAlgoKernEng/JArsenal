import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { permissionApi } from '../api/permission'

/**
 * 权限 Pinia Store
 * 管理用户权限数据、菜单、操作权限、字段权限
 */
export const usePermissionStore = defineStore('permission', () => {
  // 菜单列表（树结构）
  const menus = ref([])

  // 操作权限 Map<resourceCode, Set<action>>
  const actionMap = ref(new Map())

  // 字段权限 Map<resourceCode, Map<fieldCode, {canView, canEdit}>>
  const fieldMap = ref(new Map())

  // 权限版本号（用于轮询检查）
  const version = ref(0)

  // 是否已加载权限
  const loaded = ref(false)

  // 轮询定时器
  let pollingTimer = null

  /**
   * 加载用户权限数据
   */
  const loadPermissions = async () => {
    try {
      const data = await permissionApi.getPermissions()

      menus.value = data.menus || []
      version.value = data.version || 0

      // 构建操作权限 Map
      const newActionMap = new Map()
      if (data.actions) {
        data.actions.forEach(item => {
          newActionMap.set(item.resourceCode, new Set(item.actions))
        })
      }
      actionMap.value = newActionMap

      // 构建字段权限 Map
      const newFieldMap = new Map()
      if (data.fields) {
        data.fields.forEach(item => {
          let resourceFields = newFieldMap.get(item.resourceCode)
          if (!resourceFields) {
            resourceFields = new Map()
            newFieldMap.set(item.resourceCode, resourceFields)
          }
          resourceFields.set(item.fieldCode, {
            canView: item.canView,
            canEdit: item.canEdit
          })
        })
      }
      fieldMap.value = newFieldMap

      loaded.value = true

      // 启动版本轮询
      startPermissionPolling()

      return data
    } catch (error) {
      console.error('加载权限失败:', error)
      throw error
    }
  }

  /**
   * 检查版本并刷新权限（如果版本变更）
   */
  const checkVersionAndRefresh = async () => {
    try {
      const newVersion = await permissionApi.getVersion()
      if (newVersion !== version.value) {
        console.log('权限版本变更，重新加载权限')
        await loadPermissions()
      }
    } catch (error) {
      console.error('检查权限版本失败:', error)
    }
  }

  /**
   * 启动权限版本轮询（30秒间隔）
   */
  const startPermissionPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
    }
    pollingTimer = setInterval(checkVersionAndRefresh, 30000)
  }

  /**
   * 停止权限版本轮询
   */
  const stopPermissionPolling = () => {
    if (pollingTimer) {
      clearInterval(pollingTimer)
      pollingTimer = null
    }
  }

  /**
   * 清空权限数据
   */
  const clearPermissions = () => {
    menus.value = []
    actionMap.value = new Map()
    fieldMap.value = new Map()
    version.value = 0
    loaded.value = false
    stopPermissionPolling()
  }

  /**
   * 检查是否有指定资源的操作权限
   * @param {string} resourceCode 资源编码
   * @param {string} action 操作类型 (VIEW, CREATE, UPDATE, DELETE, EXECUTE)
   * @returns {boolean}
   */
  const hasAction = (resourceCode, action) => {
    const actions = actionMap.value.get(resourceCode)
    return actions ? actions.has(action) : false
  }

  /**
   * 检查是否可以查看指定字段
   * @param {string} resourceCode 资源编码
   * @param {string} fieldCode 字段编码
   * @returns {boolean}
   */
  const canViewField = (resourceCode, fieldCode) => {
    const resourceFields = fieldMap.value.get(resourceCode)
    if (!resourceFields) return true // 没有字段权限配置则默认可查看
    const fieldPerm = resourceFields.get(fieldCode)
    return fieldPerm ? fieldPerm.canView : true
  }

  /**
   * 检查是否可以编辑指定字段
   * @param {string} resourceCode 资源编码
   * @param {string} fieldCode 字段编码
   * @returns {boolean}
   */
  const canEditField = (resourceCode, fieldCode) => {
    const resourceFields = fieldMap.value.get(resourceCode)
    if (!resourceFields) return true // 没有字段权限配置则默认可编辑
    const fieldPerm = resourceFields.get(fieldCode)
    return fieldPerm ? fieldPerm.canEdit : true
  }

  /**
   * 检查是否有菜单访问权限
   * @param {string} menuCode 菜单编码
   * @returns {boolean}
   */
  const hasMenu = (menuCode) => {
    return findMenuByCode(menus.value, menuCode) !== null
  }

  /**
   * 递归查找菜单
   */
  const findMenuByCode = (menuList, code) => {
    for (const menu of menuList) {
      if (menu.code === code) return menu
      if (menu.children && menu.children.length > 0) {
        const found = findMenuByCode(menu.children, code)
        if (found) return found
      }
    }
    return null
  }

  // 计算属性：是否已加载权限
  const isLoaded = computed(() => loaded.value)

  return {
    menus,
    actionMap,
    fieldMap,
    version,
    loaded,
    isLoaded,
    loadPermissions,
    checkVersionAndRefresh,
    startPermissionPolling,
    stopPermissionPolling,
    clearPermissions,
    hasAction,
    canViewField,
    canEditField,
    hasMenu
  }
})