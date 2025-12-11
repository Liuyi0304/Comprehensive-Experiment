import { createRouter, createWebHistory } from 'vue-router'

// 引入页面组件
import Login from '@/views/Login.vue'
import DeviceList from '@/views/DeviceList.vue'
import UserManage from '@/views/UserManage.vue' // 1. 引入新写的用户管理页面
import LabManage from '@/views/LabManage.vue' // 引入

const routes = [
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/login',
    name: 'Login',
    component: Login
  },
  {
    path: '/dashboard',
    name: 'Dashboard',
    component: DeviceList
  },
  // 2. 添加用户管理的路由规则
  {
    path: '/user',
    name: 'UserManage',
    component: UserManage
  },
    {
    path: '/lab',
    name: 'LabManage',
    component: LabManage
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

// 路由守卫：没登录只能去登录页
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  if (to.path !== '/login' && !token) {
    next('/login')
  } else {
    next()
  }
})

export default router