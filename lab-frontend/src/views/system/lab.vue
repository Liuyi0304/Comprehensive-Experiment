<template>
  <div class="app-container" style="padding: 20px;">
    <!-- 顶部操作 -->
    <el-card shadow="never" class="mb-20">
      <el-button type="primary" icon="Plus" @click="handleAdd">新增实验室</el-button>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column prop="id" label="ID" width="80" align="center" />
        <el-table-column prop="name" label="实验室名称" min-width="150" />
        <el-table-column prop="location" label="位置" width="150" />
        
        <!-- 🔴 修改点1：展示关联查询出来的 managerRealName -->
        <el-table-column prop="managerRealName" label="负责人" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.managerRealName">{{ scope.row.managerRealName }}</el-tag>
            <span v-else class="text-gray-400">未设置</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createdTime" label="创建时间" width="180" />
        
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗 -->
    <el-dialog :title="dialog.title" v-model="dialog.visible" width="500px" @close="resetForm">
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入实验室名称" />
        </el-form-item>

        <!-- 🔴 修改点2：负责人改为下拉选择 -->
        <el-form-item label="负责人" prop="managerId">
          <el-select v-model="form.managerId" placeholder="请选择负责人" style="width: 100%">
            <el-option
              v-for="user in userOptions"
              :key="user.id"
              :label="user.realName + ' (' + user.username + ')'"
              :value="user.id"
            />
          </el-select>
        </el-form-item>

        <el-form-item label="位置" prop="location">
          <el-input v-model="form.location" placeholder="例如：综合楼301" />
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
import { getLabList, addLab, updateLab, deleteLab } from '@/api/lab'
import { getUserOptions } from '@/api/user' // 刚才新建的api
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const userOptions = ref([]) // 存储用户列表
const formRef = ref(null)

const dialog = reactive({ visible: false, title: '' })

// 🔴 修改点3：表单字段对应 DTO
const form = reactive({
  id: undefined,
  name: '',
  managerId: undefined, // 存 ID
  location: ''
})

const rules = {
  name: [{ required: true, message: '必填', trigger: 'blur' }],
  managerId: [{ required: true, message: '请选择负责人', trigger: 'change' }]
}

onMounted(() => {
  fetchList()
  fetchUserList() // 加载页面时获取用户列表
})

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getLabList()
    tableData.value = res.data
  } finally {
    loading.value = false
  }
}

const fetchUserList = async () => {
  try {
    const res = await getUserOptions()
    console.log("用户数据返回：", res) // 🔴 打开F12看看这里打印了什么

    // 兼容写法：如果拦截器处理过就是 res，没处理过可能是 res.data
    // 如果你发现打印出来是 Array(数组)，那就直接赋值
    if (Array.isArray(res)) {
       userOptions.value = res
    } else {
       // 否则尝试取 data
       userOptions.value = res.data || [] 
    }
  } catch (e) {
    console.error('获取用户列表失败', e)
  }
}

const handleAdd = () => {
  resetForm()
  dialog.title = '新增实验室'
  dialog.visible = true
}

const handleEdit = (row) => {
  resetForm()
  dialog.title = '编辑实验室'
  // 回显数据
  form.id = row.id
  form.name = row.name
  form.location = row.location
  form.managerId = row.managerId // 自动让下拉框选中
  dialog.visible = true
}

const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateLab(form)
        ElMessage.success('修改成功')
      } else {
        await addLab(form)
        ElMessage.success('添加成功')
      }
      dialog.visible = false
      fetchList()
    }
  })
}

/* 
   script setup 部分 
   找到删除方法，替换成下面这个 
*/



const handleDelete = (row) => {
  ElMessageBox.confirm(
    '确认删除该实验室吗？', 
    '危险操作', 
    {
      confirmButtonText: '确定删除',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 1. 发送删除请求
      await deleteLab(row.id)
      
      // 2. 如果没报错，说明删成功了
      ElMessage.success('删除成功')
      fetchList() // 刷新列表
      
    } catch (error) {
      // 3. 🔴 重点：如果后端拦截了，会跑进这里
      // 我们用一个更明显的弹窗告诉管理员原因
      ElMessageBox.alert(
        error.msg || '无法删除，可能存在关联数据', // 这里显示后端返回的那句 "还有 X 台设备..."
        '操作被拒绝',
        {
          confirmButtonText: '知道了',
          type: 'error',
          icon: 'CircleCloseFilled'
        }
      )
    }
  }).catch(() => {
    // 点击取消，什么都不做
  })
}

const resetForm = () => {
  form.id = undefined
  form.name = ''
  form.managerId = undefined
  form.location = ''
  if(formRef.value) formRef.value.clearValidate()
}
</script>