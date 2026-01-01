<template>
  <div class="app-container">
    <!-- 1. 顶部统计卡片 -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="8">
        <div class="stat-card bg-blue">
          <div class="icon-box"><el-icon><Document /></el-icon></div>
          <div class="info">
            <div class="label">申请总数</div>
            <div class="value">{{ tableData.length }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card bg-orange">
          <div class="icon-box"><el-icon><Timer /></el-icon></div>
          <div class="info">
            <div class="label">审核中</div>
            <div class="value">{{ stats.pending }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="8">
        <div class="stat-card bg-red">
          <div class="icon-box"><el-icon><CircleClose /></el-icon></div>
          <div class="info">
            <div class="label">被驳回</div>
            <div class="value">{{ stats.rejected }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card shadow="never" class="main-card">
      <!-- 2. 工具栏：搜索与筛选 -->
      <div class="toolbar">
        <div class="left">
          <h3 class="title">申请明细</h3>
        </div>
        <div class="right">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索设备名称/编号" 
            prefix-icon="Search"
            clearable
            style="width: 240px"
          />
          <el-radio-group v-model="filterStatus" size="default">
            <el-radio-button label="all">全部</el-radio-button>
            <el-radio-button label="pending">审核中</el-radio-button>
            <el-radio-button label="rejected">已驳回</el-radio-button>
          </el-radio-group>
          <el-button icon="Refresh" circle @click="loadData" title="刷新数据" />
        </div>
      </div>

      <!-- 3. 美化后的表格 -->
      <el-table 
        v-loading="loading" 
        :data="filteredData" 
        style="width: 100%"
        :header-cell-style="{ background: '#f8f9fa', color: '#606266' }"
      >
        <!-- 设备信息：合并显示更紧凑 -->
        <el-table-column label="设备信息" min-width="200">
          <template #default="{ row }">
            <div class="device-info">
              <div class="name">{{ row.deviceName }}</div>
              <div class="code">
                <el-icon><PriceTag /></el-icon> {{ row.deviceAssetNumber }}
              </div>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="reason" label="报废理由" min-width="200" show-overflow-tooltip />

        <el-table-column prop="createdAt" label="申请时间" width="160" sortable>
          <template #default="{ row }">
            <span class="time-text">{{ formatTime(row.createdAt) }}</span>
          </template>
        </el-table-column>

        <!-- 状态：徽章风格 -->
        <el-table-column prop="status" label="当前状态" width="120" align="center">
          <template #default="{ row }">
            <div class="status-badge" :class="row.status">
              <span class="dot"></span>
              {{ getStatusText(row.status) }}
            </div>
          </template>
        </el-table-column>

        <!-- 审批反馈：优化显示 -->
        <el-table-column label="审批反馈" min-width="180">
          <template #default="{ row }">
            <div v-if="row.status === 'rejected'" class="feedback-box rejected">
              <el-icon><Warning /></el-icon>
              <span class="reason">驳回: {{ row.rejectedReason || '无理由' }}</span>
            </div>
            <div v-else-if="row.status === 'approved'" class="feedback-box approved">
              <el-icon><CircleCheck /></el-icon>
              <span>于 {{ formatTime(row.approvedAt) }} 通过</span>
            </div>
            <span v-else class="text-gray">-</span>
          </template>
        </el-table-column>

        <!-- 空状态插槽 -->
        <template #empty>
          <el-empty description="暂无申请记录" />
        </template>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import request from '@/utils/request'
import dayjs from 'dayjs'
import { Document, Timer, CircleClose, Search, PriceTag, Warning, CircleCheck } from '@element-plus/icons-vue'

const loading = ref(false)
const tableData = ref([])
const searchKeyword = ref('')
const filterStatus = ref('all')

// 计算统计数据
const stats = computed(() => {
  return {
    pending: tableData.value.filter(i => i.status === 'pending').length,
    rejected: tableData.value.filter(i => i.status === 'rejected').length
  }
})

// 前端过滤数据
const filteredData = computed(() => {
  let data = tableData.value
  
  // 1. 状态筛选
  if (filterStatus.value !== 'all') {
    data = data.filter(item => item.status === filterStatus.value)
  }
  
  // 2. 关键词搜索
  if (searchKeyword.value) {
    const lower = searchKeyword.value.toLowerCase()
    data = data.filter(item => 
      (item.deviceName && item.deviceName.toLowerCase().includes(lower)) ||
      (item.deviceAssetNumber && item.deviceAssetNumber.toLowerCase().includes(lower))
    )
  }
  return data
})

const loadData = async () => {
  loading.value = true
  try {
    const res = await request.get('/scrap-requests/my')
    tableData.value = res.data || []
  } catch (error) {
    console.error("加载失败", error)
  } finally {
    loading.value = false
  }
}

const formatTime = (t) => t ? dayjs(t).format('YYYY-MM-DD HH:mm') : '-'
const getStatusText = (s) => ({ pending: '审核中', approved: '已通过', rejected: '已驳回' }[s] || s)

onMounted(() => {
  loadData()
})
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  background-color: #f5f7fa; /* 浅灰背景，突出卡片 */
  min-height: 100vh;
}

.mb-4 { margin-bottom: 20px; }

/* 1. 统计卡片 */
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

  /* 配色方案 */
  &.bg-blue { .icon-box { background: #ecf5ff; color: #409eff; } }
  &.bg-orange { .icon-box { background: #fdf6ec; color: #e6a23c; } }
  &.bg-red { .icon-box { background: #fef0f0; color: #f56c6c; } }
}

/* 2. 主卡片与工具栏 */
.main-card {
  border-radius: 8px;
  border: none;
}

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

/* 3. 表格内容美化 */
.device-info {
  .name { font-weight: 600; color: #303133; margin-bottom: 4px; }
  .code { 
    font-size: 12px; 
    color: #909399; 
    font-family: 'Consolas', monospace; /* 等宽字体显示编号 */
    display: flex;
    align-items: center;
    gap: 4px;
  }
}

.time-text {
  color: #606266;
  font-size: 13px;
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

/* 反馈信息框 */
.feedback-box {
  display: flex;
  align-items: center;
  gap: 5px;
  font-size: 13px;
  
  &.rejected { color: #f56c6c; }
  &.approved { color: #67c23a; }
}

.text-gray { color: #ccc; }
</style>