<template>
  <el-aside width="210px" class="sidebar-container">
    <div class="logo">
      <h3>Lab System</h3>
    </div>

    <!-- 菜单区域 -->
    <el-menu
      :default-active="activeMenu"
      background-color="#304156"
      text-color="#bfcbd9"
      active-text-color="#409EFF"
      router
      unique-opened
    >
      <!-- 1. 工作台 (所有人可见) -->
      <el-menu-item index="/dashboard">
        <el-icon><Odometer /></el-icon>
        <span>工作台</span>
      </el-menu-item>

      <!-- 2. 设备管理 (所有人可见) -->
      <el-menu-item index="/device">
        <el-icon><Monitor /></el-icon>
        <span>设备列表</span>
      </el-menu-item>

      <!-- 3. 🔴 新增：领用登记 (User 和 Manager 可见) -->
      <el-menu-item index="/usage/manage" v-if="userStore.role !== 'admin'">
        <el-icon><EditPen /></el-icon>
        <span>领用登记</span>
      </el-menu-item>

      <!-- 4. 维修管理 (所有人可见 - 这里的权限你可以根据需求调整) -->
      <el-menu-item index="/repair" v-if="userStore.role !== 'admin'">
        <el-icon><Tools /></el-icon>
        <span>维修管理</span>
      </el-menu-item>

      <!-- 5. 我的申请 (二级菜单) -->
      <el-sub-menu index="/my-application" v-if="userStore.role !== 'admin'">
        <template #title>
          <el-icon><List /></el-icon>
          <span>我的申请</span>
        </template>
        
        <el-menu-item index="/my-application/index">
          <span>采购记录</span>
        </el-menu-item>
        
        <el-menu-item index="/my-application/scrap">
          <span>报废记录</span>
        </el-menu-item>
      </el-sub-menu>
      
      <!-- 6. 报废审批 (仅管理员) -->
      <el-menu-item index="/scrap-approve" v-if="userStore.role === 'admin'">
        <el-icon><Delete /></el-icon>
        <span>报废审批</span>
      </el-menu-item>

      <!-- 7. 采购审批 (仅管理员) -->
      <el-menu-item index="/purchase/audit" v-if="userStore.role === 'admin'">
        <el-icon><Money /></el-icon>
        <span>采购审批</span>
      </el-menu-item>

      <!-- 8. 系统管理 (仅管理员) -->
      <el-sub-menu index="/system" v-if="userStore.role === 'admin'">
        <template #title>
          <el-icon><Setting /></el-icon>
          <span>系统管理</span>
        </template>
        <el-menu-item index="/system/user">User-用户管理</el-menu-item>
        <el-menu-item index="/system/labs">Lab-实验室管理</el-menu-item>
        <el-menu-item index="/system/category">Category-种类管理</el-menu-item>
      </el-sub-menu>

    </el-menu>
  </el-aside>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import { useUserStore } from '@/stores/user'
// 🔴 导入新图标 EditPen
import { Odometer, Monitor, Delete, Setting, Money, List, Tools, EditPen } from '@element-plus/icons-vue'

const route = useRoute()
const userStore = useUserStore()

// 高亮当前路由
const activeMenu = computed(() => {
  const path = route.path

  // 1. 采购审批高亮
  if (path.startsWith('/purchase')) return '/purchase/audit'
  
  // 2. 我的申请高亮
  if (path.startsWith('/my-application')) return '/my-application/index'

  // 3. 系统管理高亮
  if (path.startsWith('/system/user')) return '/system/user'
  if (path.startsWith('/system/labs')) return '/system/labs'
  if (path.startsWith('/system/category')) return '/system/category'
  
  // 4. 报废审批高亮
  if (path.startsWith('/scrap-approve')) return '/scrap-approve'

  // 5. 维修管理高亮
  if (path.startsWith('/repair')) return '/repair'

  // 6. 🔴 领用登记高亮逻辑
  if (path.startsWith('/usage')) return '/usage/manage'
  
  return path
})
</script>
<style scoped>
.sidebar-container {
  height: 100vh;
  background-color: #304156;
  overflow-x: hidden;
  box-shadow: 2px 0 6px rgba(0,21,41,.35);
  transition: width 0.3s;
}

.logo {
  height: 60px;
  line-height: 60px;
  background: #2b2f3a;
  text-align: center;
  overflow: hidden;
}

.logo h3 {
  color: #fff;
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
}

.el-menu {
  border-right: none; 
}
</style>
