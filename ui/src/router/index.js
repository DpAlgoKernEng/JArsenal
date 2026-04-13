import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

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
      title: '用户管理'
    }
  },
  {
    path: '/users/:id',
    name: 'UserEdit',
    component: () => import('../views/UserEdit.vue'),
    meta: {
      title: '编辑用户'
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

// 认证守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()

  // 设置页面标题
  document.title = to.meta.title ? `${to.meta.title} - JArsenal` : 'JArsenal'

  // 认证检查
  if (!to.meta.public && !userStore.isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

export default router