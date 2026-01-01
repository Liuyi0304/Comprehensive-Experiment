<template>
  <div class="app-container" style="padding: 20px;">
    <!-- 顶部操作栏 -->
    <el-card shadow="never" class="mb-20">
      <el-button type="primary" icon="Plus" @click="handleAdd">新增分类</el-button>
    </el-card>

    <!-- 表格区域 -->
    <el-card shadow="never" style="margin-top: 20px;">
      <el-table 
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe
        style="width: 100%"
      >
        <el-table-column prop="id" label="ID" width="100" align="center" />
        <el-table-column prop="name" label="分类名称" />
        
        <el-table-column label="操作" width="200" align="center">
          <template #default="scope">
            <el-button link type="primary" icon="Edit" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button link type="danger" icon="Delete" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 弹窗组件 -->
    <el-dialog 
      :title="dialog.title" 
      v-model="dialog.visible" 
      width="400px" 
      @close="resetForm"
    >
      <el-form ref="formRef" :model="form" :rules="rules" label-width="80px">
        <el-form-item label="名称" prop="name">
          <el-input v-model="form.name" placeholder="请输入分类名称（如：显微镜类）" />
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
import { getCategoryList, addCategory, updateCategory, deleteCategory } from '@/api/category'
import { ElMessage, ElMessageBox } from 'element-plus'

// --- 数据定义 ---
const loading = ref(false)
const tableData = ref([])
const formRef = ref(null)

const dialog = reactive({
  visible: false,
  title: ''
})

const form = reactive({
  id: undefined,
  name: ''
})

// 表单校验规则
const rules = {
  name: [{ required: true, message: '分类名称不能为空', trigger: 'blur' }]
}

// --- 初始化 ---
onMounted(() => {
  fetchList()
})

// --- 方法 ---

// 获取列表
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getCategoryList()
    // 兼容写法，防止后端直接返回 List 或者 Result
    tableData.value = Array.isArray(res) ? res : (res.data || [])
  } catch (error) {
    console.error(error)
  } finally {
    loading.value = false
  }
}

// 打开新增
const handleAdd = () => {
  resetForm()
  dialog.title = '新增分类'
  dialog.visible = true
}

// 打开编辑
const handleEdit = (row) => {
  resetForm()
  dialog.title = '编辑分类'
  // 回显数据
  form.id = row.id
  form.name = row.name
  dialog.visible = true
}

// 提交表单
const submitForm = () => {
  formRef.value.validate(async (valid) => {
    if (valid) {
      if (form.id) {
        await updateCategory(form)
        ElMessage.success('修改成功')
      } else {
        await addCategory(form)
        ElMessage.success('添加成功')
      }
      dialog.visible = false
      fetchList() // 刷新列表
    }
  })
}

// 删除
const handleDelete = (row) => {
  ElMessageBox.confirm(`确认删除分类 "${row.name}" 吗？`, '警告', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    await deleteCategory(row.id)
    ElMessage.success('已删除')
    fetchList()
  }).catch(() => {})
}

// 重置表单
const resetForm = () => {
  form.id = undefined
  form.name = ''
  if(formRef.value) formRef.value.clearValidate()
}
</script>