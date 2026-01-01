<template>
  <div class="app-container" style="padding: 20px;">
    <!-- 顶部搜索 -->
    <el-card shadow="never" class="mb-20">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="请输入用户名或真实姓名" 
            clearable 
            @clear="fetchList" 
            @keyup.enter="fetchList"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" icon="Search" @click="fetchList">搜索</el-button>
        </el-col>
        <el-col :span="14" style="text-align: right;">
          <el-button type="primary" icon="Plus" @click="handleAdd">新建用户</el-button>
        </el-col>
      </el-row>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="用户名" width="150" />
        <el-table-column prop="realName" label="真实姓名" width="120" />
        
        <el-table-column prop="role" label="角色" width="140" align="center">
          <template #default="scope">
            <el-tag v-if="scope.row.role === 'manager'" type="success">实验室负责人</el-tag>
            <el-tag v-else-if="scope.row.role === 'admin'" type="danger">管理员</el-tag>
            <el-tag v-else type="primary">普通用户</el-tag>
          </template>
        </el-table-column>

        <el-table-column prop="phone" label="手机号" width="150" />
        
        <!-- 🔴 修改点 1：表格显示实验室名称，而不是 ID -->
        <el-table-column label="所属实验室" min-width="180" show-overflow-tooltip>
             <template #default="{ row }">
                {{ getLabName(row.labId) }}
             </template>
        </el-table-column>
        
        <el-table-column label="操作" width="260" align="center" fixed="right">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="warning" icon="Key" @click="handleResetPwd(scope.row)">重置密码</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗组件 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="500px" 
      @close="resetForm"
      destroy-on-close
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" :disabled="!!form.id" placeholder="登录账号" />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input v-model="form.realName" placeholder="如：张三" />
        </el-form-item>
        
        <el-form-item v-if="!form.id" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="留空则默认为 123456" show-password />
        </el-form-item>

        <el-form-item label="角色" prop="role">
          <el-select v-model="form.role" placeholder="请选择角色" style="width: 100%">
            <el-option label="普通用户 (User)" value="user" />
            <el-option label="实验室负责人 (Manager)" value="manager" />
            <el-option label="系统管理员 (Admin)" value="admin" />
          </el-select>
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" maxlength="11" />
        </el-form-item>

        <!-- 🔴 修改点 2：弹窗使用下拉框选择实验室 -->
        <el-form-item label="所属实验室" prop="labId">
          <el-select 
            v-model="form.labId" 
            placeholder="请选择实验室" 
            style="width: 100%" 
            clearable
            filterable
          >
            <el-option 
              v-for="lab in labList" 
              :key="lab.id" 
              :label="lab.name" 
              :value="lab.id" 
            />
          </el-select>
        </el-form-item>

      </el-form>
      <template #footer>
        <el-button @click="dialog.visible = false">取 消</el-button>
        <el-button type="primary" @click="submitForm">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
// 引入 getLabList
import { getUserList, addUser, updateUser, deleteUser, resetPassword } from '@/api/user'
import { getLabList } from '@/api/lab' 
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Key, Delete } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const labList = ref([]) // 存储实验室字典
const formRef = ref(null)

const queryParams = reactive({
  keyword: ''
})

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  username: '',
  password: '', 
  realName: '',
  role: 'user', 
  phone: '',
  labId: undefined
})

const rules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入真实姓名', trigger: 'blur' }
  ],
  role: [
    { required: true, message: '请选择角色', trigger: 'change' }
  ]
}

onMounted(async () => {
  await fetchLabs() // 先加载字典
  fetchList()
})

// --- 方法 ---

// 1. 获取实验室字典
const fetchLabs = async () => {
  try {
    const res = await getLabList()
    labList.value = res.data || []
  } catch (e) {
    console.warn('加载实验室列表失败', e)
  }
}

// 辅助函数：ID 转 Name
const getLabName = (id) => {
  if (!id) return '-'
  const lab = labList.value.find(l => l.id === id)
  return lab ? lab.name : `未知实验室(ID:${id})`
}

// 2. 获取用户列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUserList(queryParams.keyword)
    tableData.value = res.data || []
  } catch (error) {
    console.error('获取用户列表失败', error)
  } finally {
    loading.value = false
  }
}

const handleAdd = () => {
  resetForm()
  dialog.title = '新增用户'
  dialog.visible = true
}

const handleEdit = (row) => {
  resetForm()
  dialog.title = '编辑用户'
  Object.assign(form, row)
  form.password = ''
  dialog.visible = true
}

const handleResetPwd = (row) => {
  ElMessageBox.confirm(
    `确认将用户 "${row.realName}" 的密码重置为 123456 吗？`,
    '高危操作警告',
    { confirmButtonText: '确定重置', cancelButtonText: '取消', type: 'warning' }
  ).then(async () => {
    await resetPassword(row.id)
    ElMessage.success('密码已重置成功')
  }).catch(() => {})
}

const submitForm = () => {
  if (!formRef.value) return
  
  formRef.value.validate(async (valid) => {
    if (valid) {
      try {
        if (form.id) {
          await updateUser(form) // 传整个 form 即可，后端 DTO 会匹配字段
          ElMessage.success('用户修改成功')
        } else {
          await addUser(form)
          ElMessage.success('用户创建成功')
        }
        dialog.visible = false
        fetchList()
      } catch (e) {
        console.error(e)
      }
    }
  })
}

const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确认永久删除用户 "${row.username}" 吗？`,
    '删除警告',
    { confirmButtonText: '删除', cancelButtonText: '取消', type: 'error' }
  ).then(async () => {
    await deleteUser(row.id)
    ElMessage.success('删除成功')
    fetchList()
  }).catch(() => {})
}

const resetForm = () => {
  form.id = undefined
  form.username = ''
  form.password = ''
  form.realName = ''
  form.role = 'user'
  form.phone = ''
  form.labId = undefined
  if (formRef.value) formRef.value.clearValidate()
}
</script>