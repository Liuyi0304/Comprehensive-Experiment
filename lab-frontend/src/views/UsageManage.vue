<template>
  <div class="app-container" style="padding: 24px; background-color: #f0f2f5; min-height: 100vh;">
    <!-- 1. 顶部统计 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="8">
        <div class="stat-card">
          <!-- 补全了图标背景和颜色 -->
          <div class="icon-box bg-blue">
            <el-icon><Monitor /></el-icon>
          </div>
          <div class="info">
            <div class="value">{{ activeUsageCount }}</div>
            <div class="label">当前正在使用中</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never">
      <div style="display:flex; justify-content: space-between; margin-bottom: 20px; align-items: center;">
        <h3 style="margin:0">设备领用登记</h3>
        <!-- 只有 User 角色可以看到登记按钮 -->
        <el-button 
          type="primary" 
          icon="Plus" 
          @click="openUsageDialog" 
          v-if="userRole === 'user'"
        >登记领用</el-button>
      </div>

      <!-- 2. 使用记录表格 -->
      <el-table :data="tableData" v-loading="loading" border stripe>
        <el-table-column label="设备信息" min-width="180">
          <template #default="{ row }">
            <div style="font-weight:bold; color: #409eff;">{{ row.deviceName }}</div>
            <!-- 对应你在 populateDetails 里塞进去的字段名 -->
            <div style="font-size:12px; color:#999">资产编号: {{ row.deviceAssetNumber }}</div>
          </template>
        </el-table-column>
        
        <el-table-column prop="userName" label="使用者" width="120" />
        
        <el-table-column prop="startTime" label="开始时间" width="170" sortable />
        
        <el-table-column prop="endTime" label="归还时间" width="170">
          <template #default="{ row }">
            <span v-if="row.endTime">{{ row.endTime }}</span>
            <el-tag v-else type="warning" effect="plain">使用中...</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="purpose" label="用途" show-overflow-tooltip />

        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <!-- 只有自己的领用单 且 还没归还时，显示归还按钮 -->
            <!-- 使用 == 兼容 String 和 Number 类型的 ID 比较 -->
            <el-button 
              v-if="!row.endTime && row.userId == currentUserId" 
              type="success" 
              size="small" 
              plain
              @click="handleReturn(row)"
            >归还设备</el-button>
            <span v-else-if="row.endTime" style="color:#ccc; font-size:12px">已完成</span>
            <span v-else style="color:#999; font-size:12px">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 3. 登记弹窗 -->
    <el-dialog title="申请领用设备" v-model="visible" width="450px" destroy-on-close>
      <el-form :model="form" label-position="top">
        <el-form-item label="选择本实验室在库设备" required>
          <el-select v-model="form.deviceId" style="width:100%" filterable placeholder="请选择设备">
            <el-option 
              v-for="d in availableDevices" 
              :key="d.id" 
              :label="`${d.name} [${d.assetNumber}]`" 
              :value="d.id" 
            />
          </el-select>
          <div v-if="availableDevices.length === 0" style="font-size:12px; color:#f56c6c; margin-top:5px;">
            暂无可领用设备（均在维修或使用中）
          </div>
        </el-form-item>
        <el-form-item label="使用用途" required>
          <el-input 
            v-model="form.purpose" 
            type="textarea" 
            rows="3" 
            placeholder="请详细描述领用用途（如：XX实验课使用）" 
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible = false">取消</el-button>
        <el-button type="primary" @click="submit" :loading="submitLoading">确认领用</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getUsageList, startUsage, endUsage } from '@/api/usage' 
import { getDeviceList } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'
// 🔴 必须导入图标，否则页面显示空白
import { Monitor, Plus } from '@element-plus/icons-vue'

// --- 状态定义 ---
const userRole = ref(localStorage.getItem('role'))
const currentUserId = ref(localStorage.getItem('userId'))
const loading = ref(false)
const submitLoading = ref(false)
const tableData = ref([])
const deviceList = ref([])
const visible = ref(false)

const form = reactive({ deviceId: null, purpose: '' })

// 统计数量
const activeUsageCount = computed(() => tableData.value.filter(i => !i.endTime).length)

// 过滤可用设备
const availableDevices = computed(() => {
  return deviceList.value.filter(d => d.status === 'in_stock' || d.status === 'available')
})

// --- 方法 ---

const fetchList = async () => {
  loading.value = true
  try {
    const res = await getUsageList()
    tableData.value = res.data || []
  } finally {
    loading.value = false
  }
}

const openUsageDialog = async () => {
  // 这里可以根据需要传入 labId
  const res = await getDeviceList({ labId: localStorage.getItem('labId') })
  deviceList.value = res.data || []
  form.deviceId = null
  form.purpose = ''
  visible.value = true
}

const submit = async () => {
  if(!form.deviceId || !form.purpose) return ElMessage.warning('请填写完整领用信息')
  
  submitLoading.value = true
  try {
    await startUsage(form)
    ElMessage.success('设备领用成功，请爱惜使用')
    visible.value = false
    fetchList()
  } finally {
    submitLoading.value = false
  }
}

const handleReturn = (row) => {
  ElMessageBox.confirm(`确认归还设备 [${row.deviceName}] 吗？`, '提示', {
    type: 'success',
    confirmButtonText: '确认归还'
  }).then(async () => {
    await endUsage(row.id)
    ElMessage.success('设备归还成功，已入库')
    fetchList()
  })
}

onMounted(() => {
  fetchList()
})
</script>

<style scoped>
.mb-20 { margin-bottom: 20px; }
.stat-card {
  background: #fff; padding: 20px; border-radius: 8px; display: flex; align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
}
.icon-box { width: 54px; height: 54px; border-radius: 12px; display: flex; align-items: center; justify-content: center; font-size: 24px; color: #fff; margin-right: 15px; }
.bg-blue { background: linear-gradient(135deg, #6e8efb, #409eff); }
.info .value { font-size: 24px; font-weight: bold; color: #303133; }
.info .label { font-size: 13px; color: #909399; margin-top: 4px; }
</style>