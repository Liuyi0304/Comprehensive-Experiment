import { defineStore } from 'pinia'

export const useUserStore = defineStore('user', {
  // 1. State 定义
  // 关键修改：把 labId, userId 等常用字段提到最外层，并从 localStorage 初始化
  state: () => ({
    token: localStorage.getItem('token') || '',
    role: localStorage.getItem('role') || '',
    // 🔴 核心修复：初始化时直接读取 labId，转为数字类型
    labId: localStorage.getItem('labId') ? Number(localStorage.getItem('labId')) : null,
    userId: localStorage.getItem('userId') || '',
    username: localStorage.getItem('username') || '',
    // 保留 userInfo 以备不时之需
    userInfo: JSON.parse(localStorage.getItem('userInfo') || '{}')
  }),

  actions: {
    // 2. 登录成功后调用
    setLoginState(data) {
      // --- 更新 Pinia 状态 (内存) ---
      this.token = data.token
      this.role = data.role
      // 🔴 核心修复：确保 state 里的 labId 被赋值
      this.labId = data.labId 
      this.userId = data.userId
      this.username = data.username
      this.userInfo = data
      
      // --- 更新 LocalStorage (硬盘缓存) ---
      localStorage.setItem('token', data.token)
      localStorage.setItem('role', data.role)
      localStorage.setItem('userInfo', JSON.stringify(data))
      
      // 🔴 核心修复：单独存储 labId，保证刷新页面后能读到
      if (data.labId) {
        localStorage.setItem('labId', data.labId)
      }
      if (data.userId) localStorage.setItem('userId', data.userId)
      if (data.username) localStorage.setItem('username', data.username)
    },

    // 3. 退出登录
    logout() {
      this.token = ''
      this.role = ''
      this.labId = null
      this.userId = ''
      this.username = ''
      this.userInfo = {}
      
      localStorage.clear()
    }
  },

  getters: {
    // 辅助判断：是否是管理员或负责人
    isAdminOrManager: (state) => ['admin', 'manager'].includes(state.role)
  }
})