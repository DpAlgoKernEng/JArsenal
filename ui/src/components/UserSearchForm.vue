<template>
  <el-form inline class="search-form">
    <el-form-item>
      <el-input
        v-model="searchForm.username"
        placeholder="搜索用户名"
        clearable
        :prefix-icon="Search"
        @keyup.enter="handleSearch"
        class="search-input"
      />
    </el-form-item>
    <el-form-item>
      <el-select
        v-model="searchForm.status"
        placeholder="状态"
        clearable
        class="status-select"
      >
        <el-option label="正常" :value="1" />
        <el-option label="禁用" :value="0" />
      </el-select>
    </el-form-item>
    <el-form-item>
      <el-button class="search-button" @click="handleSearch">
        <el-icon><Search /></el-icon>
        搜索
      </el-button>
      <el-button class="reset-button" @click="handleReset">
        <el-icon><Refresh /></el-icon>
        重置
      </el-button>
    </el-form-item>
  </el-form>
</template>

<script setup>
import { reactive } from 'vue'
import { Search, Refresh } from '@element-plus/icons-vue'

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

defineExpose({
  reset: () => {
    searchForm.username = ''
    searchForm.status = null
  }
})
</script>

<style lang="scss" scoped>
.search-form {
  display: flex;
  flex-wrap: wrap;
  gap: 12px;
  margin-bottom: 20px;

  .el-form-item {
    margin-bottom: 0;
  }

  .search-input {
    width: 200px;

    :deep(.el-input__wrapper) {
      background: var(--color-bg-glass);
      box-shadow: 0 0 0 1px var(--color-border) inset;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 0 0 1px var(--color-primary) inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px var(--color-primary) inset, var(--shadow-glow);
      }
    }
  }

  .status-select {
    width: 120px;

    :deep(.el-input__wrapper) {
      background: var(--color-bg-glass);
      box-shadow: 0 0 0 1px var(--color-border) inset;
      transition: all 0.3s ease;

      &:hover {
        box-shadow: 0 0 0 1px var(--color-primary) inset;
      }

      &.is-focus {
        box-shadow: 0 0 0 1px var(--color-primary) inset, var(--shadow-glow);
      }
    }
  }

  .search-button {
    @include gradient-button;
    padding: 8px 16px;

    .el-icon {
      margin-right: 4px;
    }
  }

  .reset-button {
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
}
</style>
