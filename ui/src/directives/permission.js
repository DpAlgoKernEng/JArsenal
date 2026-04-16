import { usePermissionStore } from '../stores/permission'

/**
 * v-permission 指令
 * 用于控制按钮/元素的显示权限
 *
 * 使用方式：
 * - v-permission="{ resource: 'USER', action: 'CREATE' }"
 * - v-permission="{ resource: 'USER', action: 'DELETE' }"
 *
 * 注意：使用 display:none 而非移除 DOM 元素，避免与 Vue 虚拟 DOM 冲突
 */
export const permissionDirective = {
  mounted(el, binding) {
    const { resource, action } = binding.value || {}

    if (!resource || !action) {
      console.warn('v-permission 需要提供 resource 和 action 参数')
      return
    }

    const permissionStore = usePermissionStore()

    // 检查权限 - 使用 display:none 隐藏元素
    if (!permissionStore.hasAction(resource, action)) {
      el.style.display = 'none'
    }
  },

  updated(el, binding) {
    const { resource, action } = binding.value || {}

    if (!resource || !action) {
      return
    }

    const permissionStore = usePermissionStore()

    // 检查权限（如果权限发生变化需要更新显示状态）
    if (!permissionStore.hasAction(resource, action)) {
      el.style.display = 'none'
    } else {
      el.style.display = ''
    }
  }
}

/**
 * v-menu-permission 指令
 * 用于控制菜单项的显示权限
 *
 * 使用方式：
 * - v-menu-permission="'USER_MANAGE'"
 */
export const menuPermissionDirective = {
  mounted(el, binding) {
    const menuCode = binding.value

    if (!menuCode) {
      console.warn('v-menu-permission 需要提供菜单编码参数')
      return
    }

    const permissionStore = usePermissionStore()

    // 检查菜单权限 - 使用 display:none
    if (!permissionStore.hasMenu(menuCode)) {
      el.style.display = 'none'
    }
  }
}

/**
 * v-field-permission 指令
 * 用于控制表单字段的显示/编辑权限
 *
 * 使用方式：
 * - v-field-permission="{ resource: 'USER', field: 'salary', mode: 'view' }"
 * - v-field-permission="{ resource: 'USER', field: 'salary', mode: 'edit' }"
 */
export const fieldPermissionDirective = {
  mounted(el, binding) {
    const { resource, field, mode } = binding.value || {}

    if (!resource || !field || !mode) {
      console.warn('v-field-permission 需要提供 resource, field 和 mode 参数')
      return
    }

    const permissionStore = usePermissionStore()

    if (mode === 'view') {
      // 查看模式：检查是否可查看 - 使用 display:none
      if (!permissionStore.canViewField(resource, field)) {
        el.style.display = 'none'
      }
    } else if (mode === 'edit') {
      // 编辑模式：检查是否可编辑
      if (!permissionStore.canEditField(resource, field)) {
        // 设置为只读或禁用
        if (el.tagName === 'INPUT' || el.tagName === 'SELECT' || el.tagName === 'TEXTAREA') {
          el.disabled = true
        } else {
          // 对于 Element Plus 组件，尝试设置 disabled 属性
          const inputEl = el.querySelector('input, select, textarea')
          if (inputEl) {
            inputEl.disabled = true
          }
        }
        el.style.opacity = '0.6'
        el.style.cursor = 'not-allowed'
      }
    }
  }
}

/**
 * 注册所有权限指令
 */
export const setupPermissionDirectives = (app) => {
  app.directive('permission', permissionDirective)
  app.directive('menu-permission', menuPermissionDirective)
  app.directive('field-permission', fieldPermissionDirective)
}