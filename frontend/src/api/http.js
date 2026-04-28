import axios from 'axios'
import { ElMessage } from 'element-plus'

const http = axios.create({
  baseURL: '/api',
  timeout: 120000,
  withCredentials: true,
})

http.interceptors.response.use(
  (response) => {
    const body = response.data || {}
    if (body.code === 0) {
      return body.data
    }

    const silent = response.config?.silent
    const message = body.message || '请求失败'

    if (body.code === 40100) {
      if (!silent && window.location.pathname !== '/login') {
        ElMessage.warning('请先登录后再进入练兵场')
        window.setTimeout(() => {
          window.location.href = '/login'
        }, 250)
      }
    } else if (!silent) {
      ElMessage.error(message)
    }

    return Promise.reject(new Error(message))
  },
  (error) => {
    if (!error.config?.silent) {
      const message = error.response?.data?.message || error.message || '网络异常，请检查后端服务'
      ElMessage.error(message)
    }
    return Promise.reject(error)
  },
)

export default http
