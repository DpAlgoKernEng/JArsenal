<template>
  <div class="not-found-container">
    <div class="not-found-content">
      <h1 class="error-code">404</h1>
      <p class="error-message">页面不存在</p>
      <p class="error-description">您访问的页面可能已被移除或暂时不可用</p>
      <el-button class="home-button" @click="goHome">
        <el-icon><HomeFilled /></el-icon>
        返回首页
      </el-button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { HomeFilled } from '@element-plus/icons-vue'

const router = useRouter()

const goHome = () => {
  router.push('/')
}
</script>

<style lang="scss" scoped>
.not-found-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 100vh;
  background: var(--gradient-bg);

  html.dark & {
    background: linear-gradient(135deg, var(--color-bg-base) 0%, #1e1b4b 50%, #0f172a 100%);
  }
}

.not-found-content {
  text-align: center;
  @include glass-card;
  padding: 48px;

  .error-code {
    font-size: 72px;
    font-weight: 700;
    @include gradient-text;
    margin-bottom: 16px;
    line-height: 1;
  }

  .error-message {
    font-size: 24px;
    color: var(--color-text-primary);
    margin-bottom: 8px;
  }

  .error-description {
    font-size: 14px;
    color: var(--color-text-secondary);
    margin-bottom: 32px;
  }

  .home-button {
    @include gradient-button;
    padding: 12px 32px;
    font-size: 16px;

    .el-icon {
      margin-right: 8px;
    }
  }
}
</style>
