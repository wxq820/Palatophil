import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'
import router from '@/router'

const request = axios.create({
  baseURL: '/api',
  timeout: 15000
})

request.interceptors.request.use((config) => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.Authorization = `Bearer ${userStore.token}`
  }
  return config
})

request.interceptors.response.use(
  (res) => {
    const body = res.data || {}
    if (body.code === 0) return body.data
    ElMessage.error(body.message || '请求失败')
    return Promise.reject(body)
  },
  (err) => {
    if (err.response?.status === 401) {
      useUserStore().logout()
      router.push('/login')
      ElMessage.error('请先登录')
    } else {
      ElMessage.error(err.message || '网络错误')
    }
    return Promise.reject(err)
  }
)

export default request
