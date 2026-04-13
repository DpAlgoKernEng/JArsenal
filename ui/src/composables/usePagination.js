import { ref, computed } from 'vue'

/**
 * 分页逻辑复用
 * @param {number} defaultPageSize - 默认每页条数
 * @returns {object} 分页状态和方法
 */
export function usePagination(defaultPageSize = 10) {
  const pageNum = ref(1)
  const pageSize = ref(defaultPageSize)
  const total = ref(0)

  // 计算属性：是否有数据
  const hasData = computed(() => total.value > 0)

  // 计算属性：总页数
  const totalPages = computed(() => Math.ceil(total.value / pageSize.value))

  // 重置分页
  const resetPagination = () => {
    pageNum.value = 1
    total.value = 0
  }

  // 构建分页参数
  const getPaginationParams = () => ({
    pageNum: pageNum.value,
    pageSize: pageSize.value
  })

  // 处理页码变化
  const handlePageChange = (callback) => {
    if (typeof callback === 'function') {
      callback()
    }
  }

  // 处理每页条数变化
  const handleSizeChange = (callback) => {
    pageNum.value = 1 // 切换每页条数时重置到第一页
    if (typeof callback === 'function') {
      callback()
    }
  }

  return {
    pageNum,
    pageSize,
    total,
    hasData,
    totalPages,
    resetPagination,
    getPaginationParams,
    handlePageChange,
    handleSizeChange
  }
}