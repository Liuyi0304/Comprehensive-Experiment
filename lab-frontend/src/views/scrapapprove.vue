<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-header">
          <div class="header-left">
            <span class="title">报废申请审批</span>
            <!-- 简单的状态筛选 -->
            <el-radio-group v-model="filterStatus" size="small" @change="loadData" style="margin-left: 20px;">
              <el-radio-button label="">全部</el-radio-button>
              <el-radio-button label="pending">待审批</el-radio-button>
            </el-radio-group>
          </div>
          <el-button type="primary" :icon="Refresh" circle @click="loadData" />
        </div>
      </template>

      <el-table 
        v-loading="loading" 
        :data="tableData" 
        border 
        stripe 
        style="width: 100%"
        highlight-current-row
      >
        <!-- 序号 -->
        <el-table-column type="index" label="#" width="50" align="center" />

        <!-- 申请人信息 (管理员需要知道是谁申请的) -->
        <el-table-column prop="applicantName" label="申请人" width="100" show-overflow-tooltip />

        <!-- 设备信息 -->
        <el-table-column prop="deviceName" label="设备名称" min-width="140" show-overflow-tooltip />
        <el-table-column prop="deviceAssetNumber" label="资产编号" width="130" />

        <!-- 报废理由 -->
        <el-table-column prop="reason" label="报废理由" min-width="200" show-overflow-tooltip />

        <!-- 申请时间 -->
        <el-table-column prop="createdAt" label="申请时间" width="160" align="center">
          <template #default="{ row }">
            {{ formatTime(row.createdAt) }}
          </template>
        </el-table-column>

        <!-- 状态展示 -->
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="light">
              {{ getStatusText(row.status) }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作列 (仅管理员可见) -->
        <el-table-column label="操作" width="150" align="center" fixed="right">
          <template #default="{ row }">
            <div v-if="row.status === 'pending'">
              <el-button 
                type="success" 
                link 
                size="small" 
                @click="handleApprove(row)"
              >
                通过
              </el-button>
              <el-button 
                type="danger" 
                link 
                size="small" 
                @click="openRejectDialog(row)"
              >
                驳回
              </el-button>
            </div>
            <span v-else class="text-gray">已归档</span>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页组件 (可选，如果数据多的话) -->
      <!-- <div class="pagination-container"> ... </div> -->
    </el-card>

    <!-- 驳回理由弹窗 -->
    <el-dialog
      v-model="rejectDialog.visible"
      title="驳回申请"
      width="400px"
      destroy-on-close
    >
      <el-form :model="rejectDialog" label-position="top">
        <el-form-item label="请输入驳回理由：">
          <el-input 
            v-model="rejectDialog.reason" 
            type="textarea" 
            rows="3" 
            placeholder="例如：设备尚可维修，不符合报废标准"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="rejectDialog.visible = false">取消</el-button>
          <el-button type="primary" @click="confirmReject">确认驳回</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
import { getAllScrapRequests, approveScrapRequest } from '@/api/scrap' // 确保引入了API
import { ElMessage, ElMessageBox } from 'element-plus'
import { Refresh } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

// --- 状态定义 ---
const loading = ref(false)
const tableData = ref([])
const filterStatus = ref('pending') // 默认看待审批

// 驳回弹窗状态
const rejectDialog = reactive({
  visible: false,
  id: null,
  reason: ''
})

// --- 数据加载 ---
const loadData = async () => {
  loading.value = true
  try {
    // 假设后端支持传 status 参数筛选，如果不支持可以去掉 params
    const params = filterStatus.value ? { status: filterStatus.value } : {}
    const res = await getAllScrapRequests(params)
    tableData.value = res.data || []
  } catch (error) {
    console.error('加载列表失败', error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

// --- 业务逻辑：通过 ---
// --- 业务逻辑：通过 ---
const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确认同意设备 "${row.deviceName}" 的报废申请吗？`,
    '审批通过',
    {
      confirmButtonText: '确认通过',
      cancelButtonText: '取消',
      type: 'warning'
    }
  ).then(async () => {
    try {
      // 👇👇👇 修正点：后端DTO要的是 requestId，不是 id 👇👇👇
      await approveScrapRequest({ 
        requestId: row.id,   // 修改这里
        status: 'approved' 
      })
      ElMessage.success('审批成功，已通过')
      loadData() 
    } catch (error) {
      // error handled
    }
  }).catch(() => {})
}

// --- 业务逻辑：打开驳回弹窗 ---
const openRejectDialog = (row) => {
  rejectDialog.id = row.id
  rejectDialog.reason = ''
  rejectDialog.visible = true
}

// --- 业务逻辑：确认驳回 ---
// --- 业务逻辑：确认驳回 ---
const confirmReject = async () => {
  if (!rejectDialog.reason.trim()) {
    ElMessage.warning('请填写驳回理由')
    return
  }
  
  try {
    // 👇👇👇 修正点：后端DTO要的是 requestId，不是 id 👇👇👇
    await approveScrapRequest({ 
      requestId: rejectDialog.id, // 修改这里
      status: 'rejected',
      rejectedReason: rejectDialog.reason 
    })
    ElMessage.success('已驳回该申请')
    rejectDialog.visible = false
    loadData()
  } catch (error) {
    console.error(error)
  }
}

// --- 工具函数 ---
const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'

const getStatusType = (status) => {
  const map = {
    pending: 'warning',
    approved: 'success',
    rejected: 'danger'
  }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = {
    pending: '待审批',
    approved: '已通过',
    rejected: '已驳回'
  }
  return map[status] || '未知'
}

// 初始化
onMounted(() => {
  loadData()
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

.header-left {
  display: flex;
  align-items: center;
  gap: 20px;
}

.title {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.text-gray {
  color: #909399;
  font-size: 13px;
}
</style>