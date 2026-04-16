import { usePermissionStore } from '../stores/permission'
import Layout from '../components/Layout.vue'

/**
 * 动态路由模块
 * 根据用户菜单权限动态生成路由
 */

// 存储动态添加的路由名称，用于清理
const dynamicRouteNames = new Set()

// 预定义的组件映射（只包含实际存在的组件）
const componentMap = {
  'views/UserList': () => import('../views/UserList.vue'),
  'views/UserEdit': () => import('../views/UserEdit.vue'),
  'views/Dashboard': () => import('../views/Placeholder.vue')
}

/**
 * 根据菜单构建路由配置
 * @param {Object} menu 菜单数据
 * @returns {Object} 路由配置
 */
const buildRoute = (menu) => {
  const route = {
    path: menu.path,
    name: menu.code,
    meta: {
      title: menu.name,
      resource: menu.code,
      icon: menu.icon
    }
  }

  // 记录路由名称
  dynamicRouteNames.add(menu.code)

  // 设置组件
  if (menu.component) {
    // 动态加载组件
    if (componentMap[menu.component]) {
      route.component = componentMap[menu.component]
    } else {
      // 尝试动态导入（使用 import.meta.glob）
      try {
        route.component = () => import(`../${menu.component}.vue`)
      } catch (e) {
        console.warn(`组件 ${menu.component} 不存在`)
        route.component = undefined
      }
    }
  }

  // 处理子菜单
  if (menu.children && menu.children.length > 0) {
    route.children = menu.children.map(child => buildRoute(child))
  }

  return route
}

/**
 * 添加动态路由
 * @param {Array} menus 菜单列表
 * @param {Object} router Vue Router 实例
 */
export const addDynamicRoutes = (menus, router) => {
  if (!menus || menus.length === 0) return

  // 构建父路由（使用 Layout 作为容器）
  const layoutRoute = {
    path: '/',
    component: Layout,
    redirect: menus[0]?.path || '/home',
    children: []
  }

  // 为每个菜单添加路由
  menus.forEach(menu => {
    const route = buildRoute(menu)

    // 如果菜单有路径但没有 component，设置一个占位组件
    if (route.path && !route.component) {
      route.component = () => import('../views/Placeholder.vue')
    }

    // 添加到 layout 的 children
    layoutRoute.children.push(route)

    // 处理子菜单的子路由
    if (menu.children && menu.children.length > 0) {
      menu.children.forEach(child => {
        const childRoute = buildRoute(child)
        if (childRoute.path && !childRoute.component) {
          childRoute.component = () => import('../views/Placeholder.vue')
        }
        layoutRoute.children.push(childRoute)
      })
    }
  })

  // 添加到路由
  router.addRoute(layoutRoute)

  return layoutRoute
}

/**
 * 移除动态路由
 * @param {Object} router Vue Router 实例
 */
export const removeDynamicRoutes = (router) => {
  // 按名称移除动态添加的路由
  dynamicRouteNames.forEach(name => {
    try {
      router.removeRoute(name)
    } catch (e) {
      // 路由可能不存在，忽略错误
    }
  })
  dynamicRouteNames.clear()

  // 移除 Layout 路由（如果存在）
  try {
    router.removeRoute('/')
  } catch (e) {
    // 路由可能不存在，忽略错误
  }
}

/**
 * 清理动态路由状态
 */
export const cleanupDynamicRoutes = () => {
  dynamicRouteNames.clear()
}

/**
 * 初始化动态路由
 * @param {Object} router Vue Router 实例
 * @returns {Promise<boolean>}
 */
export const initDynamicRoutes = async (router) => {
  const permissionStore = usePermissionStore()

  // 如果权限未加载，先加载
  if (!permissionStore.isLoaded) {
    try {
      await permissionStore.loadPermissions()
    } catch (error) {
      console.error('加载权限失败:', error)
      return false
    }
  }

  // 添加动态路由
  addDynamicRoutes(permissionStore.menus, router)

  return true
}