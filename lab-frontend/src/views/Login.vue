<template>
  <div class="login-wrapper">
    <el-card class="login-card">
      <h2 style="text-align: center">实验室系统登录</h2>
      <el-form :model="form" :rules="rules" ref="formRef">
        <el-form-item prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" :prefix-icon="User" />
        </el-form-item>
        <el-form-item prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" :prefix-icon="Lock" show-password />
        </el-form-item>
        <el-button type="primary" :loading="loading" @click="onSubmit" style="width: 100%">登录</el-button>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { login } from '@/api/auth'
import { useUserStore } from '@/stores/user'
import { User, Lock } from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)

const form = reactive({ username: '', password: '' })
const rules = {
  username: [{ required: true, message: '必填', trigger: 'blur' }],
  password: [{ required: true, message: '必填', trigger: 'blur' }]
}

const onSubmit = async () => {
  if (!form.username || !form.password) return
  loading.value = true
  try {
    const res = await login(form)
    // res.data 应包含 { token, role, username, id ... }
    userStore.setLoginState(res.data) 
    ElMessage.success('登录成功')
    router.push('/dashboard')
  } catch (e) {
    // 错误在 request.js 已拦截
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-wrapper { display: flex; justify-content: center; align-items: center; height: 100vh; background: #eee; }
.login-card { width: 400px; padding: 20px; }
</style>