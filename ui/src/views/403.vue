<template>
  <div class="forbidden-container">
    <div class="forbidden-content">
      <div class="forbidden-icon">
        <el-icon :size="80">
          <Lock />
        </el-icon>
      </div>
      <h1 class="forbidden-title">403</h1>
      <p class="forbidden-message">抱歉，您没有权限访问此页面</p>
      <div class="forbidden-actions">
        <el-button @click="goBack">返回上一页</el-button>
        <el-button type="primary" @click="goHome">返回首页</el-button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { Lock } from '@element-plus/icons-vue'

const router = useRouter()

const goBack = () => {
  router.back()
}

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.forbidden-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--color-bg-base);
}

.forbidden-content {
  text-align: center;
  @include glass-card;
  padding: 60px 80px;

  .forbidden-icon {
    color: var(--color-warning);
    margin-bottom: 24px;
  }

  .forbidden-title {
    font-size: 48px;
    font-weight: 700;
    color: var(--color-text-primary);
    margin-bottom: 16px;
  }

  .forbidden-message {
    font-size: 16px;
    color: var(--color-text-secondary);
    margin-bottom: 32px;
  }

  .forbidden-actions {
    display: flex;
    gap: 16px;
    justify-content: center;

    .el-button {
      padding: 12px 24px;
    }
  }
}
</style>