<template>
  <Navbar />
  <div class="user-edit-container">
    <el-card v-loading="loading">
      <template #header>
        <span>{{ isEdit ? '编辑用户' : '查看用户' }}</span>
      </template>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
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
        <el-form-item>
          <el-button type="primary" @click="handleSave" :loading="submitLoading">
            保存
          </el-button>
          <el-button @click="router.push('/users')">返回</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
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
    router.push('/users')
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

<style scoped>
.user-edit-container {
  padding: 20px;
  max-width: 600px;
}
</style>