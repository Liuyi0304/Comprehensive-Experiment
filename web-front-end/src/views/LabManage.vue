<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 18px;">实验室管理</span>
          <div>
            <el-button type="default" @click="$router.push('/dashboard')">返回设备列表</el-button>
            <el-button type="primary" icon="Plus" @click="openAddDialog">新增实验室</el-button>
          </div>
        </div>
      </template>

      <!-- 列表表格 -->
      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="实验室名称" />
        <el-table-column prop="location" label="位置" />
        <el-table-column prop="managerId" label="负责人ID" width="100" align="center">
          <template #default="{ row }">
            <!-- 这里简单展示ID，如果想展示名字，需要后端返回VO或前端遍历匹配 -->
            {{ row.managerId || '暂无' }}
          </template>
        </el-table-column>
        <el-table-column prop="createdTime" label="创建时间" width="180" />
      </el-table>
    </el-card>

    <!-- 新增弹窗 -->
    <el-dialog v-model="dialogVisible" title="新增实验室" width="500px">
      <el-form :model="form" label-width="100px">
        <el-form-item label="名称" required>
          <el-input v-model="form.name" placeholder="例如：化学实验室B" />
        </el-form-item>
        <el-form-item label="位置" required>
          <el-input v-model="form.location" placeholder="例如：实验楼-204" />
        </el-form-item>
        
        <!-- 负责人下拉框 (调用用户列表接口) -->
        <el-form-item label="负责人">
          <el-select v-model="form.managerId" placeholder="请选择负责人" style="width: 100%" clearable>
            <el-option 
              v-for="user in userList" 
              :key="user.id" 
              :label="user.realName" 
              :value="user.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitAdd" :loading="submitting">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getLabList, addLab, getUserList } from '@/api/lab' // 引入接口
import { ElMessage } from 'element-plus'

const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const tableData = ref([])
const userList = ref([]) // 用于存储可选的负责人列表

const form = reactive({
  name: '',
  location: '',
  managerId: null
})

// 1. 获取实验室列表
const fetchData = async () => {
  loading.value = true
  try {
    const res = await getLabList()
    tableData.value = res
  } finally {
    loading.value = false
  }
}

// 2. 获取用户列表 (为了下拉框选择负责人)
const fetchUsers = async () => {
  try {
    // 传空字符串获取所有用户
    const res = await getUserList('') 
    userList.value = res
  } catch (e) {}
}

// 3. 打开新增窗口
const openAddDialog = () => {
  form.name = ''
  form.location = ''
  form.managerId = null
  dialogVisible.value = true
  // 打开弹窗时，顺便加载一下用户列表
  if (userList.value.length === 0) {
    fetchUsers()
  }
}

// 4. 提交新增
const submitAdd = async () => {
  if (!form.name || !form.location) {
    return ElMessage.warning('请填写名称和位置')
  }
  
  submitting.value = true
  try {
    await addLab(form)
    ElMessage.success('实验室创建成功')
    dialogVisible.value = false
    fetchData() // 刷新列表
  } catch (e) {
    // request.js 会处理错误提示
  } finally {
    submitting.value = false
  }
}

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
</style>