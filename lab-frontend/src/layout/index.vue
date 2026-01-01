<template>
  <div class="app-wrapper">
    <el-container>
      <!-- 使用封装好的 Sidebar 组件 -->
      <Sidebar />
      
      <el-container class="main-container">
        <!-- 顶部导航栏 -->
        <el-header class="header-bar">
          <div class="breadcrumb">
            <!-- 这里以后可以加面包屑 -->
            <span>当前位置：{{ route.meta.title || '系统' }}</span>
          </div>
          <div class="right-menu">
            <span class="username">欢迎，{{ userStore.userInfo.username || 'User' }}</span>
            <el-button type="danger" link @click="handleLogout" size="small">退出登录</el-button>
          </div>
        </el-header>

        <!-- 主内容区域 (路由出口) -->
        <el-main class="app-main">
          <!-- 加个淡入淡出动画让体验更好 -->
          <router-view v-slot="{ Component }">
            <transition name="fade-transform" mode="out-in">
              <component :is="Component" />
            </transition>
          </router-view>
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import Sidebar from './components/Sidebar.vue' // 引入组件
import { useUserStore } from '@/stores/user'
import { useRouter, useRoute } from 'vue-router'
import { ElMessageBox } from 'element-plus'

const userStore = useUserStore()
const router = useRouter()
const route = useRoute()

const handleLogout = () => {
  ElMessageBox.confirm('确定要退出登录吗?', '提示', {
    confirmButtonText: '退出',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    userStore.logout()
    router.push('/login')
  })
}
</script>

<style scoped>
.app-wrapper {
  position: relative;
  height: 100%;
  width: 100%;
}

.main-container {
  min-height: 100vh;
  transition: margin-left .28s;
  background-color: #f0f2f5; /* 灰色背景让卡片更明显 */
}

.header-bar {
  background: #fff;
  box-shadow: 0 1px 4px rgba(0,21,41,.08);
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
  height: 60px;
}

.username {
  font-size: 14px;
  color: #666;
  margin-right: 15px;
}

.app-main {
  padding: 20px;
  position: relative;
  overflow: hidden;
}

/* 简单的页面切换动画 */
.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.3s;
}
.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-30px);
}
.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(30px);
}
</style>