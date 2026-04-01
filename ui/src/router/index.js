import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue'),
    meta: { public: true }
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue'),
    meta: { public: true }
  },
  {
    path: '/',
    redirect: '/users'
  },
  {
    path: '/users',
    name: 'UserList',
    component: () => import('../views/UserList.vue')
  },
  {
    path: '/users/:id',
    name: 'UserEdit',
    component: () => import('../views/UserEdit.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 认证守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  if (!to.meta.public && !userStore.isLoggedIn()) {
    next('/login')
  } else {
    next()
  }
})

export default router