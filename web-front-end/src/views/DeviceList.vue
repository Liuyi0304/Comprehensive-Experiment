<template>
  <div class="app-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>设备列表</span>
          <div>
            <el-button type="success" icon="Plus" @click="openAddDialog">新增设备</el-button>
            <el-button type="warning" @click="$router.push('/lab')">实验室管理</el-button>
            <el-button type="info" @click="$router.push('/user')">用户管理</el-button>
            <el-button type="primary" @click="fetchData">刷新列表</el-button>
          </div>
        </div>
      </template>

      <el-table :data="tableData" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" align="center" />
        <el-table-column prop="assetNumber" label="资产编号" width="120" />
        <el-table-column prop="name" label="设备名称" min-width="120" />
        <el-table-column prop="model" label="型号" width="100" />
        
        <el-table-column prop="status" label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getStatusStyle(row.status)">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        
        <el-table-column prop="location" label="存放位置" width="150" />
        
        <!-- 操作列：根据状态动态显示按钮 -->
        <el-table-column label="操作" width="300" fixed="right">
          <template #default="{ row }">
            <!-- 1. 在库状态：可借用、可调拨、可报修 -->
            <template v-if="row.status === 'in_stock'">
              <el-button link type="primary" @click="openBorrow(row)">借用</el-button>
              <el-button link type="warning" @click="openTransfer(row)">调拨</el-button>
              <el-button link type="danger" @click="openRepair(row)">报修</el-button>
            </template>

            <!-- 2. 在用状态：可归还、可报修 -->
            <template v-if="row.status === 'in_use'">
              <el-button link type="success" @click="handleReturn(row)">归还</el-button>
              <el-button link type="danger" @click="openRepair(row)">报修</el-button>
            </template>

            <!-- 3. 维修状态：可维修完成 -->
            <template v-if="row.status === 'under_repair'">
              <el-button link type="success" @click="openCompleteRepair(row)">维修完成</el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    <!-- ====== 新增设备弹窗 (优化版) ====== -->
    <el-dialog v-model="dialog.add" title="新增设备" width="500px">
      <el-form :model="formAdd" label-width="100px">
        <el-form-item label="资产编号" required>
          <el-input v-model="formAdd.assetNumber" placeholder="例如：DEV-2023001" />
        </el-form-item>
        <el-form-item label="设备名称" required>
          <el-input v-model="formAdd.name" placeholder="请输入设备名称" />
        </el-form-item>
        <el-form-item label="设备型号">
          <el-input v-model="formAdd.model" placeholder="请输入型号" />
        </el-form-item>
        
        <!-- 补全信息 -->
        <el-form-item label="生产商">
          <el-input v-model="formAdd.manufacturer" placeholder="请输入厂家" />
        </el-form-item>
        <el-form-item label="采购价格">
          <el-input-number v-model="formAdd.price" :precision="2" :step="100" style="width: 100%" />
        </el-form-item>
        <el-form-item label="购买日期">
          <el-date-picker 
            v-model="formAdd.purchaseDate" 
            type="date" 
            placeholder="选择日期" 
            value-format="YYYY-MM-DD"
            style="width: 100%"
          />
        </el-form-item>

        <el-form-item label="存放位置">
          <el-input v-model="formAdd.created_at" placeholder="例如：实验室A-301" />
        </el-form-item>
        
        <!-- 优化：下拉选择实验室 -->
        <el-form-item label="所属实验室" required>
          <el-select v-model="formAdd.labId" placeholder="请选择实验室" style="width: 100%">
            <el-option 
              v-for="item in labList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.add = false">取消</el-button>
        <el-button type="primary" @click="submitAdd">确定</el-button>
      </template>
    </el-dialog>

    <!-- 借用弹窗 -->
    <el-dialog v-model="dialog.borrow" title="申请借用" width="400px">
      <el-form :model="formBorrow" label-width="80px">
        <el-form-item label="用途">
          <el-input type="textarea" v-model="formBorrow.purpose" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.borrow = false">取消</el-button>
        <el-button type="primary" @click="submitBorrow">确定</el-button>
      </template>
    </el-dialog>

    <!-- 调拨弹窗 -->
<!-- 调拨弹窗 -->
    <el-dialog v-model="dialog.transfer" title="设备调拨" width="450px">
      <el-form :model="formTransfer" label-width="100px">
        
        <!-- 改成下拉框选择实验室 -->
        <el-form-item label="目标实验室" required>
          <el-select 
            v-model="formTransfer.toLabId" 
            placeholder="请选择目标实验室" 
            style="width: 100%"
          >
            <el-option 
              v-for="item in labList" 
              :key="item.id" 
              :label="item.name" 
              :value="item.id" 
            />
          </el-select>
        </el-form-item>
        
        <el-form-item label="调拨原因">
          <el-input 
            type="textarea" 
            v-model="formTransfer.reason" 
            placeholder="请输入调拨原因（选填）" 
          />
        </el-form-item>
        
      </el-form>
      <template #footer>
        <el-button @click="dialog.transfer = false">取消</el-button>
        <el-button type="primary" @click="submitTransfer">确定</el-button>
      </template>
    </el-dialog>

    <!-- 报修弹窗 -->
    <el-dialog v-model="dialog.repair" title="设备报修" width="400px">
      <el-form :model="formRepair" label-width="80px">
        <el-form-item label="故障描述">
          <el-input type="textarea" v-model="formRepair.description" rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.repair = false">取消</el-button>
        <el-button type="primary" @click="submitRepair">确定</el-button>
      </template>
    </el-dialog>
     <!-- ====== 新增：维修完成弹窗 ====== -->
    <el-dialog v-model="dialog.complete" title="维修完成登记" width="450px">
      <el-form :model="formComplete" label-width="100px">
        <el-form-item label="处理方案">
          <el-input type="textarea" v-model="formComplete.solution" placeholder="例如：更换了电源适配器" />
        </el-form-item>
        <el-form-item label="维修费用">
          <el-input-number v-model="formComplete.cost" :precision="2" :min="0" style="width: 100%" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialog.complete = false">取消</el-button>
        <el-button type="primary" @click="submitCompleteRepair">确定</el-button>
      </template>
    </el-dialog>
    
    <!-- 这里为了完整性，把之前的弹窗代码补上，防止你复制出错 -->
    <!-- 新增设备弹窗 -->
    <el-dialog v-model="dialog.add" title="新增设备" width="500px">
      <el-form :model="formAdd" label-width="100px">
        <el-form-item label="资产编号" required><el-input v-model="formAdd.assetNumber"/></el-form-item>
        <el-form-item label="设备名称" required><el-input v-model="formAdd.name"/></el-form-item>
        <el-form-item label="设备型号"><el-input v-model="formAdd.model"/></el-form-item>
        <el-form-item label="生产商"><el-input v-model="formAdd.manufacturer"/></el-form-item>
        <el-form-item label="采购价格"><el-input-number v-model="formAdd.price" style="width:100%"/></el-form-item>
        <el-form-item label="购买日期"><el-date-picker v-model="formAdd.purchaseDate" style="width:100%" value-format="YYYY-MM-DD"/></el-form-item>
        <el-form-item label="存放位置"><el-input v-model="formAdd.location"/></el-form-item>
        <el-form-item label="所属实验室" required>
          <el-select v-model="formAdd.labId" style="width:100%"><el-option v-for="i in labList" :key="i.id" :label="i.name" :value="i.id"/></el-select>
        </el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog.add=false">取消</el-button><el-button type="primary" @click="submitAdd">确定</el-button></template>
    </el-dialog>

    <!-- 借用、调拨、报修 弹窗代码同上，此处省略以节省篇幅，请保留你原来的代码 -->
    <el-dialog v-model="dialog.borrow" title="申请借用" width="400px">
      <el-form :model="formBorrow" label-width="80px">
        <el-form-item label="用途"><el-input type="textarea" v-model="formBorrow.purpose"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog.borrow=false">取消</el-button><el-button type="primary" @click="submitBorrow">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialog.transfer" title="设备调拨" width="450px">
      <el-form :model="formTransfer" label-width="100px">
        <el-form-item label="目标实验室" required><el-select v-model="formTransfer.toLabId" style="width:100%"><el-option v-for="i in labList" :key="i.id" :label="i.name" :value="i.id"/></el-select></el-form-item>
        <el-form-item label="调拨原因"><el-input type="textarea" v-model="formTransfer.reason"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog.transfer=false">取消</el-button><el-button type="primary" @click="submitTransfer">确定</el-button></template>
    </el-dialog>

    <el-dialog v-model="dialog.repair" title="设备报修" width="400px">
      <el-form :model="formRepair" label-width="80px">
        <el-form-item label="故障描述"><el-input type="textarea" v-model="formRepair.description"/></el-form-item>
      </el-form>
      <template #footer><el-button @click="dialog.repair=false">取消</el-button><el-button type="primary" @click="submitRepair">确定</el-button></template>
    </el-dialog>

  </div>
</template>

<script setup>
import { ref, onMounted, reactive } from 'vue'
// 引入 completeRepair 接口 (如果api里没有，请去加上)
import { 
  getDeviceList, borrowDevice, returnDevice, transferDevice, reportRepair, addDevice, getLabList, completeRepair
} from '@/api/lab'
import { ElMessage, ElMessageBox } from 'element-plus'

const loading = ref(false)
const tableData = ref([])
const labList = ref([]) 
const currentUserId = Number(localStorage.getItem('userId')) || 1 

// 弹窗控制 (加了一个 complete)
const dialog = reactive({
  add: false, borrow: false, transfer: false, repair: false, complete: false
})

// 表单数据
const formAdd = reactive({ assetNumber: '', name: '', model: '', location: '', labId: null, manufacturer: '', price: 0, purchaseDate: '' })
const formBorrow = reactive({ deviceId: null, purpose: '' })
const formTransfer = reactive({ deviceId: null, toLabId: null, reason: '' })
const formRepair = reactive({ deviceId: null, description: '' })
// 新增维修完成表单
const formComplete = reactive({ repairId: null, solution: '', cost: 0 })

const statusDict = {
  'in_stock': { text: '在库', type: 'success' },
  'in_use': { text: '在用', type: 'warning' },
  'under_repair': { text: '维修中', type: 'danger' },
  'scrapped': { text: '已报废', type: 'info' }
}
const getStatusText = (s) => statusDict[s]?.text || s
const getStatusStyle = (s) => statusDict[s]?.type || 'info'

const fetchData = async () => {
  loading.value = true
  try { tableData.value = await getDeviceList() } 
  catch (e) { console.error(e) } 
  finally { loading.value = false }
}
const fetchLabs = async () => { try { labList.value = await getLabList() } catch (e){} }

// ... (其他 submit 方法保持不变) ...
const openAddDialog = () => { Object.assign(formAdd, { assetNumber: '', name: '', model: '', location: '', labId: null, manufacturer: '', price: 0, purchaseDate: '' }); if (labList.value.length === 0) fetchLabs(); dialog.add = true }
const submitAdd = async () => { try { await addDevice(formAdd); ElMessage.success('添加成功'); dialog.add=false; fetchData() } catch (e) {} }
const openBorrow = (row) => { formBorrow.deviceId = row.id; formBorrow.purpose = ''; dialog.borrow = true }
const submitBorrow = async () => { try { await borrowDevice({ deviceId: formBorrow.deviceId, userId: currentUserId, purpose: formBorrow.purpose }); ElMessage.success('借用成功'); dialog.borrow = false; fetchData() } catch (e) {} }
const handleReturn = (row) => { ElMessageBox.confirm(`确认归还?`, '提示').then(async () => { await returnDevice(row.id); ElMessage.success('归还成功'); fetchData() }) }
const openTransfer = (row) => { formTransfer.deviceId = row.id; formTransfer.toLabId = null; formTransfer.reason = ''; if (labList.value.length === 0) fetchLabs(); dialog.transfer = true }
const submitTransfer = async () => { try { await transferDevice({ deviceId: formTransfer.deviceId, toLabId: formTransfer.toLabId, operatorId: currentUserId, reason: formTransfer.reason }); ElMessage.success('调拨成功'); dialog.transfer = false; fetchData() } catch (e) {} }
const openRepair = (row) => { formRepair.deviceId = row.id; formRepair.description = ''; dialog.repair = true }
const submitRepair = async () => { try { await reportRepair({ deviceId: formRepair.deviceId, reporterId: currentUserId, description: formRepair.description }); ElMessage.success('报修成功'); dialog.repair = false; fetchData() } catch (e) {} }

// === 新增：维修完成逻辑 ===
// 注意：这里有个逻辑坑，row.id 是设备ID，但 completeRepair 通常需要 维修记录ID(repairId)。
// 简单做法：我们先通过设备ID查到正在进行的维修记录，或者简化接口只传设备ID。
// 这里假设后端 completeRepair 接口做了适配，可以通过 deviceId 找到最近一条 repair record。
// 如果后端必须传 repairId，那前端列表里得有这个字段，或者点进去详情页处理。
// 为了简化，我们假设 openCompleteRepair 接收的是设备 row
const openCompleteRepair = (row) => {
  // 这里暂时存 deviceId，后端逻辑需要支持通过 deviceId 找到 repairId
  // 或者你得在列表接口里把 currentRepairId 返回给前端
  formComplete.repairId = row.id; // 这里可能有问题，取决于后端实现！
  formComplete.solution = '';
  formComplete.cost = 0;
  dialog.complete = true;
}

const submitCompleteRepair = async () => {
  try {
    // 假设后端有一个 /api/repair/completeByDevice 接口
    // 或者我们直接调用 completeRepair，但参数传 deviceId
    await completeRepair({ 
      deviceId: formComplete.repairId, // 暂时用 deviceId
      solution: formComplete.solution,
      cost: formComplete.cost
    })
    ElMessage.success('维修完成');
    dialog.complete = false;
    fetchData();
  } catch (e) {}
}

onMounted(() => { fetchData(); fetchLabs() })
</script>