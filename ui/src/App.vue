<template>
  <el-config-provider :locale="zhCn">
    <router-view />
  </el-config-provider>
</template>

<script setup>
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { useTheme } from './composables/useTheme'

// 初始化主题
useTheme()
</script>

<style>
/* 全局样式已由 global.scss 处理，这里仅保留必要覆盖 */
</style>
