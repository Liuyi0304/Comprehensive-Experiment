<template>
  <div class="app-container">
    <!-- 1. 顶部统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="stat-card bg-blue">
          <div class="icon-box"><el-icon><Tickets /></el-icon></div>
          <div class="info">
            <div class="label">申请总数</div>
            <div class="value">{{ tableData.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-orange">
          <div class="icon-box"><el-icon><Timer /></el-icon></div>
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
            <div class="label">已通过</div>
            <div class="value">{{ stats.approved }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-purple">
          <div class="icon-box"><el-icon><Money /></el-icon></div>
          <div class="info">
            <div class="label">申请总额 (元)</div>
            <div class="value">¥ {{ stats.totalAmount }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <!-- 2. 工具栏 -->
      <div class="toolbar">
        <div class="left">
          <h3 class="title">采购申请明细</h3>
        </div>
        <div class="right">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索设备名称" 
            prefix-icon="Search"
            clearable
            style="width: 220px"
          />
          <el-radio-group v-model="filterStatus" size="default">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">待审批</el-radio-button>
            <el-radio-button label="approved">已通过</el-radio-button>
            <el-radio-button label="rejected">已驳回</el-radio-button>
          </el-radio-group>
          <el-button icon="Refresh" circle @click="loadData" title="刷新" />
        </div>
      </div>

      <!-- 3. 表格 -->
      <el-table 
        :data="filteredData" 
        v-loading="loading" 
        style="width: 100%"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
      >
        <!-- 设备详情列：合并显示名称、型号、厂商 -->
        <el-table-column label="设备详情" min-width="220">
          <template #default="{ row }">
            <div class="device-info">
              <div class="name">{{ row.deviceName }}</div>
              <div class="sub-info">
                <el-tag size="small" type="info" effect="plain">{{ row.model || '无型号' }}</el-tag>
                <span class="divider">|</span>
                <span>{{ row.manufacturer || '未知厂商' }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 价格与数量 -->
        <el-table-column label="预算" width="180">
          <template #default="{ row }">
            <div class="price-info">
              <div class="unit-price">单价: <span class="red">¥{{ row.onePrice }}</span></div>
              <div class="total-price">
                数量: <strong>{{ row.number }}</strong> 
                <span class="gray">(总计: ¥{{ (row.onePrice * row.number).toFixed(2) }})</span>
              </div>
            </div>
          </template>
        </el-table-column>

        <!-- 申请时间 -->
        <el-table-column prop="createdAt" label="申请时间" width="160" sortable>
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <!-- 状态 -->
        <el-table-column prop="status" label="状态" width="120" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status">
              <span class="dot"></span>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <!-- 审批反馈 -->
        <el-table-column label="审批反馈" min-width="150">
          <template #default="{ row }">
            <span v-if="row.status === 'rejected'" class="text-danger">
              <el-icon style="vertical-align: -2px"><Warning /></el-icon> 
              {{ row.rejectedReason || '未填写驳回理由' }}
            </span>
            <span v-else-if="row.status === 'approved'" class="text-success">
              <el-icon style="vertical-align: -2px"><CircleCheck /></el-icon> 通过
            </span>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getMyPurchaseRequests } from '@/api/purchase'
import dayjs from 'dayjs'
import { Tickets, Timer, CircleCheck, Money, Search, Warning } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const filterStatus = ref('all')

// 统计数据
const stats = computed(() => {
  const list = tableData.value
  const totalAmount = list.reduce((sum, item) => sum + (item.onePrice * item.number), 0)
  return {
    pending: list.filter(i => i.status === 'pending').length,
    approved: list.filter(i => i.status === 'approved').length,
    totalAmount: totalAmount.toLocaleString()
  }
})

// 过滤数据
const filteredData = computed(() => {
  let data = tableData.value
  
  // 状态筛选
  if (filterStatus.value !== 'all') {
    data = data.filter(item => item.status === filterStatus.value)
  }
  
  // 搜索筛选
  if (searchKeyword.value) {
    const lower = searchKeyword.value.toLowerCase()
    data = data.filter(item => 
      item.deviceName.toLowerCase().includes(lower)
    )
  }
  return data
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await getMyPurchaseRequests()
    tableData.value = res.data || []
  } catch (error) {
    console.error("获取申请记录失败", error)
    tableData.value = []
  } finally {
    loading.value = false
  }
}

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
const getStatusText = (s) => ({ pending: '待审批', approved: '已通过', rejected: '已驳回' }[s] || s)

onMounted(() => {
  loadData()
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
  font-size: 13px;
  .unit-price { margin-bottom: 4px; }
  .red { color: #F56C6C; font-weight: bold; font-family: 'Consolas', monospace; }
  .gray { color: #909399; font-size: 12px; margin-left: 5px; }
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

.text-danger { color: #F56C6C; }
.text-success { color: #67C23A; }
.text-gray { color: #ccc; }
</style>