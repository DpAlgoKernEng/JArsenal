<template>
  <Navbar />
  <div class="user-edit-container">
    <div class="edit-card">
      <div class="edit-header">
        <el-button class="back-button" @click="router.push('/system/users')">
          <el-icon><ArrowLeft /></el-icon>
          返回列表
        </el-button>
        <h2 class="edit-title">{{ isEdit ? '编辑用户' : '查看用户' }}</h2>
      </div>

      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px" v-loading="loading">
        <el-form-item label="ID">
          <el-input v-model="form.id" disabled />
        </el-form-item>
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="form.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="创建时间">
          <el-input v-model="form.createTime" disabled />
        </el-form-item>
        <el-form-item label="更新时间">
          <el-input v-model="form.updateTime" disabled />
        </el-form-item>
        <el-form-item class="action-buttons">
          <el-button class="save-button" @click="handleSave" :loading="submitLoading">
            <el-icon><Check /></el-icon>
            保存
          </el-button>
          <el-button @click="router.push('/system/users')">
            取消
          </el-button>
        </el-form-item>
      </el-form>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { ArrowLeft, Check } from '@element-plus/icons-vue'
import { userApi } from '../api'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const route = useRoute()
const loading = ref(false)
const submitLoading = ref(false)
const formRef = ref()
const isEdit = ref(true)

const form = reactive({
  id: '',
  username: '',
  email: '',
  status: 1,
  createTime: '',
  updateTime: ''
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

const fetchUser = async () => {
  loading.value = true
  try {
    const data = await userApi.get(route.params.id)
    form.id = data.id
    form.username = data.username
    form.email = data.email
    form.status = data.status
    form.createTime = data.createTime
    form.updateTime = data.updateTime
  } catch (error) {
    router.push('/system/users')
  } finally {
    loading.value = false
  }
}

const handleSave = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true
    await userApi.update(form.id, {
      username: form.username,
      email: form.email,
      status: form.status
    })
    ElMessage.success('保存成功')
    fetchUser()
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchUser()
})
</script>

<style lang="scss" scoped>
.user-edit-container {
  padding: 84px 24px 24px 24px;
  min-height: 100vh;
  background: var(--color-bg-base);
  display: flex;
  justify-content: center;
}

.edit-card {
  width: 100%;
  max-width: 600px;
  @include glass-card;
  padding: 24px;

  .edit-header {
    display: flex;
    align-items: center;
    gap: 16px;
    margin-bottom: 24px;

    .back-button {
      background: var(--color-bg-glass);
      border: 1px solid var(--color-border);
      color: var(--color-text-primary);
      transition: all 0.3s ease;

      &:hover {
        background: var(--color-border);
      }

      .el-icon {
        margin-right: 4px;
      }
    }

    .edit-title {
      @include gradient-text;
      font-size: 20px;
      font-weight: 600;
    }
  }

  .el-form {
    .el-form-item {
      margin-bottom: 20px;
    }

    .el-input {
      --el-input-bg-color: var(--color-bg-glass);
      --el-input-border-color: var(--color-border);
      --el-input-text-color: var(--color-text-primary);
      --el-input-placeholder-color: var(--color-text-muted);
    }

    .action-buttons {
      margin-top: 32px;

      .save-button {
        @include gradient-button;
        padding: 10px 24px;

        .el-icon {
          margin-right: 4px;
        }
      }
    }
  }
}
</style>