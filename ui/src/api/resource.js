import api from './index'

/**
 * 资源管理 API
 */
export const resourceApi = {
  tree: () => api.get('/resources'),
  treeV2: () => api.get('/resources/tree'),
  get: (id) => api.get(`/resources/${id}`),
  create: (data) => api.post('/resources', data),
  update: (id, data) => api.put(`/resources/${id}`, data),
  delete: (id) => api.delete(`/resources/${id}`),
  enable: (id) => api.post(`/resources/${id}/enable`),
  disable: (id) => api.post(`/resources/${id}/disable`),
  getFields: (id) => api.get(`/resources/${id}/fields`),
  addField: (id, data) => api.post(`/resources/${id}/fields`, data)
}
