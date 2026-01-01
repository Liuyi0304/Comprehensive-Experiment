import { createRouter, createWebHistory } from 'vue-router'
import Layout from '@/layout/index.vue'

// ====================================================
// 路由配置
// ====================================================
export const routes = [
  // 1. 登录页
  {
    path: '/login',
    component: () => import('@/views/Login.vue'),
    hidden: true,
    meta: { title: '登录' }
  },

  // 2. 404 页面
  {
    path: '/:pathMatch(.*)*',
    component: () => import('@/views/404.vue'),
    hidden: true
  },

  // 3. 首页 (Dashboard)
  {
    path: '/',
    component: Layout,
    redirect: '/dashboard',
    children: [
      {
        path: 'dashboard',
        name: 'Dashboard',
        component: () => import('@/views/dashboard/index.vue'),
        meta: { title: '首页', icon: 'House', requiresAuth: true } // 这里 icon 改为了 House，更符合 Element Plus
      }
    ]
  },

  // 4. 系统管理
  {
    path: '/system',
    component: Layout,
    redirect: '/system/labs',
    meta: { title: '系统管理', icon: 'Setting', requiresAuth: true },
    children: [
      // 4.1 实验室管理
      {
        path: 'labs',
        name: 'LabManage',
        component: () => import('@/views/system/lab.vue'),
        meta: { title: '实验室管理', icon: 'School' }
      },
      // 4.2 分类管理
      {
        path: 'category',
        name: 'Category',
        component: () => import('@/views/system/category.vue'), // 确保你文件名也是 category.vue
        meta: { title: '分类管理', icon: 'Collection' }
      },
      // 4.3 用户管理 (✅ 已取消注释并修正路径)
      {
        path: 'user',
        name: 'UserManage',
        component: () => import('@/views/system/user.vue'), // 指向 src/views/system/user.vue
        meta: { title: '用户管理', icon: 'User' }
      }
    ]
  },

  // ✅ 新增：采购审批管理 (仅管理员可见)
  {
    path: '/purchase',
    component: Layout, // 使用你的主布局组件
    redirect: '/purchase/audit',
    name: 'PurchaseManage',
    meta: { 
      title: '采购管理', 
      icon: 'Money', // 找一个像钱或印章的图标
      roles: ['admin'] // 关键：只有 admin 角色能看到这个菜单
    },
    children: [
      {
        path: 'audit',
        name: 'PurchaseAudit',
        component: () => import('@/views/purchase.vue'), // 👈 指向刚才创建的文件
        meta: { 
          title: '申请审批台', 
          icon: 'Stamp',
          roles: ['admin'] // 双重保险
        }
      }
    ]
  },
// ✅ 新增：报废审批路由（仅管理员/负责人可见）
{
  path: '/scrap-approve', // 与侧边栏 index 一致
  component: Layout,
  redirect: '/scrap-approve',
  name: 'ScrapApprove',
  meta: { 
    title: '报废审批', 
    icon: 'Delete', // 与侧边栏图标一致
    requiresAuth: true,
    roles: ['admin', 'manager'] // 根据需求调整可见角色
  },
  children: [
    {
      path: '', // 子路由为空，直接匹配 /scrap-approve
      name: 'ScrapApproveList',
      component: () => import('@/views/scrapapprove.vue'), // 指向你的文件路径
      meta: { 
        title: '报废审批', 
        icon: 'Delete' 
      }
    }
  ]
},


{
  path: '/repair',
  component: Layout,
  redirect: '/repair/manage',
  name: 'RepairManage',
  meta: { title: '维修管理', icon: 'Tools', requiresAuth: true },
  children: [
    {
      path: 'manage',
      name: 'RepairManageList',
      component: () => import('@/views/RepairManage.vue'),
      meta: { title: '维修工单管理', icon: 'Tools' }
    }
  ]
},
// src/router/index.js

{
  path: '/usage',
  component: Layout,
  name: 'UsageModule', // 确保这个名字是唯一的，不要和别的路由重复
  meta: { title: '设备领用', icon: 'Operation' },
  children: [
    {
      path: 'manage', // 🔴 改成具体的路径名
      name: 'UsageManage',
      component: () => import('@/views/UsageManage.vue'),
      meta: { title: '领用登记', icon: 'EditPen' }
    }
  ]
},
  // ... 其他路由 ...
  // 5. 设备管理
  {
    path: '/device',
    component: Layout,
    redirect: '/device/list',
    name: 'DeviceManage',
    meta: { title: '设备管理', icon: 'Monitor', requiresAuth: true },
    children: [
      {
        path: 'list',
        name: 'DeviceList',
        component: () => import('@/views/device/index.vue'),
        meta: { title: '设备列表' }
      }
    ]
  },

  // 6. 我的申请 (仅普通用户和负责人可见)
// 6. 我的申请
  {
    path: '/my-application',
    component: Layout,
    redirect: '/my-application/index', // 默认跳到采购
    meta: { title: '我的申请', icon: 'List' },
    children: [
      {
        // 重点：路径叫 'index'，对应你的文件名 index.vue
        path: 'index', 
        name: 'MyPurchase', // 给个名字叫采购
        component: () => import('@/views/my-application/index.vue'), // 👈 指向你原来的文件，不改名！
        meta: { title: '采购申请记录' }
      },
      {
        // 新增的报废
        path: 'scrap',
        name: 'MyScrap',
        component: () => import('@/views/my-application/scrap.vue'), // 👈 新建的 scrap.vue
        meta: { title: '报废申请记录' }
      }
    ]
  },
  


]


// ====================================================
// Router 实例
// ====================================================
const router = createRouter({
  history: createWebHistory(),
  scrollBehavior: () => ({ top: 0 }),
  routes
})

// ====================================================
// 路由守卫
// ====================================================
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const whiteList = ['/login', '/404']

  if (token) {
    if (to.path === '/login') {
      next({ path: '/' })
    } else {
      next()
    }
  } else {
    // 如果没有 Token，但在白名单内，直接放行
    if (whiteList.includes(to.path)) {
      next()
    } else {
      // 否则跳去登录页
      next(`/login?redirect=${to.path}`)
    }
  }
})

export default router