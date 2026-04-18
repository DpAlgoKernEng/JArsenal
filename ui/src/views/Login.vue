<template>
  <div class="login-container">
    <div class="login-card">
      <div class="login-header">
        <h1 class="login-title">JGuard</h1>
        <p class="login-subtitle">用户登录</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="login-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            class="login-button"
            size="large"
            @click="handleLogin"
            :loading="loading"
          >
            登录
          </el-button>
        </el-form-item>
      </el-form>
      <div class="login-footer">
        <el-button text @click="router.push('/register')">
          没有账号？点击注册
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { useUserStore } from '../stores/user'
import { usePermissionStore } from '../stores/permission'
import { hashPassword } from '../utils/passwordEncrypt'

const router = useRouter()
const userStore = useUserStore()
const permissionStore = usePermissionStore()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }]
}

const handleLogin = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    // 密码哈希后再传输
    const hashedPassword = hashPassword(form.password)
    const data = await authApi.login(form.username, hashedPassword)
    userStore.setUser(data)

    // 加载用户权限
    try {
      await permissionStore.loadPermissions()
    } catch (permError) {
      console.warn('加载权限失败，将使用默认权限:', permError)
    }

    ElMessage.success('登录成功')
    router.push('/system/users')
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);
  padding: 20px;

  // 深色模式下的额外渐变层
  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.login-card {
  width: 420px;
  @include glass-card;
  padding: 40px;

  .login-header {
    text-align: center;
    margin-bottom: 32px;

    .login-title {
      font-size: 32px;
      font-weight: 700;
      @include gradient-text;
      margin-bottom: 8px;
    }

    .login-subtitle {
      color: var(--color-text-secondary);
      font-size: 14px;
    }
  }

  .login-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .login-button {
      width: 100%;
      @include gradient-button;
      height: 44px;
      font-size: 16px;
    }
  }

  .login-footer {
    text-align: center;
    margin-top: 16px;

    .el-button {
      color: var(--color-text-secondary);

      &:hover {
        color: var(--color-primary);
      }
    }
  }
}
</style>