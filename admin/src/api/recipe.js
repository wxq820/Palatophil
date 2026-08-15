import request from '@/utils/request'

export const recipeApi = {
  page: (params) => request.get('/recipes', { params }),
  detail: (id) => request.get(`/recipes/${id}`),
  create: (data) => request.post('/recipes', data),
  update: (id, data) => request.put(`/recipes/${id}`, data),
  remove: (id) => request.delete(`/recipes/${id}`),
  copy: (id) => request.post(`/recipes/${id}/copy`),
  audit: (id, auditStatus) => request.post(`/recipes/${id}/audit`, { auditStatus })
}

export const VISIBILITY = {
  PRIVATE: { label: '私有', type: 'info' },
  PUBLIC: { label: '公开', type: 'success' }
}

export const AUDIT_STATUS = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' }
}
