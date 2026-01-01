import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api', // 对应 vue.config.js 的 proxy
  timeout: 5000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 从 localStorage 获取 Token
    const token = localStorage.getItem('token')
    if (token) {
      // 这里的 Header Key 必须和你后端 LoginInterceptor 取的一致
      // 通常是 'Authorization' 或 'token'
      // 假设你后端是: request.getHeader("token")
      config.headers['token'] = token 
      // 如果你后端是标准的 Bearer Token:
      // config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  error => Promise.reject(error)
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    const res = response.data
    // 假设后端通用返回结构 Result<T> code=200 为成功
    if (res.code !== 200 && res.code !== 0) {
      ElMessage.error(res.message || '系统错误')

      // 401: Token 过期或非法
      if (res.code === 401 || res.code === 403) {
        localStorage.clear()
        window.location.href = '/login'
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    ElMessage.error(error.response?.data?.message || '网络连接失败')
    return Promise.reject(error)
  }
)

export default service