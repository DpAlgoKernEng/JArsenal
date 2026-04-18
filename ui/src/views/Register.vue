<template>
  <div class="register-container">
    <div class="register-card">
      <div class="register-header">
        <h1 class="register-title">JGuard</h1>
        <p class="register-subtitle">用户注册</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" class="register-form">
        <el-form-item prop="username">
          <el-input
            v-model="form.username"
            placeholder="请输入用户名 (2-50字符)"
            size="large"
            :prefix-icon="User"
          />
        </el-form-item>
        <el-form-item prop="password">
          <el-input
            v-model="form.password"
            type="password"
            placeholder="请输入密码 (6-100字符)"
            size="large"
            show-password
            :prefix-icon="Lock"
          />
        </el-form-item>
        <el-form-item prop="email">
          <el-input
            v-model="form.email"
            placeholder="请输入邮箱"
            size="large"
            :prefix-icon="Message"
          />
        </el-form-item>
        <el-form-item>
          <el-button
            class="register-button"
            size="large"
            @click="handleRegister"
            :loading="loading"
          >
            注册
          </el-button>
        </el-form-item>
      </el-form>
      <div class="register-footer">
        <el-button text @click="router.push('/login')">
          已有账号？点击登录
        </el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Message } from '@element-plus/icons-vue'
import { authApi } from '../api'
import { hashPassword } from '../utils/passwordEncrypt'

const router = useRouter()
const formRef = ref()
const loading = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 50, message: '用户名长度2-50字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  try {
    await formRef.value.validate()
    loading.value = true
    // 密码哈希后再传输
    const hashedPassword = hashPassword(form.password)
    await authApi.register(form.username, hashedPassword, form.email)
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style lang="scss" scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);
  padding: 20px;

  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.register-card {
  width: 480px;
  @include glass-card;
  padding: 40px;

  .register-header {
    text-align: center;
    margin-bottom: 32px;

    .register-title {
      font-size: 32px;
      font-weight: 700;
      @include gradient-text;
      margin-bottom: 8px;
    }

    .register-subtitle {
      color: var(--color-text-secondary);
      font-size: 14px;
    }
  }

  .register-form {
    .el-form-item {
      margin-bottom: 24px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .register-button {
      width: 100%;
      @include gradient-button;
      height: 44px;
      font-size: 16px;
    }
  }

  .register-footer {
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