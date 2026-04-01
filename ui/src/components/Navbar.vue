<template>
  <el-header class="navbar">
    <div class="logo">JArsenal</div>
    <div class="user-info" v-if="userStore.isLoggedIn()">
      <span>欢迎, {{ userStore.username }}</span>
      <el-button type="danger" link @click="handleLogout">退出登录</el-button>
    </div>
  </el-header>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { useUserStore } from '../stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleLogout = () => {
  userStore.logout()
  ElMessage.success('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.navbar {
  background-color: #409eff;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}
.logo {
  font-size: 20px;
  font-weight: bold;
}
.user-info {
  display: flex;
  align-items: center;
  gap: 16px;
}
</style>