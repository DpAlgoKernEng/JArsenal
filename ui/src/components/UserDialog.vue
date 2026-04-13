<template>
  <el-dialog v-model="visible" :title="title" width="400px" @closed="handleClosed">
    <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
      <el-form-item label="用户名" prop="username">
        <el-input v-model="form.username" placeholder="请输入用户名" />
      </el-form-item>
      <el-form-item label="密码" prop="password" v-if="!isEdit">
        <el-input
          v-model="form.password"
          type="password"
          placeholder="请输入密码 (6-100字符)"
          show-password
        />
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
    </el-form>
    <template #footer>
      <el-button @click="visible = false">取消</el-button>
      <el-button type="primary" @click="handleSubmit" :loading="submitLoading">确定</el-button>
    </template>
  </el-dialog>
</template>

<script setup>
import { ref, reactive, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { userApi } from '../api'

const props = defineProps({
  modelValue: {
    type: Boolean,
    default: false
  },
  isEdit: {
    type: Boolean,
    default: false
  },
  userId: {
    type: Number,
    default: null
  },
  userData: {
    type: Object,
    default: null
  }
})

const emit = defineEmits(['update:modelValue', 'success'])

// 双向绑定 visible
const visible = computed({
  get: () => props.modelValue,
  set: (val) => emit('update:modelValue', val)
})

const title = computed(() => props.isEdit ? '编辑用户' : '新增用户')

const formRef = ref()
const submitLoading = ref(false)

const form = reactive({
  username: '',
  password: '',
  email: '',
  status: 1
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 20, message: '用户名长度2-20字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 100, message: '密码长度6-100字符', trigger: 'blur' }
  ],
  email: [
    { required: true, message: '请输入邮箱', trigger: 'blur' },
    { type: 'email', message: '邮箱格式不正确', trigger: 'blur' }
  ],
  status: [{ required: true, message: '请选择状态', trigger: 'change' }]
}

// 监听 userData 变化，填充表单（编辑模式）
watch(() => props.userData, (data) => {
  if (data && props.isEdit) {
    form.username = data.username || ''
    form.email = data.email || ''
    form.status = data.status ?? 1
  }
}, { immediate: true })

// 对话框关闭时重置表单
const handleClosed = () => {
  formRef.value?.resetFields()
  form.username = ''
  form.password = ''
  form.email = ''
  form.status = 1
}

const handleSubmit = async () => {
  try {
    await formRef.value.validate()
    submitLoading.value = true

    if (props.isEdit) {
      await userApi.update(props.userId, {
        username: form.username,
        email: form.email,
        status: form.status
      })
      ElMessage.success('更新成功')
    } else {
      await userApi.create({
        username: form.username,
        password: form.password,
        email: form.email,
        status: form.status
      })
      ElMessage.success('创建成功')
    }

    visible.value = false
    emit('success')
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

// 暴露方法
defineExpose({
  reset: handleClosed
})
</script>