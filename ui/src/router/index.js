import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import Layout from '../components/Layout.vue'

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
  // Layout 路由（包含所有业务页面）
  {
    path: '/',
    component: Layout,
    redirect: '/system/users',
    children: [
      {
        path: 'system/users',
        name: 'UserList',
        component: () => import('../views/UserList.vue'),
        meta: {
          title: '用户管理',
          resource: 'USER_MANAGE'
        }
      },
      {
        path: 'system/users/:id',
        name: 'UserEdit',
        component: () => import('../views/UserEdit.vue'),
        meta: {
          title: '编辑用户',
          resource: 'USER_MANAGE'
        }
      },
      {
        path: 'system/roles',
        name: 'RoleList',
        component: () => import('../views/RoleList.vue'),
        meta: {
          title: '角色管理',
          resource: 'ROLE_MANAGE'
        }
      },
      {
        path: 'system/resources',
        name: 'ResourceList',
        component: () => import('../views/ResourceList.vue'),
        meta: {
          title: '资源管理',
          resource: 'RESOURCE_MANAGE'
        }
      },
      {
        path: 'system/permissions',
        name: 'PermissionList',
        component: () => import('../views/Placeholder.vue'),
        meta: {
          title: '权限管理',
          resource: 'PERMISSION'
        }
      }
    ]
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

  // 加载权限（如果未加载）
  if (!permissionStore.loaded) {
    try {
      await permissionStore.loadPermissions()
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
}

export default router