import request from '@/utils/request'

export const ingredientApi = {
  page: (params) => request.get('/ingredients', { params }),
  detail: (id) => request.get(`/ingredients/${id}`),
  create: (data) => request.post('/ingredients', data),
  update: (id, data) => request.put(`/ingredients/${id}`, data),
  remove: (id) => request.delete(`/ingredients/${id}`),
  audit: (id, auditStatus) => request.post(`/ingredients/${id}/audit`, { auditStatus })
}

export const CATEGORIES = [
  { value: 'VEGETABLE', label: '蔬菜' },
  { value: 'MEAT', label: '肉类' },
  { value: 'AQUATIC', label: '水产' },
  { value: 'GRAIN', label: '主食' },
  { value: 'SEASONING', label: '调料' },
  { value: 'DAIRY', label: '蛋奶' },
  { value: 'FRUIT', label: '水果' },
  { value: 'OTHER', label: '其他' }
]

export const AUDIT_STATUS = {
  0: { label: '待审核', type: 'warning' },
  1: { label: '已通过', type: 'success' },
  2: { label: '已拒绝', type: 'danger' }
}
