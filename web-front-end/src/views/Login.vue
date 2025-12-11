<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="title">实验室设备管理系统</h2>
      <el-form :model="loginForm" label-width="0">
        <el-form-item>
          <el-input v-model="loginForm.username" placeholder="用户名/学号" prefix-icon="User" />
        </el-form-item>
        <el-form-item>
          <el-input v-model="loginForm.password" type="password" placeholder="密码" prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" class="login-btn" @click="handleLogin" :loading="loading">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/lab'
import { ElMessage } from 'element-plus'

const router = useRouter()
const loading = ref(false)
const loginForm = ref({
  username: '',
  password: ''
})

const handleLogin = async () => {
  // 1. 校验空值
  if (!loginForm.value.username || !loginForm.value.password) {
    return ElMessage.warning('请输入用户名和密码')
  }
  
  try {
    loading.value = true
    // 2. 发起请求
    const data = await login(loginForm.value)
    
    // 3. 登录成功逻辑
    localStorage.setItem('token', data.token) 
    localStorage.setItem('userId', data.userId)
    ElMessage.success('登录成功')
    router.push('/dashboard')
    
  } catch (error) {
    // === 关键修改：添加 catch 块 ===
    // 这里捕获 request.js 抛出的错误，防止控制台报红
    // 因为 request.js 已经弹出了 ElMessage.error，这里可以留空，或者打印日志
    console.warn("登录失败:", error.message)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background-color: #f0f2f5;
}
.login-card {
  width: 400px;
  padding: 20px;
}
.title {
  text-align: center;
  margin-bottom: 30px;
  color: #333;
}
.login-btn {
  width: 100%;
}
</style>