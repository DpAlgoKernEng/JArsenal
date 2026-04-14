<template>
  <Navbar />
  <div class="user-list-container">
    <div class="content-card">
      <!-- 搜索表单 -->
      <UserSearchForm ref="searchFormRef" @search="handleSearch" @reset="handleReset" />

      <!-- 操作按钮 -->
      <div class="action-bar">
        <el-button class="create-button" @click="showCreateDialog">
          <el-icon><Plus /></el-icon>
          新增用户
        </el-button>
      </div>

      <!-- 用户列表 -->
      <UserTable
        :users="users"
        :loading="loading"
        @edit="handleEdit"
        @refresh="fetchUsers"
      />

      <!-- 分页 -->
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pageNum"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[5, 10, 20, 50]"
          layout="total, sizes, prev, pager, next"
          @size-change="fetchUsers"
          @current-change="fetchUsers"
        />
      </div>
    </div>

    <!-- 创建/编辑弹窗 -->
    <UserDialog
      v-model="dialogVisible"
      :is-edit="isEdit"
      :user-id="editId"
      :user-data="editUserData"
      @success="fetchUsers"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { Plus } from '@element-plus/icons-vue'
import { usePagination } from '../composables/usePagination'
import { userApi } from '../api'
import Navbar from '../components/Navbar.vue'
import UserSearchForm from '../components/UserSearchForm.vue'
import UserTable from '../components/UserTable.vue'
import UserDialog from '../components/UserDialog.vue'

const router = useRouter()
const searchFormRef = ref()

// 使用分页 composable
const { pageNum, pageSize, total, resetPagination, getPaginationParams } = usePagination(10)

// 用户列表数据
const loading = ref(false)
const users = ref([])
const searchParams = ref({})

// 弹窗状态
const dialogVisible = ref(false)
const isEdit = ref(false)
const editId = ref(null)
const editUserData = ref(null)

// 获取用户列表
const fetchUsers = async () => {
  loading.value = true
  try {
    const data = await userApi.list({
      ...getPaginationParams(),
      ...searchParams.value
    })
    users.value = data.list
    total.value = data.total
  } catch (error) {
    // error handled by interceptor
  } finally {
    loading.value = false
  }
}

// 搜索
const handleSearch = (params) => {
  searchParams.value = params
  pageNum.value = 1
  fetchUsers()
}

// 重置搜索
const handleReset = () => {
  searchParams.value = {}
  resetPagination()
  fetchUsers()
}

// 显示创建弹窗
const showCreateDialog = () => {
  isEdit.value = false
  editId.value = null
  editUserData.value = null
  dialogVisible.value = true
}

// 编辑用户
const handleEdit = (id) => {
  router.push(`/users/${id}`)
}

onMounted(() => {
  fetchUsers()
})
</script>

<style lang="scss" scoped>
.user-list-container {
  padding: 84px 24px 24px 24px; // 顶部留出 Navbar 空间
  min-height: 100vh;
  background: var(--color-bg-base);
}

.content-card {
  max-width: 1200px;
  margin: 0 auto;
  @include glass-card;
  padding: 24px;
}

.action-bar {
  margin-bottom: 16px;
  display: flex;
  justify-content: flex-start;

  .create-button {
    @include gradient-button;
    padding: 10px 20px;

    .el-icon {
      margin-right: 4px;
    }
  }
}

.pagination-wrapper {
  margin-top: 24px;
  display: flex;
  justify-content: flex-end;
}
</style>