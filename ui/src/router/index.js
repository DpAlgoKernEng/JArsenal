import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import { initDynamicRoutes, removeDynamicRoutes, cleanupDynamicRoutes } from './dynamicRoutes'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: {
      public: true,
      title: '登录'
    }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: {
      public: true,
      title: '注册'
    }
  },
  {
    path: '/403',
    name: 'Forbidden',
    component: () => import('../views/403.vue'),
    meta: {
      public: true,
      title: '无权限'
    }
  },
  {
    path: '/',
    redirect: '/users',
    meta: {
      title: '首页'
    }
  },
  {
    path: '/users',
    name: 'UserList',
    component: () => import('../views/UserList.vue'),
    meta: {
      title: '用户管理',
      resource: 'USER'
    }
  },
  {
    path: '/users/:id',
    name: 'UserEdit',
    component: () => import('../views/UserEdit.vue'),
    meta: {
      title: '编辑用户',
      resource: 'USER'
    }
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('../views/NotFound.vue'),
    meta: {
      public: true,
      title: '页面不存在'
    }
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 标记是否已初始化动态路由
let dynamicRoutesInitialized = false

// 认证守卫
router.beforeEach(async (to, from, next) => {
  const userStore = useUserStore()
  const permissionStore = usePermissionStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - JArsenal` : 'JArsenal'

  // 公开页面直接访问
  if (to.meta.public) {
    next()
    return
  }

  // 认证检查
  if (!userStore.isLoggedIn()) {
    next('/login')
    return
  }

  // 首次登录后加载权限并初始化动态路由
  if (!dynamicRoutesInitialized) {
    try {
      await permissionStore.loadPermissions()
      await initDynamicRoutes(router)
      dynamicRoutesInitialized = true

      // 重新导航到目标路由（确保动态路由已添加）
      next({ ...to, replace: true })
      return
    } catch (error) {
      console.error('加载权限失败:', error)
      next('/login')
      return
    }
  }

  // 检查路由权限
  if (to.meta.resource) {
    if (!permissionStore.hasMenu(to.meta.resource)) {
      next('/403')
      return
    }
  }

  next()
})

// 登出时清理权限
export const cleanupPermissionOnLogout = () => {
  const permissionStore = usePermissionStore()
  permissionStore.clearPermissions()
  removeDynamicRoutes(router)
  cleanupDynamicRoutes()
  dynamicRoutesInitialized = false
}

export default router