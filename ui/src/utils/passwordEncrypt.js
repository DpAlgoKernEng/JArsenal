/**
 * 密码加密工具
 * 前端使用 SHA-256 对密码进行单向哈希
 * 防止明文密码在传输过程中暴露
 */
import { sha256 } from 'js-sha256'

/**
 * 对密码进行 SHA-256 哈希
 * @param {string} password 原始密码
 * @returns {string} SHA-256 哈希值（小写十六进制）
 */
export function hashPassword(password) {
  if (!password) {
    throw new Error('密码不能为空')
  }
  return sha256(password)
}

/**
 * 验证密码强度规则（在哈希前验证）
 * @param {string} password 原始密码
 * @returns {Object} { valid: boolean, message: string }
 */
export function validatePasswordStrength(password) {
  if (!password) {
    return { valid: false, message: '密码不能为空' }
  }
  if (password.length < 6) {
    return { valid: false, message: '密码长度至少6位' }
  }
  if (password.length > 50) {
    return { valid: false, message: '密码长度不能超过50位' }
  }
  return { valid: true, message: '' }
}