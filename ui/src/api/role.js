import api from './index'

/**
 * 角色管理 API
 */
export const roleApi = {
  tree: () => api.get('/roles'),
  get: (id) => api.get(`/roles/${id}`),
  create: (data) => api.post('/roles', data),
  update: (id, data) => api.put(`/roles/${id}`, data),
  delete: (id) => api.delete(`/roles/${id}`),
  getUserRoles: (userId) => api.get(`/roles/user/${userId}`),
  assignToUser: (userId, roleIds) => api.post(`/roles/user/${userId}/roles`, { roleIds }),
  removeFromUser: (userId, roleId) => api.delete(`/roles/user/${userId}/roles/${roleId}`),
  assignPermission: (roleId, data) => api.post(`/roles/${roleId}/permissions`, data)
}
