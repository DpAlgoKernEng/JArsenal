<template>
  <div class="placeholder-container">
    <div class="placeholder-content">
      <h2>页面开发中</h2>
      <p>该页面尚未实现，请稍后再试</p>
    </div>
  </div>
</template>

<script setup>
</script>

<style lang="scss" scoped>
.placeholder-container {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 400px;
}

.placeholder-content {
  text-align: center;

  h2 {
    color: var(--color-text-primary);
    margin-bottom: 16px;
  }

  p {
    color: var(--color-text-secondary);
  }
}
</style>