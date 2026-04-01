<template>
  <Navbar />
  <div class="user-list-container">
    <el-card>
      <!-- 搜索栏 -->
      <el-form inline class="search-form">
        <el-form-item label="用户名">
          <el-input v-model="searchForm.username" placeholder="搜索用户名" clearable />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="选择状态" clearable style="width: 120px">
            <el-option label="正常" :value="1" />
            <el-option label="禁用" :value="0" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="fetchUsers">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>

      <!-- 操作按钮 -->
      <el-button type="primary" @click="showCreateDialog" style="margin-bottom: 16px">
        新增用户
      </el-button>

      <!-- 用户列表 -->
      <el-table :data="users" v-loading="loading" stripe>
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="username" label="用户名" />
        <el-table-column prop="email" label="邮箱" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 1 ? 'success' : 'danger'">
              {{ row.status === 1 ? '正常' : '禁用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="180" />
        <el-table-column label="操作" width="180">
          <template #default="{ row }">
            <el-button type="primary" link @click="editUser(row.id)">编辑</el-button>
            <el-button type="danger" link @click="deleteUser(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <el-pagination
        v-model:current-page="pageNum"
        v-model:page-size="pageSize"
        :total="total"
        :page-sizes="[5, 10, 20, 50]"
        layout="total, sizes, prev, pager, next"
        @size-change="fetchUsers"
        @current-change="fetchUsers"
        style="margin-top: 16px; justify-content: flex-end"
      />
    </el-card>

    <!-- 创建/编辑弹窗 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑用户' : '新增用户'" width="400px">
      <el-form ref="dialogFormRef" :model="dialogForm" :rules="dialogRules" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="dialogForm.username" placeholder="请输入用户名" />
        </el-form-item>
        <el-form-item label="密码" prop="password" v-if="!isEdit">
          <el-input v-model="dialogForm.password" type="password" placeholder="请输入密码 (6-100字符)" show-password />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="dialogForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="状态" prop="status">
          <el-radio-group v-model="dialogForm.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { userApi } from '../api'
import Navbar from '../components/Navbar.vue'

const router = useRouter()
const loading = ref(false)
const users = ref([])
const total = ref(0)
const pageNum = ref(1)
const pageSize = ref(10)

const searchForm = reactive({
  username: '',
  status: null
})

const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const dialogFormRef = ref()
const submitLoading = ref(false)

const dialogForm = reactive({
  username: '',
  password: '',
  email: '',
  status: 1
})

const dialogRules = {
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

const fetchUsers = async () => {
  loading.value = true
  try {
    const data = await userApi.list({
      pageNum: pageNum.value,
      pageSize: pageSize.value,
      username: searchForm.username || undefined,
      status: searchForm.status ?? undefined
    })
    users.value = data.list
    total.value = data.total
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

const resetSearch = () => {
  searchForm.username = ''
  searchForm.status = null
  pageNum.value = 1
  fetchUsers()
}

const showCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  dialogForm.username = ''
  dialogForm.password = ''
  dialogForm.email = ''
  dialogForm.status = 1
  dialogVisible.value = true
}

const editUser = (id) => {
  router.push(`/users/${id}`)
}

const deleteUser = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.username}"？`, '提示', {
      type: 'warning'
    })
    await userApi.delete(row.id)
    ElMessage.success('删除成功')
    fetchUsers()
  } catch (error) {
    if (error !== 'cancel') {
      // error handled by interceptor
    }
  }
}

const submitForm = async () => {
  try {
    await dialogFormRef.value.validate()
    submitLoading.value = true
    if (isEdit.value) {
      await userApi.update(editId.value, {
        username: dialogForm.username,
        email: dialogForm.email,
        status: dialogForm.status
      })
      ElMessage.success('更新成功')
    } else {
      await userApi.create({
        username: dialogForm.username,
        password: dialogForm.password,
        email: dialogForm.email,
        status: dialogForm.status
      })
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    fetchUsers()
  } catch (error) {
    // error handled by interceptor
  } finally {
    submitLoading.value = false
  }
}

onMounted(() => {
  fetchUsers()
})
</script>

<style scoped>
.user-list-container {
  padding: 20px;
}
.search-form {
  margin-bottom: 16px;
}
</style>