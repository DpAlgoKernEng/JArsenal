<template>
  <div class="resource-list-container">
    <!-- Tab切换 -->
    <el-tabs v-model="activeTab" class="resource-tabs" @tab-change="handleTabChange">
      <el-tab-pane label="菜单资源" name="MENU">
        <ResourceTree
          ref="menuTreeRef"
          :type="activeTab"
          :resources="allResources"
          @create="handleCreate"
          @edit="handleEdit"
          @refresh="loadResources"
        />
      </el-tab-pane>
      <el-tab-pane label="操作资源" name="OPERATION">
        <ResourceTree
          ref="operationTreeRef"
          :type="activeTab"
          :resources="allResources"
          @create="handleCreate"
          @edit="handleEdit"
          @refresh="loadResources"
        />
      </el-tab-pane>
      <el-tab-pane label="API资源" name="API">
        <ResourceTree
          ref="apiTreeRef"
          :type="activeTab"
          :resources="allResources"
          @create="handleCreate"
          @edit="handleEdit"
          @refresh="loadResources"
        />
      </el-tab-pane>
    </el-tabs>

    <!-- 编辑弹窗 -->
    <ResourceForm
      v-model="formVisible"
      :parent-id="formParentId"
      :resource-data="editResourceData"
      :is-edit="isEdit"
      :default-type="activeTab"
      :all-resources="allResources"
      @success="handleFormSuccess"
    />
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { resourceApi } from '../api/resource'
import ResourceTree from '../components/resource/ResourceTree.vue'
import ResourceForm from '../components/resource/ResourceForm.vue'

// 当前Tab
const activeTab = ref('MENU')

// 全部资源列表
const allResources = ref([])

// 树组件引用
const menuTreeRef = ref()
const operationTreeRef = ref()
const apiTreeRef = ref()

// 弹窗状态
const formVisible = ref(false)
const isEdit = ref(false)
const formParentId = ref(null)
const editResourceData = ref(null)

// 加载资源树
const loadResources = async () => {
  try {
    allResources.value = await resourceApi.tree() || []
  } catch (error) {
    // error handled by interceptor
  }
}

// Tab切换
const handleTabChange = (name) => {
  // Tab切换时不需要重新加载，因为数据已经全部加载
}

// 创建资源
const handleCreate = (parentId, type) => {
  isEdit.value = false
  formParentId.value = parentId
  editResourceData.value = null
  formVisible.value = true
}

// 编辑资源
const handleEdit = (resource) => {
  isEdit.value = true
  formParentId.value = null
  editResourceData.value = resource
  formVisible.value = true
}

// 表单提交成功
const handleFormSuccess = async () => {
  await loadResources()
}

onMounted(async () => {
  await loadResources()
})
</script>

<style lang="scss" scoped>
.resource-list-container {
  height: calc(100vh - 60px - 48px); // 减去 header 和 main-content padding
  display: flex;
  flex-direction: column;
  padding: 0;

  .resource-tabs {
    flex: 1;
    display: flex;
    flex-direction: column;

    :deep(.el-tabs__header) {
      margin-bottom: 0;
      padding: 0 16px;
      background: var(--color-bg-glass);
      border-radius: 8px 8px 0 0;
      border: 1px solid var(--color-border);
      border-bottom: none;

      .el-tabs__nav-wrap {
        &::after {
          display: none; // 隐藏默认分隔线
        }
      }

      .el-tabs__item {
        height: 48px;
        line-height: 48px;
        color: var(--color-text-secondary);
        font-size: 14px;
        font-weight: 500;
        transition: all 0.3s ease;

        &:hover {
          color: var(--color-primary);
        }

        &.is-active {
          color: var(--color-primary);
          font-weight: 600;
        }
      }

      .el-tabs__active-bar {
        background: var(--color-primary);
        height: 3px;
        border-radius: 3px 3px 0 0;
      }
    }

    :deep(.el-tabs__content) {
      flex: 1;
      overflow: hidden;
      padding: 16px;
      background: var(--color-bg-glass);
      border-radius: 0 0 8px 8px;
      border: 1px solid var(--color-border);

      .el-tab-pane {
        height: 100%;
      }
    }
  }
}
</style>