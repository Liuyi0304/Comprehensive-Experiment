<template>
  <div class="app-container">
    <el-card shadow="always">
      <!-- 顶部操作栏 -->
      <template #header>
        <div class="card-header">
          <div class="left-panel">
            <span class="page-title">用户管理</span>
          </div>
          <div class="right-panel">
            <el-input
              v-model="searchKeyword"
              placeholder="搜索用户名或姓名"
              style="width: 200px; margin-right: 10px"
              clearable
              @clear="fetchData"
              @keyup.enter="fetchData"
            >
              <template #append>
                <el-button icon="Search" @click="fetchData" />
              </template>
            </el-input>
            <el-button type="primary" icon="Plus" @click="openDialog('add')">新增用户</el-button>
          </div>
        </div>
      </template>

      <!-- 用户列表表格 -->
      <el-table :data="tableData" border stripe v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="150" />
       <el-table-column prop="role" label="角色" width="120" align="center">
        <template #default="{ row }">
            <el-tag v-if="row.role === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else-if="row.role === 'manager'" type="warning">负责人</el-tag>
              <el-tag v-else-if="row.role === 'repairman'" type="primary">维修人员</el-tag>
            <el-tag v-else type="success">用户</el-tag>
        </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column label="操作" min-width="250" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" icon="Edit" @click="openDialog('edit', row)">编辑</el-button>
            <el-button link type="warning" icon="Key" @click="handleResetPwd(row)">重置密码</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑 用户弹窗 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="dialogType === 'add' ? '新增用户' : '编辑用户'" 
      width="450px"
      @close="resetForm"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <!-- 用户名：编辑时通常不允许修改 -->
        <el-form-item label="用户名" prop="username">
          <el-input 
            v-model="form.username" 
            placeholder="请输入登录账号" 
            :disabled="dialogType === 'edit'" 
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="请输入姓名" />
        </el-form-item>

        <!-- 密码：只有新增时显示 -->
        <el-form-item label="密码" prop="password" v-if="dialogType === 'add'">
          <el-input 
            v-model="form.password" 
            type="password" 
            placeholder="请输入初始密码" 
            show-password 
          />
        </el-form-item>

        <el-form-item label="角色" prop="role">
        <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <!-- value 必须对应数据库里的 enum 值 -->
            <el-option label="学生" value="user" />
            <el-option label="实验室负责人" value="manager" />
            <el-option label="系统管理员" value="admin" />
              <el-option label="维修员" value="repairman" />
        </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm" :loading="submitLoading">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserList, addUser, updateUser, deleteUser, resetPassword } from '@/api/lab'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 数据定义 ---
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')

// 弹窗相关
const dialogVisible = ref(false)
const dialogType = ref('add') // 'add' 或 'edit'
const formRef = ref(null)

const form = reactive({
  id: null,
  username: '',
  realName: '',
  password: '',
  role: 'user'
})

// 表单校验规则
const rules = reactive({
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  realName: [{ required: true, message: '请输入真实姓名', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  // 动态校验：只有新增时密码必填
  password: [{ 
    validator: (rule, value, callback) => {
      if (dialogType.value === 'add' && !value) {
        callback(new Error('请输入密码'))
      } else if (dialogType.value === 'add' && value.length < 6) {
        callback(new Error('密码至少6位'))
      } else {
        callback()
      }
    }, 
    trigger: 'blur' 
  }]
})

// --- 方法定义 ---

// 1. 获取列表
const fetchData = async () => {
  loading.value = true
  // === 修改开始：加上 try...catch ===
  try {
    const res = await getUserList(searchKeyword.value)
    tableData.value = res 
  } catch (error) {
    console.error("获取用户列表失败:", error)
    // 这里捕获错误，界面就不会弹出红色的 AxiosError 遮罩层了
  } finally {
    loading.value = false
  }
  // === 修改结束 ===
}
// 2. 打开弹窗 (新增或编辑)
const openDialog = (type, row = null) => {
  dialogType.value = type
  dialogVisible.value = true
  
  if (type === 'edit' && row) {
    // 回显数据
    form.id = row.id
    form.username = row.username
    form.realName = row.realName
    form.role = row.role
    form.password = '' // 编辑不传密码
  } else {
    // 新增模式，重置数据
    resetForm()
  }
}

// 3. 重置表单
const resetForm = () => {
  if (formRef.value) formRef.value.clearValidate()
  form.id = null
  form.username = ''
  form.realName = ''
  form.password = ''
  form.role = 'user'
}

// 4. 提交表单 (新增/修改)
const submitForm = async () => {
  if (!formRef.value) return
  await formRef.value.validate(async (valid) => {
    if (valid) {
      submitLoading.value = true
      try {
        if (dialogType.value === 'add') {
          // 新增逻辑：UserAddDTO
          await addUser(form)
          ElMessage.success('添加成功')
        } else {
          // 修改逻辑：UserUpdateDTO
          await updateUser({
            id: form.id,
            realName: form.realName,
            role: form.role
          })
          ElMessage.success('修改成功')
        }
        dialogVisible.value = false
        fetchData() // 刷新列表
      } finally {
        submitLoading.value = false
      }
    }
  })
}

// 5. 删除用户
const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除用户 "${row.username}" 吗？此操作不可恢复！`, '警告', {
    confirmButtonText: '确定删除',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchData()
  }).catch(() => {})
}

// 6. 重置密码
const handleResetPwd = (row) => {
  ElMessageBox.confirm(`确定要重置用户 "${row.realName}" 的密码吗？`, '提示', {
    confirmButtonText: '确定重置',
    cancelButtonText: '取消',
    type: 'info'
  }).then(async () => {
    await resetPassword(row.id)
    ElMessage.success('密码已重置为 123456')
  }).catch(() => {})
}

// 初始化加载
onMounted(() => {
  fetchData()
})
</script>

<style scoped>
.app-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.page-title {
  font-size: 18px;
  font-weight: bold;
}
.right-panel {
  display: flex;
  align-items: center;
}
</style>