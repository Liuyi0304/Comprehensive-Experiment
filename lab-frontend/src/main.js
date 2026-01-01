import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

// 引入 Element Plus
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import * as ElementPlusIconsVue from '@element-plus/icons-vue'

// ==========================================================
// 🛡️ 1. 全局错误拦截 (必须在 app 挂载前执行)
// ==========================================================

// A. 拦截未捕获的 Promise 错误 (解决 ERROR cancel 弹窗)
window.addEventListener('unhandledrejection', event => {
  const reason = event.reason
  // 如果错误原因是 'cancel' (Element Plus 弹窗取消) 或包含 cancel
  if (reason === 'cancel' || reason === 'cancel' || (reason && reason.message === 'cancel')) {
    event.preventDefault() // 阻止控制台报红
    event.stopPropagation()
    return true
  }
})

// B. 屏蔽 ResizeObserver 循环警告 (开发环境常见噪音)
if (process.env.NODE_ENV === 'development') {
  const originalError = console.error
  console.error = (...args) => {
    if (
      args[0] &&
      typeof args[0] === 'string' &&
      args[0].includes('ResizeObserver loop completed with undelivered notifications')
    ) {
      return // 静默忽略
    }
    originalError.apply(console, args)
  }
}

// ==========================================================
// 🚀 2. 应用实例初始化
// ==========================================================
const app = createApp(App)

// C. 拦截 Vue 框架内部错误
app.config.errorHandler = (err, vm, info) => {
  // 忽略 'cancel' 字符串错误
  if (err === 'cancel') return
  // 忽略 Error 对象中的 message 为 'cancel'
  if (err instanceof Error && err.message === 'cancel') return
  
  console.error('Vue Error:', err)
}

// 注册所有图标
for (const [key, component] of Object.entries(ElementPlusIconsVue)) {
  app.component(key, component)
}

// 挂载插件
app.use(createPinia())
app.use(router)
app.use(ElementPlus)

// 挂载应用
app.mount('#app')