import request from '@/utils/request'

export const authApi = {
  adminLogin: (data) => request.post('/auth/admin-login', data),
  me: () => request.get('/auth/me'),
  logout: () => request.post('/auth/logout')
}
