<template>
  <div class="dashboard-container">
    <!-- 1. 欢迎横幅 -->
    <el-card class="welcome-card" shadow="never">
      <div class="welcome-content">
        <div class="avatar-section">
          <el-avatar :size="64" src="https://cube.elemecdn.com/0/88/03b0d39583f48206768a7534e55bcpng.png" />
        </div>
        <div class="text-section">
          <h2 class="greet">早安，{{ username }}，祝你开心每一天！</h2>
          <p class="subtitle">当前角色：<el-tag size="small" effect="dark">{{ roleName }}</el-tag> | 所属实验室：{{ userLabName }}</p>
        </div>
      </div>
    </el-card>

    <!-- 2. 核心数据统计 (实时计算) -->
    <el-row :gutter="20" class="mt-20">
      <el-col :span="6">
        <div class="stat-card bg-gradient-blue">
          <div class="icon-wrapper"><el-icon><Monitor /></el-icon></div>
          <div class="info">
            <div class="label">设备总数</div>
            <div class="num">{{ stats.total }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-gradient-green">
          <div class="icon-wrapper"><el-icon><Box /></el-icon></div>
          <div class="info">
            <div class="label">在库可用</div>
            <div class="num">{{ stats.inStock }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-gradient-orange">
          <div class="icon-wrapper"><el-icon><Tools /></el-icon></div>
          <div class="info">
            <div class="label">维修/报废</div>
            <div class="num">{{ stats.abnormal }}</div>
          </div>
        </div>
      </el-col>
      <el-col :span="6">
        <div class="stat-card bg-gradient-purple">
          <div class="icon-wrapper"><el-icon><Money /></el-icon></div>
          <div class="info">
            <div class="label">资产总值 (元)</div>
            <div class="num">¥ {{ stats.totalPrice }}</div>
          </div>
        </div>
      </el-col>

      <el-col :span="6">
  <div class="stat-card bg-gradient-orange">
    <div class="icon-wrapper"><el-icon><EditPen /></el-icon></div> <!-- 改为领用图标 -->
    <div class="info">
      <div class="label">领用中</div>
      <div class="num">{{ stats.inUse }}</div> <!-- 显示在用数量 -->
    </div>
  </div>
</el-col>

    </el-row>

    <el-row :gutter="20" class="mt-20">
      <!-- 3. 快捷导航 -->
      <el-col :span="16">
        <el-card shadow="hover" class="h-full">
          <template #header>
            <div class="card-header">
              <span><el-icon><Compass /></el-icon> 快捷导航</span>
            </div>
          </template>

          <div class="quick-actions">

            <!-- 查设备：所有人可见 -->
            <div class="action-item" @click="$router.push('/device')">
              <div class="icon-box bg-blue-light">
                <el-icon><Search /></el-icon>
              </div>
              <span>查设备</span>
            </div>

            <!-- 领用设备：所有人可见 -->
            <div class="action-item" @click="$router.push('/usage/manage')">
              <div class="icon-box bg-green-light">
                <el-icon><EditPen /></el-icon>
              </div>
              <span>领用设备</span>
            </div>

            <!-- 去报修：所有人可见 -->
            <div class="action-item" @click="$router.push('/repair/manage')">
              <div class="icon-box bg-purple-light">
                <el-icon><Tools /></el-icon>
              </div>
              <span>去报修</span>
            </div>

            <!-- ===== 管理员专属 ===== -->

            <!-- 管用户 -->
            <div v-if="isAdmin" class="action-item" @click="$router.push('/system/user')">
              <div class="icon-box bg-orange-light">
                <el-icon><User /></el-icon>
              </div>
              <span>管用户</span>
            </div>

            <!-- 管实验室 -->
            <div v-if="isAdmin" class="action-item" @click="$router.push('/system/labs')">
              <div class="icon-box bg-purple-light">
                <el-icon><House /></el-icon>
              </div>
              <span>管实验室</span>
            </div>

          </div>

        </el-card>
      </el-col>

      <!-- 4. 系统公告/状态分布 -->
      <el-col :span="8">
        <el-card shadow="hover" class="h-full">
          <template #header>
            <div class="card-header">
              <span><el-icon><PieChart /></el-icon> 状态分布</span>
            </div>
          </template>
          <div class="status-list">
            <div class="status-item">
              <span>在库 (In Stock)</span>
              <el-progress :percentage="getPercent(stats.inStock)" status="success" />
            </div>
            <div class="status-item">
              <span>在用 (In Use)</span>
              <el-progress :percentage="getPercent(stats.inUse)" />
            </div>
            <div class="status-item">
              <span>维修中 (Repair)</span>
              <el-progress :percentage="getPercent(stats.repair)" status="exception" />
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>

import { ref, onMounted, reactive, computed } from 'vue'
import { getLabList } from '@/api/lab'
import { getDeviceList } from '@/api/device'
import { useUserStore } from '@/stores/user'
// 🔴 确保导入了所有用到的图标
import { 
  Tools, Monitor, Box, Money, Compass, 
  Search, Plus, User, House, PieChart, EditPen 
} from '@element-plus/icons-vue';

const userStore = useUserStore()
const username = ref(localStorage.getItem('username') || 'Admin')

// 🔴 定义 isAdmin 变量
const isAdmin = computed(() => {
  const role = localStorage.getItem('role') || userStore.role
  return role === 'admin' || role === 'ROOT'
})

const roleName = computed(() => {
  const role = localStorage.getItem('role') || 'user'
  const map = { 'admin': '系统管理员', 'manager': '实验室负责人', 'user': '普通用户' }
  return map[role] || role
})

// 实验室名称缓存
const userLabName = ref('加载中...')

// 统计数据
const stats = reactive({
  total: 0,
  inStock: 0,
  inUse: 0,
  repair: 0,
  abnormal: 0, // 维修+报废
  totalPrice: '0.00'
})

// 计算百分比
const getPercent = (val) => {
  if (stats.total === 0) return 0
  return Math.round((val / stats.total) * 100)
}

// 加载数据
const initData = async () => {
  try {
    const res = await getDeviceList({}) 
    const list = res.data || []

    // 计算统计指标
    stats.total = list.length
    stats.inStock = list.filter(i => i.status === 'in_stock').length
    stats.inUse = list.filter(i => i.status === 'in_use').length
    stats.repair = list.filter(i => i.status === 'under_repair').length
    const scrapped = list.filter(i => i.status === 'scrapped').length
    
    stats.abnormal = stats.repair + scrapped

    // 计算总价
    const price = list.reduce((sum, item) => sum + (Number(item.price) || 0), 0)
    stats.totalPrice = price.toLocaleString()

    // 获取实验室名称
    const labId = localStorage.getItem('labId')
    if (labId && labId !== 'null') {
      const labRes = await getLabList()
      const myLab = labRes.data.find(l => l.id == labId)
      userLabName.value = myLab ? myLab.name : '未绑定'
    } else {
      userLabName.value = '全校范围'
    }
  } catch (e) {
    console.error('加载统计数据失败', e)
  }
}

onMounted(() => {
  initData()
})
</script>

<style scoped lang="scss">
.dashboard-container {
  padding: 20px;
  background-color: #f0f2f5;
  min-height: 100vh;
}

.mt-20 { margin-top: 20px; }
.h-full { height: 100%; }

/* 1. 欢迎卡片 */
.welcome-card {
  border: none;
  background: #fff;
  .welcome-content {
    display: flex;
    align-items: center;
    gap: 20px;
    
    .text-section {
      .greet { font-size: 20px; margin-bottom: 10px; color: #303133; }
      .subtitle { color: #909399; font-size: 14px; }
    }
  }
}

/* 2. 统计卡片 (渐变色风格) */
.stat-card {
  border-radius: 8px;
  padding: 25px 20px;
  display: flex;
  align-items: center;
  color: #fff;
  box-shadow: 0 4px 12px rgba(0,0,0,0.1);
  transition: transform 0.3s;
  cursor: default;

  &:hover { transform: translateY(-5px); }

  .icon-wrapper {
    font-size: 48px;
    opacity: 0.8;
    margin-right: 15px;
  }

  .info {
    .label { font-size: 14px; opacity: 0.9; margin-bottom: 5px; }
    .num { font-size: 28px; font-weight: bold; }
  }

  /* 渐变背景 */
  &.bg-gradient-blue { background: linear-gradient(135deg, #36d1dc, #5b86e5); }
  &.bg-gradient-green { background: linear-gradient(135deg, #43e97b, #38f9d7); }
  &.bg-gradient-orange { background: linear-gradient(135deg, #ff9966, #ff5e62); }
  &.bg-gradient-purple { background: linear-gradient(135deg, #667eea, #764ba2); }
}

/* 3. 快捷导航 */
.quick-actions {
  display: flex;
  gap: 30px;
  padding: 20px 0;
  
  .action-item {
    display: flex;
    flex-direction: column;
    align-items: center;
    cursor: pointer;
    transition: all 0.3s;

    &:hover {
      transform: scale(1.05);
      .icon-box { box-shadow: 0 4px 12px rgba(0,0,0,0.15); }
    }

    .icon-box {
      width: 60px;
      height: 60px;
      border-radius: 16px;
      display: flex;
      justify-content: center;
      align-items: center;
      font-size: 28px;
      margin-bottom: 10px;
    }
    
    span { font-size: 14px; color: #606266; }

    .bg-blue-light { background: #ecf5ff; color: #409eff; }
    .bg-green-light { background: #f0f9eb; color: #67c23a; }
    .bg-orange-light { background: #fdf6ec; color: #e6a23c; }
    .bg-purple-light { background: #f4f4f5; color: #909399; }
  }
}

/* 4. 状态分布 */
.status-list {
  padding: 10px 0;
  .status-item {
    margin-bottom: 15px;
    span { display: block; margin-bottom: 5px; font-size: 13px; color: #606266; }
  }
}

.card-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  .el-icon { margin-right: 6px; }
}
</style>