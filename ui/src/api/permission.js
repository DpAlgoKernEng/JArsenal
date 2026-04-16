import api from './index'

/**
 * 权限 API
 * 用于前端权限系统
 */
export const permissionApi = {
  /**
   * 获取用户完整权限信息
   * @returns {Promise<{menus: Array, actions: Array, fields: Array, version: number}>}
   */
  getPermissions: () => api.get('/auth/permissions'),

  /**
   * 获取用户菜单（树结构）
   * @returns {Promise<Array>}
   */
  getMenus: () => api.get('/auth/menus'),

  /**
   * 获取用户操作权限
   * @returns {Promise<Array<{resourceCode: string, actions: Array<string>}>}
   */
  getActions: () => api.get('/auth/actions'),

  /**
   * 获取用户字段权限
   * @returns {Promise<Array<{resourceCode: string, fieldCode: string, canView: boolean, canEdit: boolean}>}
   */
  getFields: () => api.get('/auth/fields'),

  /**
   * 获取权限版本号
   * 用于前端轮询判断是否需要刷新
   * @returns {Promise<number>}
   */
  getVersion: () => api.get('/auth/permissions/version')
}