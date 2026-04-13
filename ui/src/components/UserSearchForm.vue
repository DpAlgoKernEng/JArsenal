<template>
  <el-form inline class="search-form">
    <el-form-item label="用户名">
      <el-input
        v-model="searchForm.username"
        placeholder="搜索用户名"
        clearable
        @keyup.enter="handleSearch"
      />
    </el-form-item>
    <el-form-item label="状态">
      <el-select
        v-model="searchForm.status"
        placeholder="选择状态"
        clearable
        style="width: 120px"
      >
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button type="primary" @click="handleSearch">搜索</el-button>
      <el-button @click="handleReset">重置</el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'

const emit = defineEmits(['search', 'reset'])

const searchForm = reactive({
  username: '',
  status: null
})

const handleSearch = () => {
  emit('search', {
    username: searchForm.username || undefined,
    status: searchForm.status ?? undefined
  })
}

const handleReset = () => {
  searchForm.username = ''
  searchForm.status = null
  emit('reset')
}

// 暴露方法供父组件调用
defineExpose({
  reset: () => {
    searchForm.username = ''
    searchForm.status = null
  }
})
</script>

<style scoped>
.search-form {
  margin-bottom: 16px;
}
</style>