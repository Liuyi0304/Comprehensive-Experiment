<template>
  <div class="app-container">
    <!-- 1. 顶部统计仪表盘 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="stat-card bg-blue">
          <div class="icon-box"><el-icon><Files /></el-icon></div>
          <div class="info">
            <div class="label">申请总数</div>
            <div class="value">{{ tableData.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-orange">
          <div class="icon-box"><el-icon><Bell /></el-icon></div>
          <div class="info">
            <div class="label">待审批</div>
            <div class="value">{{ stats.pending }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-green">
          <div class="icon-box"><el-icon><CircleCheck /></el-icon></div>
          <div class="info">
            <div class="label">本月已通过</div>
            <div class="value">{{ stats.approved }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-purple">
          <div class="icon-box"><el-icon><Money /></el-icon></div>
          <div class="info">
            <div class="label">待审金额 (元)</div>
            <div class="value">¥ {{ stats.pendingAmount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <!-- 2. 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <h3 class="title">采购审批台</h3>
        </div>
        <div class="right">
          <!-- 搜索框 -->
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索申请人 / 设备名称" 
            prefix-icon="Search"
            clearable
            style="width: 240px"
          />
          
          <!-- 状态筛选 -->
          <el-radio-group v-model="filterStatus" size="default">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待办</el-radio-button>

            <el-radio-button label="approved">已通过</el-radio-button>
            <el-radio-button label="rejected">已驳回</el-radio-button>
          </el-radio-group>

          <el-button icon="Refresh" circle @click="fetchData" title="刷新数据" />
        </div>
      </div>

      <!-- 3. 数据表格 -->
      <el-table 
        :data="filteredData" 
        v-loading="loading" 
        style="width: 100%"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
      >
        <!-- 设备详情：聚合显示 -->
        <el-table-column label="采购设备详情" min-width="220">
          <template #default="{ row }">
            <div class="device-info">
              <div class="name">{{ row.deviceName }}</div>
              <div class="sub-info">
                <el-tag size="small" type="info" effect="plain">{{ row.model || '标准型号' }}</el-tag>
                <span class="divider">|</span>
                <span>{{ row.manufacturer || '通用厂商' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 预算信息 -->
        <el-table-column label="预算核算" width="180">
          <template #default="{ row }">
            <div class="price-info">
              <div class="total">
                <span class="currency">¥</span>
                <span class="amount">{{ (row.number * row.onePrice).toLocaleString() }}</span>
              </div>
              <div class="detail">
                {{ row.number }} 台 × ¥{{ row.onePrice }}
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 申请理由 -->
        <el-table-column prop="reason" label="申请理由" min-width="200" show-overflow-tooltip />

        <!-- 申请时间 -->
        <el-table-column prop="createdAt" label="申请时间" width="160" sortable>
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column label="状态" width="120" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status">
              <span class="dot"></span>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <!-- 操作列 -->
        <el-table-column label="审批操作" width="140" fixed="right" align="center">
          <template #default="{ row }">
            <div v-if="row.status === 'pending'" class="action-buttons">
              <el-tooltip content="批准申请" placement="top">
                <el-button type="success" circle icon="Check" @click="handleApprove(row)" />
              </el-tooltip>
              <el-tooltip content="驳回申请" placement="top">
                <el-button type="danger" circle icon="Close" @click="handleReject(row)" />
              </el-tooltip>
            </div>
            <span v-else class="text-gray">已归档</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getPurchaseList, approvePurchase } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Files, Bell, CircleCheck, Money, Search, Check, Close, Timer } from '@element-plus/icons-vue'
import dayjs from 'dayjs'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const filterStatus = ref('all') 

// 统计数据计算
const stats = computed(() => {
  const list = tableData.value
  const pendingList = list.filter(i => i.status === 'pending')
  const pendingAmount = pendingList.reduce((sum, item) => sum + (item.number * item.onePrice), 0)
  
  return {
    pending: pendingList.length,
    approved: list.filter(i => i.status === 'approved').length,
    pendingAmount: pendingAmount.toLocaleString()
  }
})

// 前端过滤逻辑
const filteredData = computed(() => {
  let data = tableData.value
  
  // 1. 状态筛选
  if (filterStatus.value !== 'all') {
    data = data.filter(item => item.status === filterStatus.value)
  }
  
  // 2. 搜索筛选
  if (searchKeyword.value) {
    const lower = searchKeyword.value.toLowerCase()
    data = data.filter(item => 
      item.deviceName.toLowerCase().includes(lower) || 
      (item.reason && item.reason.toLowerCase().includes(lower))
    )
  }
  return data
})

const fetchData = async () => {
  loading.value = true
  try {
    // 获取全部数据，前端做筛选和统计
    const res = await getPurchaseList({}) 
    tableData.value = res.data || []
  } catch (e) {
    console.error(e)
  } finally {
    loading.value = false
  }
}

// 审批通过
const handleApprove = (row) => {
  ElMessageBox.confirm(
    `确认批准采购【${row.deviceName}】吗？\n总金额：¥${(row.number * row.onePrice).toLocaleString()}`, 
    '审批确认', 
    { confirmButtonText: '批准', cancelButtonText: '取消', type: 'success' }
  ).then(() => submitAudit(row.id, 'approved'))
}

// 审批驳回
const handleReject = (row) => {
  ElMessageBox.prompt('请输入驳回理由', '驳回确认', {
    confirmButtonText: '确认驳回',
    cancelButtonText: '取消',
    inputPattern: /\S/,
    inputErrorMessage: '理由不能为空'
  }).then(({ value }) => {
    // 这里假设后端接口支持传 rejectReason，如果不支持，就只传 status
    submitAudit(row.id, 'rejected', value)
  })
}

const submitAudit = async (id, status, reason = null) => {
  const loadingInstance = ElMessage.info('正在处理...')
  try {
    // 构造参数，根据你后端的实际 DTO 调整
    const params = { id, status }
    if (reason) params.rejectReason = reason
    
    await approvePurchase(params)
    ElMessage.success('操作成功')
    fetchData()
  } catch (err) {
    console.error(err)
  } finally {
    loadingInstance.close()
  }
}

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
const getStatusText = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s)

onMounted(() => {
  fetchData()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.mb-4 { margin-bottom: 20px; }

/* 统计卡片 */
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  transition: transform 0.3s;
  
  &:hover { transform: translateY(-3px); }

  .icon-box {
    width: 48px;
    height: 48px;
    border-radius: 12px;
    display: flex;
    align-items: center;
    justify-content: center;
    font-size: 24px;
    margin-right: 15px;
  }

  .info {
    .label { font-size: 14px; color: #909399; margin-bottom: 4px; }
    .value { font-size: 24px; font-weight: bold; color: #303133; }
  }

  &.bg-blue { .icon-box { background: #ecf5ff; color: #409eff; } }
  &.bg-orange { .icon-box { background: #fdf6ec; color: #e6a23c; } }
  &.bg-green { .icon-box { background: #f0f9eb; color: #67c23a; } }
  &.bg-purple { .icon-box { background: #f4f4f5; color: #909399; } }
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  
  .title {
    font-size: 16px;
    font-weight: bold;
    color: #303133;
    border-left: 4px solid #409eff;
    padding-left: 10px;
    margin: 0;
  }
  
  .right {
    display: flex;
    gap: 12px;
    align-items: center;
  }
}

/* 表格内容样式 */
.device-info {
  .name { font-weight: 600; color: #303133; margin-bottom: 6px; font-size: 15px; }
  .sub-info { 
    font-size: 12px; color: #909399; display: flex; align-items: center; gap: 8px;
    .divider { color: #e4e7ed; }
  }
}

.price-info {
  .total { 
    color: #F56C6C; font-weight: bold; font-size: 16px; 
    .currency { font-size: 12px; margin-right: 2px; }
  }
  .detail { font-size: 12px; color: #909399; margin-top: 2px; }
}

.time-text {
  color: #606266;
  font-size: 13px;
  font-family: 'Consolas', monospace;
}

/* 状态徽章 */
.status-badge {
  display: inline-flex;
  align-items: center;
  padding: 4px 12px;
  border-radius: 15px;
  font-size: 12px;
  
  .dot {
    width: 6px;
    height: 6px;
    border-radius: 50%;
    margin-right: 6px;
  }

  &.pending { background: #fdf6ec; color: #e6a23c; .dot { background: #e6a23c; } }
  &.approved { background: #f0f9eb; color: #67c23a; .dot { background: #67c23a; } }
  &.rejected { background: #fef0f0; color: #f56c6c; .dot { background: #f56c6c; } }
}

.action-buttons {
  display: flex;
  justify-content: center;
  gap: 10px;
}

.text-gray { color: #ccc; font-size: 12px; }
</style>