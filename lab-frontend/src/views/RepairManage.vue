<template>
  <div class="app-container" style="padding: 24px; background-color: #f0f2f5; min-height: 100vh;">
    <!-- 1. 顶部统计卡片 -->
    <el-row :gutter="20" class="mb-20">
      <el-col :span="6" v-for="item in statConfig" :key="item.label">
        <div class="stat-card-v2">
          <div class="icon-side" :class="item.colorClass">
            <el-icon><component :is="item.icon" /></el-icon>
          </div>
          <div class="content-side">
            <div class="stat-value">{{ stats[item.key] || 0 }}</div>
            <div class="stat-label">{{ item.label }}</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- 2. 工具栏 -->
    <el-card shadow="never" class="mb-20">
      <div style="display: flex; justify-content: space-between; align-items: center;">
        <div>
          <h3 style="margin: 0; display: inline-block; vertical-align: middle;">报修工单中心</h3>
          <el-tag size="small" type="info" style="margin-left: 12px;">
            {{ isAdmin ? '系统管理员' : `实验室 ${userLabId || '-'} 负责人` }}
          </el-tag>
        </div>
        <div style="display: flex; gap: 10px;">
          <el-input 
            v-model="queryParams.keyword" 
            placeholder="搜索设备名或资产编号" 
            clearable 
            prefix-icon="Search"
            style="width: 250px"
            @clear="fetchList" 
            @keyup.enter="fetchList"
          />
          <el-button type="danger" icon="Plus" @click="openReportDialog">申请报修</el-button>
          <el-button icon="Refresh" circle @click="fetchList" />
        </div>
      </div>
    </el-card>

    <!-- 3. 数据表格 -->
    <el-card shadow="never">
      <el-table 
        ref="repairTableRef"
        :data="tableData" 
        v-loading="loading" 
        border 
        stripe 
        row-key="id"
        @row-click="handleRowClick"
        highlight-current-row
      >
        <!-- 详情折叠列 -->
        <el-table-column type="expand">
          <template #default="{ row }">
            <div class="expand-detail-container">
              <el-descriptions title="工单全生命周期追溯" :column="3" border size="small">
                <el-descriptions-item label="设备型号">{{ deviceMap.get(row.deviceId)?.model || '标准' }}</el-descriptions-item>
                <el-descriptions-item label="购入时间">{{ deviceMap.get(row.deviceId)?.createdAt || '-' }}</el-descriptions-item>
                <el-descriptions-item label="单价">¥{{ deviceMap.get(row.deviceId)?.price || '0.00' }}</el-descriptions-item>
                <el-descriptions-item label="详细故障描述" :span="3">
                  <div class="desc-text-full">{{ row.description }}</div>
                </el-descriptions-item>
                <el-descriptions-item label="最终处理方案" :span="2">
                  <el-tag v-if="deviceMap.get(row.deviceId)?.status === 'scrapped'" type="danger" size="small" style="margin-right:8px">已报废</el-tag>
                  <span :class="{'text-bold': row.solution}">{{ row.solution || '暂无录入记录' }}</span>
                </el-descriptions-item>
                <el-descriptions-item label="维修支出">
                  <span class="price-text" v-if="row.cost">¥{{ row.cost.toFixed(2) }}</span>
                  <span v-else>--</span>
                </el-descriptions-item>
              </el-descriptions>
            </div>
          </template>
        </el-table-column>

        <el-table-column prop="id" label="单号" width="80" align="center" />
        
        <el-table-column label="报修设备" min-width="180">
          <template #default="{ row }">
            <div class="device-cell">
              <span class="device-name">{{ row.deviceName }}</span>
              <span class="device-code">{{ row.deviceAssetNumber }}</span>
            </div>
          </template>
        </el-table-column>

        <el-table-column label="报修信息 (点击查看详情)" min-width="220">
          <template #default="{ row }">
            <div class="info-cell-clickable">
              <div class="brief-desc">{{ row.description }}</div>
              <div class="meta-line">
                <span><el-icon><User /></el-icon> {{ row.reporterName }}</span>
                <el-divider direction="vertical" />
                <span><el-icon><Clock /></el-icon> {{ row.reportedTime }}</span>
              </div>
            </div>
          </template>
        </el-table-column>

       <el-table-column label="工单状态" width="160" align="center">
  <template #default="{ row }">
    <div class="status-cell-v2">
      <el-tag :type="getStatusType(row.status)" effect="dark" size="small" round>
        {{ getStatusText(row.status) }}
      </el-tag>
      
      <!-- 仅在维修完成状态下显示结果 -->
      <div v-if="row.status === 'completed'" class="result-indicator-v2">
        <!-- 检查设备状态 -->
        <template v-if="deviceMap.get(row.deviceId)?.status === 'scrapped'">
          <span class="tag-scrapped"><el-icon><CircleClose /></el-icon> 维修失败(已报废)</span>
        </template>
        <!-- 如果还在“维修中”且工单已完结，说明正在报废审批中 -->
        <template v-else-if="deviceMap.get(row.deviceId)?.status === 'under_repair'">
          <span class="tag-pending"><el-icon><Warning /></el-icon> 报废申请中</span>
        </template>
        <!-- 明确设备变回 in_stock 或 in_use 才显示修复成功 -->
        <template v-else-if="['in_stock', 'in_use'].includes(deviceMap.get(row.deviceId)?.status)">
          <span class="tag-success"><el-icon><CircleCheck /></el-icon> 修复成功</span>
        </template>
        <!-- 兜底显示 -->
        <template v-else>
          <span style="font-size: 10px; color: #999;">等待结果同步...</span>
        </template>
      </div>
    </div>
  </template>
</el-table-column>

        <el-table-column label="操作" width="180" align="center" fixed="right">
          <template #default="{ row }">
            <!-- Manager 专属操作流 -->
            <template v-if="userRole.toLowerCase() === 'manager'">
              <div v-if="row.status === 'reported'" class="btn-group">
                <el-button link type="primary" icon="Finished" @click.stop="handleAudit(row, 'approved')">安排</el-button>
                <el-button link type="danger" icon="Close" @click.stop="handleAudit(row, 'rejected')">驳回</el-button>
              </div>
              <el-button v-else-if="row.status === 'assigned'" type="primary" size="small" plain icon="VideoPlay" @click.stop="handleStartRepair(row)">开始维修</el-button>
              <el-button v-else-if="row.status === 'in_progress'" type="success" size="small" icon="Edit" @click.stop="openCompleteDialog(row)">录入结果</el-button>
              
              <!-- 结束态的操作列语义化展示 -->
              <div v-else class="status-final-text">
                <span v-if="row.status === 'rejected'" class="c-gray">已驳回申请</span>
                <span v-else-if="deviceMap.get(row.deviceId)?.status === 'scrapped'" class="c-red">已转报废</span>
                <span v-else class="c-green">已修复归档</span>
                <el-button link type="info" icon="InfoFilled" @click.stop="handleRowClick(row)" style="margin-left: 4px;">详情</el-button>
              </div>
            </template>

            <!-- 非 Manager 视图 -->
            <template v-else>
              <div class="user-view-status">
                <span v-if="['completed', 'rejected'].includes(row.status)">已办结</span>
                <span v-else><el-icon class="is-loading"><Loading /></el-icon> 待主管处理</span>
                <el-button link type="primary" @click.stop="handleRowClick(row)">详情</el-button>
              </div>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 报修弹窗 (略) - 保持现有逻辑 -->
    <!-- 录入结果弹窗 - 保持逻辑 -->

    <!-- [新增弹窗] 报废申请成功提示 -->
    <el-dialog title="系统提示" v-model="scrapNoticeVisible" width="400px" center>
      <div style="text-align: center;">
        <el-icon color="#E6A23C" size="48"><WarningFilled /></el-icon>
        <h4 style="margin: 15px 0 10px;">维修失败，已转报废流程</h4>
        <p style="color: #666; font-size: 14px;">该设备维修结果已保存，系统已自动向管理员提交了“报废申请”，请等待进一步审批。</p>
      </div>
      <template #footer>
        <el-button type="primary" @click="scrapNoticeVisible = false">我知道了</el-button>
      </template>
    </el-dialog>


    <!-- 4. 报修弹窗 -->
    <el-dialog title="新建报修单" v-model="visible.report" width="550px" destroy-on-close>
      <el-form :model="reportForm" ref="reportFormRef" :rules="reportRules" label-position="top">
      <el-form-item label="1. 选择故障设备" prop="deviceId">
        <el-select 
          v-model="reportForm.deviceId" 
          filterable 
          placeholder="输入名称或编号搜索..." 
          style="width:100%"
          no-data-text="暂无可报修设备（设备可能已在维修或不属于本实验室）"
        >
          <el-option 
            v-for="item in availableDevices" 
            :key="item.id" 
            :label="`${item.name} [${item.assetNumber}]`" 
            :value="item.id" 
          />
        </el-select>
      </el-form-item>
        <div v-if="selectedDevice" class="device-preview-card">
          <div style="color: #409eff; font-weight: bold; margin-bottom: 10px;"><el-icon><Monitor /></el-icon> 设备信息确认</div>
          <el-row :gutter="20">
            <el-col :span="12"><p>资产名：{{ selectedDevice.name }}</p><p>编号：{{ selectedDevice.assetNumber }}</p></el-col>
            <el-col :span="12"><p>型号：{{ selectedDevice.model || '-' }}</p><p>位置：{{ selectedDevice.location || '-' }}</p></el-col>
          </el-row>
        </div>
        <el-form-item label="2. 详细故障描述" prop="description">
          <el-input type="textarea" v-model="reportForm.description" rows="4" placeholder="请描述具体故障表现..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible.report = false">取消</el-button>
        <el-button type="primary" @click="handleSubmitReport">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- 5. 录入结果弹窗 -->
<el-dialog title="维修结果反馈" v-model="visible.complete" width="480px">
  <el-form :model="completeForm" label-position="top">
    <el-form-item label="最终处理结果" required>
      <el-radio-group v-model="completeForm.resultStatus">
        <el-radio label="in_stock">✅ 修复成功（准予回库）</el-radio>
        <el-radio label="scrapped">⚠️ 无法修复（转报废申请）</el-radio>
      </el-radio-group>
    </el-form-item>

    <el-form-item label="维修详情说明" required>
      <el-input 
        type="textarea" 
        v-model="completeForm.solution" 
        placeholder="请详细描述故障处理过程，如选择报废请注明原因" 
      />
    </el-form-item>
        <el-form-item label="产生费用 (元)">
          <el-input-number v-model="completeForm.cost" :min="0" style="width:100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible.complete = false">取消</el-button>
        <el-button type="success" @click="handleSubmitComplete">提交完成</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { getRepairList, reportRepair, auditRepair, startRepair, completeRepair } from '@/api/repair'
import { getDeviceList } from '@/api/device'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Search, Plus, Refresh, User, Clock, Monitor, Stamp, Tools, 
  Checked, CircleCheck, CircleClose, Edit, VideoPlay, 
  DocumentChecked, Loading, Warning, Finished, InfoFilled, WarningFilled 
} from '@element-plus/icons-vue'


const repairTableRef = ref(null)
const scrapNoticeVisible = ref(false)
// ... 原有变量定义保持 ...

/**
 * 优化 1: 点击行展开/折叠
 */
const handleRowClick = (row) => {
  repairTableRef.value.toggleRowExpansion(row)
}

/**
 * 优化 2: 结果录入逻辑增强
 */
const handleSubmitComplete = async () => {
  if (!completeForm.solution) return ElMessage.warning('请填写维修详细说明')
  
  try {
    const outcome = completeForm.resultStatus === 'scrapped' ? 'fail' : 'success'
    const params = {
      repairId: completeForm.repairId,
      solution: completeForm.solution,
      cost: completeForm.cost,
      outcome: outcome
    }

    await completeRepair(params)
    visible.complete = false
    
    // 🔴 关键修复：提交后必须同时刷新这两个数据，界面才会变
    await fetchDevices() // 刷新设备状态字典
    await fetchList()    // 刷新报修工单列表

    if (outcome === 'fail') {
      scrapNoticeVisible.value = true 
    } else {
      ElMessage.success('设备已修复，状态更新为：在库')
    }
  } catch (e) {
    console.error('提交失败', e)
  }
}

// --- 基础状态 ---
const userRole = ref(localStorage.getItem('role') || 'user')
const userLabId = ref(localStorage.getItem('labId'))
const isAdmin = computed(() => userRole.value.toLowerCase() === 'admin')
const loading = ref(false)
const tableData = ref([])
const rawDeviceList = ref([])
const queryParams = reactive({ keyword: '' })
const visible = reactive({ report: false, complete: false })
const currentOrder = ref(null)

const reportForm = reactive({ deviceId: null, description: '' })
const completeForm = reactive({ repairId: null, solution: '', cost: 0, resultStatus: 'in_stock' })
const reportFormRef = ref(null)

const reportRules = {
  deviceId: [{ required: true, message: '请选择设备', trigger: 'change' }],
  description: [{ required: true, message: '请填写故障描述', trigger: 'blur' }]
}

// --- 计算属性与统计 ---
const statConfig = [
  { label: '新申报', key: 'pending', colorClass: 'c-red', icon: 'Stamp' },
  { label: '已安排', key: 'assigned', colorClass: 'c-blue', icon: 'Checked' },
  { label: '维修中', key: 'process', colorClass: 'c-orange', icon: 'Tools' },
  { label: '已结单', key: 'closed', colorClass: 'c-green', icon: 'CircleCheck' }
]

const stats = computed(() => ({
  pending: tableData.value.filter(i => i.status === 'reported').length,
  assigned: tableData.value.filter(i => i.status === 'assigned').length,
  process: tableData.value.filter(i => i.status === 'in_progress').length,
  closed: tableData.value.filter(i => ['completed', 'rejected'].includes(i.status)).length
}))

const deviceMap = computed(() => {
  const map = new Map(); rawDeviceList.value.forEach(d => map.set(d.id, d)); return map
})

// --- 核心过滤逻辑：哪些设备可以被“报修” ---
const availableDevices = computed(() => {
  // 1. 获取当前正在处理中的报修工单所关联的设备 ID
  // 包含：已申报(reported)、已安排(assigned)、维修中(in_progress)
  const activeRepairDeviceIds = tableData.value
    .filter(order => ['reported', 'assigned', 'in_progress'].includes(order.status))
    .map(order => order.deviceId)
  
  return rawDeviceList.value.filter(device => {
    // A. 实验室权限检查：只有 Admin 能看全校，其他人只能看自己实验室
    const isMyLab = isAdmin.value || String(device.labId) === String(userLabId.value)
    
    // B. 设备状态检查：只有“在库”或“在用”的设备才能发起报修
    // 排除已报废(scrapped)和已经在维修状态(under_repair)的
    const isGoodStatus = !['scrapped', 'under_repair'].includes(device.status)
    
    // C. 流程冲突检查：防止对同一个设备重复发起多笔工单
    const isNotProcessing = !activeRepairDeviceIds.includes(device.id)

    return isMyLab && isGoodStatus && isNotProcessing
  })
})

// --- 数据获取：确保列表只展示本实验室数据 ---
const fetchList = async () => {
  loading.value = true
  try {
    const res = await getRepairList({ 
      keyword: queryParams.keyword, 
      // 🔴 关键：非 Admin 强制传 labId 给后端过滤
      labId: isAdmin.value ? null : userLabId.value 
    })
    tableData.value = res.data || []
  } catch (error) {
    console.error("加载列表失败", error)
  } finally { 
    loading.value = false 
  }
}

const selectedDevice = computed(() => rawDeviceList.value.find(d => d.id === reportForm.deviceId))


const fetchDevices = async () => {
  const res = await getDeviceList(); rawDeviceList.value = res.data || []
}

const openReportDialog = () => { reportForm.deviceId = null; reportForm.description = ''; visible.report = true }

const handleSubmitReport = () => {
  reportFormRef.value.validate(async (valid) => {
    if (valid) { await reportRepair(reportForm); ElMessage.success('申报成功'); visible.report = false; fetchList() }
  })
}

const handleAudit = async (row, status) => {
  await auditRepair({ repairId: row.id, status }); ElMessage.success('操作成功'); fetchList()
}

const handleStartRepair = async (row) => {
  await startRepair(row.id); ElMessage.success('维修开始'); fetchList()
}

const openCompleteDialog = (row) => {
  currentOrder.value = row; completeForm.repairId = row.id; completeForm.solution = ''; completeForm.cost = 0;
  completeForm.resultStatus = 'in_stock'; visible.complete = true
}


const getStatusText = (s) => ({ reported: '已申报', assigned: '已安排', in_progress: '维修中', completed: '维修完成', rejected: '已驳回' }[s] || s)
const getStatusType = (s) => ({ reported: 'danger', assigned: 'primary', in_progress: 'warning', completed: 'success', rejected: 'info' }[s] || 'info')

onMounted(async () => { await fetchDevices(); fetchList() })
</script>

<style scoped lang="scss">
.mb-20 { margin-bottom: 20px; }
.stat-card-v2 {
  background: #fff; border-radius: 8px; display: flex; height: 80px; box-shadow: 0 1px 3px rgba(0,0,0,0.1);
  .icon-side {
    width: 60px; display: flex; align-items: center; justify-content: center; font-size: 24px;
    &.c-red { background: #fff1f0; color: #f5222d; }
    &.c-orange { background: #fff7e6; color: #fa8c16; }
    &.c-blue { background: #e6f7ff; color: #1890ff; }
    &.c-green { background: #f6ffed; color: #52c41a; }
  }
  .content-side { flex: 1; padding: 0 15px; display: flex; flex-direction: column; justify-content: center; .stat-value { font-size: 20px; font-weight: bold; } .stat-label { font-size: 12px; color: #8c8c8c; } }
}
.status-cell { display: flex; flex-direction: column; align-items: center; gap: 6px; }
.result-indicator { font-size: 11px; .text-danger { color: #f56c6c; background: #fef0f0; padding: 2px 5px; border-radius: 4px; border: 1px solid #fab6b6; } .text-success { color: #67c23a; background: #f0f9eb; padding: 2px 5px; border-radius: 4px; border: 1px solid #c2e7b0; } }
.device-preview-card { background: #fafafa; border: 1px dashed #d9d9d9; border-radius: 8px; padding: 15px; margin-bottom: 20px; font-size: 13px; p { margin: 5px 0; span { color: #888; width: 70px; display: inline-block; } } }
.op-done { color: #c0c4cc; font-size: 12px; display: flex; align-items: center; justify-content: center; gap: 4px; }
.op-waiting { color: #909399; font-size: 12px; display: flex; align-items: center; justify-content: center; gap: 4px; }
.expand-detail-container {
  padding: 20px 50px;
  background: #fafafa;
  border-radius: 0 0 8px 8px;
}

.device-cell {
  display: flex;
  flex-direction: column;
  .device-name { font-weight: bold; color: #1890ff; }
  .device-code { font-size: 11px; color: #999; font-family: monospace; }
}

.info-cell-clickable {
  cursor: pointer;
  .brief-desc { 
    color: #333; 
    margin-bottom: 5px; 
    overflow: hidden; text-overflow: ellipsis; white-space: nowrap; 
  }
  .meta-line {
    font-size: 11px; color: #999; display: flex; align-items: center; gap: 8px;
    i { vertical-align: middle; }
  }
  &:hover .brief-desc { color: #1890ff; }
}

.status-cell-v2 {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 5px;
}

.result-indicator-v2 {
  font-size: 11px;
  span {
    padding: 1px 6px;
    border-radius: 4px;
    display: inline-flex;
    align-items: center;
    gap: 3px;
  }
  .tag-scrapped { color: #f56c6c; background: #fff1f0; border: 1px solid #ffa39e; }
  .tag-pending { color: #e6a23c; background: #fff7e6; border: 1px solid #ffe58f; }
  .tag-success { color: #52c41a; background: #f6ffed; border: 1px solid #b7eb8f; }
}

.status-final-text {
  font-size: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  .c-gray { color: #bfbfbf; }
  .c-red { color: #f5222d; font-weight: bold; }
  .c-green { color: #52c41a; font-weight: bold; }
}

.user-view-status {
  color: #999;
  font-size: 12px;
  display: flex;
  flex-direction: column;
  align-items: center;
}

.price-text { color: #cf1322; font-weight: bold; font-size: 14px; }
.desc-text-full { line-height: 1.6; color: #555; }
.text-bold { font-weight: 500; color: #262626; }
</style>