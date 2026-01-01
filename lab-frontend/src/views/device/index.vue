<template>
  <div class="app-container">
    <!-- 1. 顶部统计卡片 -->
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
            <div class="stat-value">¥{{ calculateTotalPrice }}</div>
            <div class="stat-label">资产总值</div>
          </div>
        </div>
      </el-col>
    </el-row>

    <el-card class="main-card" shadow="never">
      <!-- 2. 工具栏 -->
      <div class="toolbar">
        <div class="toolbar-left">
          <h3 class="page-title">{{ isAdmin ? '全校资产监控' : '本实验室设备列表' }}</h3>
        </div>
        <div class="toolbar-right" style="display: flex; gap: 10px;">
          <el-input v-model="searchKeyword" placeholder="搜索名称/编号" prefix-icon="Search" style="width: 220px;" clearable />

          <!-- 只有管理员可以筛选实验室 -->
          <el-select v-if="isAdmin" v-model="queryParams.labId" placeholder="所有实验室" clearable @change="loadData" style="width: 180px;">
            <el-option v-for="lab in labList" :key="lab.id" :label="lab.name" :value="lab.id" />
          </el-select>

          <div class="button-group">
            <!-- 只有管理员可以执行新设备入库 -->
            <el-button v-if="isAdmin" type="primary" icon="Plus" @click="openAddDialog">新设备入库</el-button>
            <el-button icon="Refresh" circle @click="loadData" />
          </div>
        </div>
      </div>

      <!-- 3. 表格区域 -->
      <el-table :data="pagedData" v-loading="loading" border stripe style="width: 100%; flex: 1; margin-top: 15px;">
        <el-table-column prop="assetNumber" label="资产编号" width="200" sortable fixed="left" />
        <el-table-column prop="name" label="设备名称" min-width="160" show-overflow-tooltip />
        <el-table-column prop="categoryId" label="分类" width="120">
          <template #default="{ row }">
            <el-tag size="small" type="info" effect="plain">{{ getCategoryName(row.categoryId) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="110" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusType(row.status)" size="small" effect="dark">{{ formatStatus(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column v-if="isAdmin" label="所属实验室" width="180">
          <template #default="{ row }">
            <el-icon><Location /></el-icon> {{ getLabName(row.labId) }}
          </template>
        </el-table-column>
        <el-table-column prop="price" label="单价" align="right" width="120">
          <template #default="{ row }">¥ {{ Number(row.price).toFixed(2) }}</template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right" align="center">
          <template #default="{ row }">
            <div class="action-buttons">
              <el-tooltip content="查看详情" placement="top">
                <el-button link type="primary" icon="View" @click="openDetail(row)"></el-button>
              </el-tooltip>
              
              <!-- 管理员专有的敏感操作 -->
              <template v-if="isAdmin && row.status !== 'scrapped'">
                <el-tooltip :content="row.status === 'under_repair' ? '维修中不可调拨' : '调拨'" placement="top">
                  <span>
                    <el-button link type="warning" icon="Switch" :disabled="row.status === 'under_repair'" @click="openTransferDialog(row)" />
                  </span>
                </el-tooltip>
                <el-tooltip :content="row.status === 'under_repair' ? '维修中不可报废' : '报废'" placement="top">
                  <span>
                    <el-button link type="danger" icon="Delete" :disabled="row.status === 'under_repair'" @click="openScrapDialog(row)" />
                  </span>
                </el-tooltip>
              </template>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container">
        <el-pagination v-model:current-page="currentPage" v-model:page-size="pageSize" :total="filteredData.length" layout="total, prev, pager, next" @current-change="handleCurrentChange" />
      </div>
    </el-card>

    <!-- ================== 弹窗部分 ================== -->

    <!-- A. 新增弹窗 -->
    <el-dialog title="新设备入库录入" v-model="visible.add" width="600px" destroy-on-close>
      <el-form :model="addForm" ref="addFormRef" :rules="rules" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="24"><el-form-item label="设备名称" prop="name"><el-input v-model="addForm.name" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="资产编号" prop="assetNumber"><el-input v-model="addForm.assetNumber" /></el-form-item></el-col>
          <el-col :span="12">
            <el-form-item label="设备分类" prop="categoryId">
              <el-select v-model="addForm.categoryId" style="width:100%">
                <el-option v-for="c in categoryList" :key="c.id" :label="c.name" :value="c.id" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12"><el-form-item label="规格型号"><el-input v-model="addForm.model" /></el-form-item></el-col>
          <el-col :span="12"><el-form-item label="单价 (元)"><el-input-number v-model="addForm.price" :precision="2" style="width:100%" /></el-form-item></el-col>
          <el-col :span="24">
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

    <!-- B. 调拨弹窗 -->
    <el-dialog title="资产调拨" v-model="visible.transfer" width="450px" destroy-on-close>
      <el-form :model="transferForm" label-width="90px">
        <el-form-item label="当前设备"><strong>{{ currentDevice?.name }}</strong></el-form-item>
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

    <!-- C. 报废确认 -->
    <el-dialog title="直接报废确认" v-model="visible.scrap" width="400px">
      <el-alert title="警告" type="error" description="直接报废将立即停用资产并归档，不可撤回。" show-icon :closable="false" />
      <p style="margin-top: 20px; text-align: center;">确定报废：<strong>{{ currentDevice?.name }}</strong>？</p>
      <template #footer>
        <el-button @click="visible.scrap = false">取消</el-button>
        <el-button type="danger" :loading="btnLoading" @click="handleSubmitScrap">确认报废</el-button>
      </template>
    </el-dialog>

    <!-- D. 详情弹窗 -->
    <el-dialog title="设备档案详情" v-model="visible.detail" width="600px">
      <el-descriptions border :column="2" v-if="currentDevice">
        <el-descriptions-item label="设备名称">{{ currentDevice.name }}</el-descriptions-item>
        <el-descriptions-item label="资产编号">{{ currentDevice.assetNumber }}</el-descriptions-item>
        <el-descriptions-item label="当前状态"><el-tag :type="getStatusType(currentDevice.status)">{{ formatStatus(currentDevice.status) }}</el-tag></el-descriptions-item>
        <el-descriptions-item label="单价">¥{{ Number(currentDevice.price).toFixed(2) }}</el-descriptions-item>
        <el-descriptions-item label="所属实验室" :span="2">{{ getLabName(currentDevice.labId) }}</el-descriptions-item>
        <el-descriptions-item label="型号">{{ currentDevice.model || '-' }}</el-descriptions-item>
        <el-descriptions-item label="录入时间">{{ currentDevice.createdAt || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Plus, Refresh, Monitor, Location, View, Switch, Delete, Tools, Box, Money } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user' 
import { getDeviceList, addDevice, transferDevice, adminDirectScrap, getLabList, getCategoryList } from '@/api/device'

const userStore = useUserStore()
// 🔴 严格权限判断：只有 admin 或 ROOT 才是管理员
const isAdmin = computed(() => ['admin', 'ROOT'].includes(userStore.role))

const loading = ref(false)
const btnLoading = ref(false)
const rawTableData = ref([]) 
const searchKeyword = ref('') 
const labList = ref([]) 
const categoryList = ref([]) 
const currentDevice = ref(null)
const visible = reactive({ add: false, transfer: false, scrap: false, detail: false })

const currentPage = ref(1)
const pageSize = ref(10)
const queryParams = reactive({ labId: null })

const addForm = reactive({ name: '', assetNumber: '', categoryId: null, labId: null, price: 0, model: '' })
const transferForm = reactive({ deviceId: null, toLabId: null, reason: '' })
const addFormRef = ref(null)

const rules = {
  name: [{ required: true, message: '必填', trigger: 'blur' }],
  assetNumber: [{ required: true, message: '必填', trigger: 'blur' }],
  labId: [{ required: true, message: '必选', trigger: 'change' }],
  categoryId: [{ required: true, message: '必选', trigger: 'change' }]
}

// --- 初始化与加载 ---
onMounted(async () => {
  await loadDicts()
  // 非管理员强制锁定 labId
  if (!isAdmin.value) {
    queryParams.labId = userStore.labId
  }
  loadData()
})

const loadData = async () => {
  loading.value = true
  try {
    const params = { ...queryParams }
    if (!isAdmin.value) params.labId = userStore.labId
    const res = await getDeviceList(params)
    rawTableData.value = res.data || [] 
  } finally { loading.value = false }
}

const loadDicts = async () => {
  const [labRes, catRes] = await Promise.all([getLabList(), getCategoryList()])
  labList.value = labRes.data || []
  categoryList.value = catRes.data || []
}

// --- 计算属性 ---
const filteredData = computed(() => {
  if (!searchKeyword.value) return rawTableData.value
  const kw = searchKeyword.value.toLowerCase()
  return rawTableData.value.filter(i => i.name?.toLowerCase().includes(kw) || i.assetNumber?.toLowerCase().includes(kw))
})

const pagedData = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

const stats = computed(() => ({
  total: filteredData.value.length,
  inStock: filteredData.value.filter(i => i.status === 'in_stock').length,
  underRepair: filteredData.value.filter(i => i.status === 'under_repair').length
}))

const calculateTotalPrice = computed(() => {
  return filteredData.value.reduce((s, i) => s + (Number(i.price) || 0), 0).toLocaleString()
})

// --- 业务函数 ---
const openDetail = (row) => { currentDevice.value = row; visible.detail = true }
const openAddDialog = () => { Object.assign(addForm, { name: '', assetNumber: '', categoryId: null, labId: null, price: 0 }); visible.add = true }
const openTransferDialog = (row) => { currentDevice.value = row; transferForm.deviceId = row.id; transferForm.toLabId = null; visible.transfer = true }
const openScrapDialog = (row) => { currentDevice.value = row; visible.scrap = true }

const handleSubmitAdd = async () => {
  addFormRef.value.validate(async (valid) => {
    if (!valid) return
    btnLoading.value = true
    try { await addDevice(addForm); ElMessage.success('录入成功'); visible.add = false; loadData() } finally { btnLoading.value = false }
  })
}

const handleSubmitTransfer = async () => {
  if (!transferForm.toLabId) return ElMessage.warning('请选择目标实验室')
  btnLoading.value = true
  try {
    const submitData = { ...transferForm, operatorId: userStore.id || localStorage.getItem('userId') }
    await transferDevice(submitData)
    ElMessage.success('调拨成功'); visible.transfer = false; loadData()
  } finally { btnLoading.value = false }
}

const handleSubmitScrap = async () => {
  btnLoading.value = true
  try { await adminDirectScrap({ deviceId: currentDevice.value.id }); ElMessage.success('已报废'); visible.scrap = false; loadData() } finally { btnLoading.value = false }
}

const handleCurrentChange = (val) => { currentPage.value = val }
const getCategoryName = (id) => categoryList.value.find(c => c.id === id)?.name || id
const getLabName = (id) => labList.value.find(l => l.id === id)?.name || id
const formatStatus = (s) => ({ in_stock: '在库', in_use: '在用', under_repair: '维修中', scrapped: '已报废' }[s] || s)
const getStatusType = (s) => ({ in_stock: 'success', in_use: 'primary', under_repair: 'warning', scrapped: 'info' }[s] || 'info')
const resetForm = (name) => { if (name === 'addFormRef') addFormRef.value?.resetFields() }
</script>

<style scoped lang="scss">
.app-container { padding: 20px; background-color: #f0f2f5; height: 100vh; display: flex; flex-direction: column; }
.main-card { flex: 1; display: flex; flex-direction: column; :deep(.el-card__body) { flex: 1; display: flex; flex-direction: column; padding: 20px; } }
.stat-card { background: #fff; border-radius: 8px; padding: 20px; display: flex; align-items: center; box-shadow: 0 2px 12px 0 rgba(0,0,0,0.05);
  &.bg-blue { border-left: 4px solid #409EFF; .stat-icon { color: #409EFF; } }
  &.bg-green { border-left: 4px solid #67C23A; .stat-icon { color: #67C23A; } }
  &.bg-orange { border-left: 4px solid #E6A23C; .stat-icon { color: #E6A23C; } }
  &.bg-purple { border-left: 4px solid #909399; .stat-icon { color: #909399; } }
  .stat-icon { font-size: 40px; margin-right: 15px; }
  .stat-info { .stat-value { font-size: 24px; font-weight: bold; } .stat-label { color: #909399; font-size: 14px; } }
}
.toolbar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 15px; }
.pagination-container { margin-top: 20px; display: flex; justify-content: flex-end; }
</style>