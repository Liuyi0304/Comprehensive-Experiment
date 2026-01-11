<template>
  <div class="app-container">
    <!-- ================== 1. 顶部统计卡片 (自动计算) ================== -->
    <el-row :gutter="20" class="mb-4">
      <el-col :span="6">
        <div class="stat-card bg-blue">
          <div class="stat-icon"><el-icon><Monitor /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.total }}</div>
            <div class="stat-label">资产总数</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-green">
          <div class="stat-icon"><el-icon><Box /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.inStock }}</div>
            <div class="stat-label">在库设备</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-orange">
          <div class="stat-icon"><el-icon><Tools /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">{{ stats.underRepair }}</div>
            <div class="stat-label">维修中</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-purple">
          <div class="stat-icon"><el-icon><Money /></el-icon></div>
          <div class="stat-info">
            <div class="stat-value">¥{{ stats.totalPrice }}</div>
            <div class="stat-label">资产总值</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <!-- ================== 2. 主内容区域 ================== -->
    <el-card class="main-card" shadow="never">
      <!-- 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <h3 class="page-title">
            {{ isAdmin ? '全校设备资产库' : '我的实验室设备' }}
          </h3>
        </div>
        
        <div class="toolbar-right">
          <!-- 搜索框 -->
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索设备名称/编号" 
            prefix-icon="Search" 
            style="width: 220px;" 
            clearable 
          />

          <!-- 实验室筛选 (仅管理员可见) -->
          <el-select 
            v-if="isAdmin" 
            v-model="queryParams.labId" 
            placeholder="所有实验室" 
            clearable 
            @change="loadData" 
            style="width: 160px;"
          >
            <el-option v-for="lab in labList" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>

          <div class="button-group">
            <!-- 🟢 申请采购 (非管理员可见) -->
            <el-button 
              v-if="!isAdmin" 
              type="success" 
              icon="ShoppingCart" 
              @click="openPurchaseDialog"
              :disabled="!userStore.labId"
            >
              申请采购
            </el-button>

            <!-- 🔵 直接入库 (仅管理员可见) -->
            <el-button 
              v-if="isAdmin" 
              type="primary" 
              icon="Plus" 
              @click="openAddDialog"
            >
              直接入库
            </el-button>
            
            <el-button icon="Refresh" circle @click="loadData" title="刷新列表" />
          </div>
        </div>
      </div>

      <!-- 表格区域 -->
      <el-table 
        :data="pagedData" 
        v-loading="loading" 
        border 
        stripe 
        style="width: 100%; margin-top: 15px;"
      >
        <el-table-column prop="assetNumber" label="资产编号" width="160" sortable fixed="left" />
        <el-table-column prop="name" label="设备名称" min-width="160" show-overflow-tooltip />
        
        <el-table-column label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>

        <el-table-column v-if="isAdmin" label="所属实验室" width="150" show-overflow-tooltip>
          <template #default="{ row }">
            <el-icon style="vertical-align: middle"><Location /></el-icon> 
            {{ getLabName(row.labId) }}
          </template>
        </el-table-column>

        <el-table-column prop="price" label="单价" align="right" width="120">
          <template #default="{ row }">¥ {{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" effect="dark" size="small">
              {{ formatStatus(row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        
        <!-- 操作列 -->
        <el-table-column label="操作" width="220" fixed="right" align="center">
          <template #default="{ row }">
            <el-button link type="primary" size="small" icon="View" @click="openDetail(row)">详情</el-button>
            
            <!-- 管理员操作 -->
            <template v-if="isAdmin">
              <el-divider direction="vertical" />
              <el-button link type="warning" size="small" icon="Switch" @click="openTransferDialog(row)">调拨</el-button>
              <el-divider direction="vertical" />
              <el-button link type="danger" size="small" icon="Delete" @click="openScrapDialog(row)">报废</el-button>
            </template>

            <!-- 普通用户操作 -->
            <template v-else>
               <el-divider direction="vertical" />
               <!-- 只有在库或在用的设备可以申请 -->
               <el-button 
                 link type="danger" size="small" 
                 @click="openScrapDialog(row)"
                 :disabled="!['in_stock', 'in_use'].includes(row.status)"
               >申请报废</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 (前端伪分页) -->
      <div class="pagination-container">
        <el-pagination 
          v-model:current-page="currentPage" 
          v-model:page-size="pageSize" 
          :total="filteredData.length" 
          layout="total, prev, pager, next" 
          @current-change="handleCurrentChange" 
        />
      </div>
    </el-card>

    <!-- ================== A. 采购申请弹窗 (用户用) ================== -->
    <el-dialog title="发起设备采购申请" v-model="visible.purchase" width="550px" @close="resetForm('purchaseFormRef')">
      <el-alert title="采购流程" type="info" :closable="false" show-icon style="margin-bottom: 15px;">
        填写申请 -> 提交 -> 管理员审核 -> 审核通过自动入库
      </el-alert>
      <el-form :model="purchaseForm" ref="purchaseFormRef" :rules="purchaseRules" label-width="100px">
        <el-form-item label="设备名称" prop="deviceName">
          <el-input v-model="purchaseForm.deviceName" placeholder="请输入计划采购的设备名称" />
        </el-form-item>
        
        <el-row>
          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-select v-model="purchaseForm.categoryId" placeholder="选择分类" style="width:100%">
                 <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="采购数量" prop="number">
              <el-input-number v-model="purchaseForm.number" :min="1" :max="50" style="width:100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row>
          <el-col :span="12">
            <el-form-item label="规格型号" prop="model">
              <el-input v-model="purchaseForm.model" placeholder="选填" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生产厂商" prop="manufacturer">
              <el-input v-model="purchaseForm.manufacturer" placeholder="选填" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="预估单价" prop="onePrice">
          <el-input-number 
            v-model="purchaseForm.onePrice" 
            :precision="2" :min="0" :step="100" 
            style="width: 100%" placeholder="请输入单价"
          />
        </el-form-item>

        <el-form-item label="申请理由" prop="reason">
          <el-input type="textarea" v-model="purchaseForm.reason" rows="3" placeholder="请说明采购用途..." />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible.purchase = false">取消</el-button>
        <el-button type="success" :loading="btnLoading" @click="handleSubmitPurchase">提交申请</el-button>
      </template>
    </el-dialog>

    <!-- ================== B. 直接入库弹窗 (管理员用) ================== -->
    <el-dialog title="新设备入库录入" v-model="visible.add" width="600px" @close="resetForm('addFormRef')">
      <el-form :model="addForm" ref="addFormRef" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24">
            <el-form-item label="设备名称" prop="name"><el-input v-model="addForm.name" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="资产编号" prop="assetNumber"><el-input v-model="addForm.assetNumber" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-select v-model="addForm.categoryId" style="width:100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="规格型号"><el-input v-model="addForm.model" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="生产厂商"><el-input v-model="addForm.manufacturer" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="单价 (元)"><el-input-number v-model="addForm.price" :precision="2" style="width:100%" /></el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属实验室" prop="labId">
              <el-select v-model="addForm.labId" style="width:100%">
                <el-option v-for="lab in labList" :key="lab.id" :label="lab.name" :value="lab.id" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="visible.add = false">取消</el-button>
        <el-button type="primary" :loading="btnLoading" @click="handleSubmitAdd">确认入库</el-button>
      </template>
    </el-dialog>

    <!-- ================== C. 调拨弹窗 ================== -->
    <el-dialog title="资产调拨" v-model="visible.transfer" width="450px" destroy-on-close>
      <div class="dialog-info-box">
        <p>当前设备：<strong>{{ currentDevice?.name }}</strong></p>
        <p>当前位置：{{ getLabName(currentDevice?.labId) }}</p>
      </div>
      <el-form :model="transferForm" label-width="90px">
        <el-form-item label="目标实验室" required>
          <el-select v-model="transferForm.toLabId" placeholder="选择新实验室" style="width: 100%">
            <el-option v-for="lab in labList" :key="lab.id" :label="lab.name" :value="lab.id" :disabled="lab.id === currentDevice?.labId" />
          </el-select>
        </el-form-item>
        <el-form-item label="调拨原因"><el-input type="textarea" v-model="transferForm.reason" rows="3" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="visible.transfer = false">取消</el-button>
        <el-button type="warning" :loading="btnLoading" @click="handleSubmitTransfer">执行调拨</el-button>
      </template>
    </el-dialog>

    <!-- ================== D. 报废弹窗 (智能判断角色) ================== -->
    <el-dialog 
      :title="isAdmin ? '管理员直接报废' : '提交报废申请'" 
      v-model="visible.scrap" 
      width="450px"
    >
      <div v-if="isAdmin">
         <el-alert title="高危操作" type="error" :closable="false" show-icon style="margin-bottom: 15px;">
           作为管理员，您正在执行<strong>直接报废</strong>操作。该操作无需审批，设备将立即归档。
         </el-alert>
         <div class="dialog-info-box">
           确认报废设备：<strong>{{ currentDevice?.name }}</strong> ？
         </div>
      </div>
      <div v-else>
        <el-alert title="流程说明" type="info" :closable="false" show-icon style="margin-bottom: 15px;">
          报废申请提交后需要管理员审核，通过后方可报废。
        </el-alert>
        <el-form :model="scrapForm" label-width="80px">
          <el-form-item label="报废原因" required>
            <el-input type="textarea" v-model="scrapForm.reason" rows="4" placeholder="请详细描述故障情况..." />
          </el-form-item>
        </el-form>
      </div>
      <template #footer>
        <el-button @click="visible.scrap = false">取消</el-button>
        <el-button type="danger" :loading="btnLoading" @click="handleSubmitScrap">
          {{ isAdmin ? '确认直接报废' : '提交申请' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- ================== E. 详情弹窗 ================== -->
    <el-dialog title="设备档案详情" v-model="visible.detail" width="650px">
      <el-descriptions border :column="2" v-if="currentDevice">
        <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentDevice.assetNumber }}</el-descriptions-item>
        <el-descriptions-item label="状态"><el-tag :type="getStatusType(currentDevice.status)">{{ formatStatus(currentDevice.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ Number(currentDevice.price).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="所属实验室" :span="2">{{ getLabName(currentDevice.labId) }}</el-descriptions-item>
        <el-descriptions-item label="规格型号">{{ currentDevice.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="生产厂商">{{ currentDevice.manufacturer || '-' }}</el-descriptions-item>
        <el-descriptions-item label="录入时间" :span="2">{{ currentDevice.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="visible.detail = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Search, Plus, Refresh, Monitor, Location, View, 
  Switch, Delete, Tools, Box, Money, ShoppingCart 
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user' 
import { 
  getDeviceList, addDevice, transferDevice, 
  submitScrapRequest, adminDirectScrap, 
  getLabList, getCategoryList,
  submitPurchase // 确保 api/device.js 或 api/purchase.js 中导出了此方法
} from '@/api/device'

const userStore = useUserStore()
const isAdmin = computed(() => ['admin', 'ROOT'].includes(userStore.role))

// 状态定义
const loading = ref(false)
const btnLoading = ref(false)
const rawTableData = ref([]) 
const searchKeyword = ref('') 
const labList = ref([]) 
const categoryList = ref([]) 
const currentDevice = ref(null)

const visible = reactive({ add: false, transfer: false, scrap: false, detail: false, purchase: false })

// 分页与查询
const currentPage = ref(1)
const pageSize = ref(10)
const queryParams = reactive({ labId: null })

// --- 表单数据 ---
const addForm = reactive({ name: '', assetNumber: '', categoryId: null, labId: null, price: 0, model: '', manufacturer: '' })
const transferForm = reactive({ deviceId: null, toLabId: null, reason: '', operatorId: null })
const scrapForm = reactive({ deviceId: null, reason: '' })
const purchaseForm = reactive({ deviceName: '', categoryId: null, model: '', manufacturer: '', number: 1, onePrice: 0, reason: '' })

// --- 校验规则 ---
const rules = {
  name: [{ required: true, message: '必填', trigger: 'blur' }],
  assetNumber: [{ required: true, message: '必填', trigger: 'blur' }],
  labId: [{ required: true, message: '必选', trigger: 'change' }],
  categoryId: [{ required: true, message: '必选', trigger: 'change' }]
}
const purchaseRules = {
  deviceName: [{ required: true, message: '请输入名称', trigger: 'blur' }],
  categoryId: [{ required: true, message: '请选择分类', trigger: 'change' }],
  number: [{ required: true, message: '至少1台', trigger: 'blur' }],
  reason: [{ required: true, message: '请填写申请理由', trigger: 'blur' }]
}

const addFormRef = ref(null)
const purchaseFormRef = ref(null)

// ================== 生命周期 ==================
onMounted(async () => {
  await loadDicts()
  // 非管理员自动锁定实验室
  if (!isAdmin.value) {
    queryParams.labId = userStore.labId
  }
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    // 安全策略：如果不是管理员，强制覆盖 labId
    if (!isAdmin.value) params.labId = userStore.labId
    
    const res = await getDeviceList(params)
    rawTableData.value = res.data || [] 
  } catch (error) {
    console.error("加载失败", error)
  } finally { 
    loading.value = false 
  }
}

const loadDicts = async () => {
  try {
    const [labRes, catRes] = await Promise.all([getLabList(), getCategoryList()])
    labList.value = labRes.data || []
    categoryList.value = catRes.data || []
  } catch (e) { console.warn('字典加载失败', e) }
}

// ================== 计算属性 ==================
const filteredData = computed(() => {
  if (!searchKeyword.value) return rawTableData.value
  const kw = searchKeyword.value.toLowerCase()
  return rawTableData.value.filter(i => 
    (i.name && i.name.toLowerCase().includes(kw)) || 
    (i.assetNumber && i.assetNumber.toLowerCase().includes(kw))
  )
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

// 顶部卡片统计数据
const stats = computed(() => {
  const data = filteredData.value
  return {
    total: data.length,
    inStock: data.filter(i => i.status === 'in_stock').length,
    underRepair: data.filter(i => i.status === 'under_repair').length,
    totalPrice: data.reduce((sum, i) => sum + (Number(i.price) || 0), 0).toLocaleString()
  }
})

// ================== 业务逻辑 ==================

// 1. 详情
const openDetail = (row) => { currentDevice.value = row; visible.detail = true }

// 2. 调拨
const openTransferDialog = (row) => {
  currentDevice.value = row
  transferForm.deviceId = row.id
  transferForm.toLabId = null
  transferForm.reason = ''
  transferForm.operatorId = userStore.id
  visible.transfer = true
}
const handleSubmitTransfer = async () => {
  if (!transferForm.toLabId) return ElMessage.warning('请选择目标实验室')
  btnLoading.value = true
  try {
    await transferDevice(transferForm)
    ElMessage.success('调拨成功')
    visible.transfer = false
    loadData()
  } finally { btnLoading.value = false }
}

// 3. 报废 (区分管理员和普通用户)
const openScrapDialog = (row) => {
  currentDevice.value = row
  scrapForm.deviceId = row.id
  scrapForm.reason = ''
  visible.scrap = true
}
const handleSubmitScrap = async () => {
  btnLoading.value = true
  try {
    if (isAdmin.value) {
      await adminDirectScrap({ deviceId: scrapForm.deviceId })
      ElMessage.success('设备已直接报废')
    } else {
      if (!scrapForm.reason) return ElMessage.warning('请填写报废原因')
      await submitScrapRequest(scrapForm)
      ElMessage.success('报废申请已提交，等待审核')
    }
    visible.scrap = false
    loadData()
  } catch(e) { console.error(e) } 
  finally { btnLoading.value = false }
}

// 4. 新增设备 (Admin)
const openAddDialog = () => {
  Object.assign(addForm, { name: '', assetNumber: '', categoryId: null, labId: null, price: 0, model: '', manufacturer: '' })
  visible.add = true
}
const handleSubmitAdd = async () => {
  addFormRef.value.validate(async (valid) => {
    if (!valid) return
    btnLoading.value = true
    try {
      await addDevice(addForm)
      ElMessage.success('录入成功')
      visible.add = false
      loadData()
    } finally { btnLoading.value = false }
  })
}

// 5. 采购申请 (User)
const openPurchaseDialog = () => {
  if (!userStore.labId) return ElMessage.error('您未绑定实验室，无法申请')
  Object.assign(purchaseForm, { deviceName: '', categoryId: null, model: '', manufacturer: '', number: 1, onePrice: 0, reason: '' })
  visible.purchase = true
}
const handleSubmitPurchase = async () => {
  purchaseFormRef.value.validate(async (valid) => {
    if (!valid) return
    btnLoading.value = true
    try {
      await submitPurchase(purchaseForm)
      ElMessage.success('采购申请已提交')
      visible.purchase = false
    } catch(e) { console.error(e) }
    finally { btnLoading.value = false }
  })
}

// ================== 工具函数 ==================
const handleCurrentChange = (val) => { currentPage.value = val }
const getCategoryName = (id) => categoryList.value.find(c => c.id === id)?.name || id
const getLabName = (id) => labList.value.find(l => l.id === id)?.name || id
const formatStatus = (s) => ({ 'in_stock': '在库', 'in_use': '在用', 'under_repair': '维修中', 'scrapped': '已报废' }[s] || s)
const getStatusType = (s) => ({ 'in_stock': 'success', 'in_use': 'primary', 'under_repair': 'warning', 'scrapped': 'info' }[s] || 'info')
const resetForm = (name) => {
  if (name === 'addFormRef') addFormRef.value?.resetFields()
  if (name === 'purchaseFormRef') purchaseFormRef.value?.resetFields()
}
</script>

<style scoped lang="scss">
.app-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

/* 顶部统计卡片样式 */
.stat-card {
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  transition: all 0.3s;
  
  &:hover { transform: translateY(-3px); box-shadow: 0 4px 16px rgba(0,0,0,0.1); }
  
  &.bg-blue { border-left: 5px solid #409EFF; .stat-icon { color: #409EFF; background: rgba(64,158,255,0.1); } }
  &.bg-green { border-left: 5px solid #67C23A; .stat-icon { color: #67C23A; background: rgba(103,194,58,0.1); } }
  &.bg-orange { border-left: 5px solid #E6A23C; .stat-icon { color: #E6A23C; background: rgba(230,162,60,0.1); } }
  &.bg-purple { border-left: 5px solid #909399; .stat-icon { color: #909399; background: rgba(144,147,153,0.1); } }

  .stat-icon {
    width: 60px; height: 60px;
    border-radius: 50%;
    display: flex; justify-content: center; align-items: center;
    font-size: 28px;
    margin-right: 15px;
  }

  .stat-info {
    .stat-value { font-size: 24px; font-weight: bold; color: #303133; margin-bottom: 5px; }
    .stat-label { color: #909399; font-size: 14px; }
  }
}

/* 主内容卡片 */
.main-card {
  margin-top: 10px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  border-left: 4px solid #409EFF;
  padding-left: 10px;
  margin: 0;
}

.toolbar-right {
  display: flex;
  gap: 10px;
  align-items: center;
}

.button-group {
  display: flex;
  gap: 10px;
  align-items: center;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-info-box {
  background: #f5f7fa;
  padding: 15px;
  border-radius: 4px;
  margin-bottom: 20px;
  p { margin: 5px 0; color: #606266; }
}
</style>