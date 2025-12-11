// src/utils/request.js
import axios from 'axios'
import { ElMessage } from 'element-plus'

const service = axios.create({
  baseURL: '/api', // 这里对应你后端的 @RequestMapping("/api/...")，开发环境需配置代理
  timeout: 5000
})

// 请求拦截器：自动携带 Token
service.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
      if (token) {
        // 确保后端能读到这个 Header
        config.headers['Authorization'] = token 
      }
      return config
  },
  error => Promise.reject(error)
)

// 响应拦截器：统一处理 Result 结构
// src/utils/request.js

service.interceptors.response.use(
  response => {
    const res = response.data
    
    // 如果 code 不是 200，说明业务出错（比如密码错误）
    if (res.code !== 200) {
      // 关键修改：优先读取 res.msg，如果没有再读 res.message
      const errorMessage = res.msg || res.message || '系统错误'
      
      ElMessage.error(errorMessage)
      
      // 抛出错误，中断后续逻辑
      return Promise.reject(new Error(errorMessage))
    }
    return res.data
  },
  error => {
    console.error('err' + error)
    ElMessage.error(error.message || '网络请求失败')
    return Promise.reject(error)
  }
)
export default service