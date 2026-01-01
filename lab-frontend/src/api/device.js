import request from '@/utils/request'

// ==========================================
// 1. 设备核心管理 (对应 DeviceController)
// ==========================================

// 获取设备列表
export function getDeviceList(params) {
  return request({
    url: '/devices', // 对应后端 @GetMapping("/devices") 或 @GetMapping
    method: 'get',
    params
  })
}

// 新增设备
export function addDevice(data) {
  return request({
    url: '/devices/add', // 对应后端 @PostMapping("/devices/add")
    method: 'post',
    data
  })
}

// 设备调拨
export function transferDevice(data) {
  return request({
    url: '/devices/transfer', // 对应后端 @PostMapping("/devices/transfer")
    method: 'post',
    data
  })
}

// 管理员直接强制报废 (不走申请流程)
export function adminDirectScrap(data) {
  return request({
    url: '/admin/device/scrap', // 对应后端 @PostMapping("/admin/device/scrap")
    method: 'post',
    data
  })
}

// ==========================================
// 2. 辅助数据接口 (对应 LabController, CategoryController)
// ==========================================

// 获取实验室列表
export function getLabList() {
  return request({
    url: '/lab/list', // 对应 LabController @GetMapping("/list")
    method: 'get'
  })
}

// 获取设备分类列表
export function getCategoryList() {
  return request({
    url: '/category/list', // 对应 CategoryController @GetMapping("/list")
    method: 'get'
  })
}

// ==========================================
// 3. 报废申请流程 (对应 ScrapRequestController)
// 恢复了你原有的这部分逻辑
// ==========================================

// [普通用户] 提交报废申请
export function submitScrapRequest(data) {
  return request({
    // 请确认后端 ScrapRequestController 的路径，通常是 /scrap-requests/submit
    url: '/scrap-requests/submit', 
    method: 'post',
    data
  })
}
// [管理员] 驳回申请
export function rejectScrapRequest(data) {
  return request({
    url: '/scrap-requests/reject', 
    method: 'post',
    data
  })
}
// [普通用户] 查看我的报废申请
export function getMyScrapRequests(params) {
  return request({
    url: '/scrap-requests/my', // 假设路径
    method: 'get',
    params
  })
}

// [管理员] 获取待审批列表
export function getPendingScrapRequests(params) {
  return request({
    url: '/scrap-requests/pending', // 假设路径
    method: 'get',
    params
  })
}

// [管理员] 审批通过
export function approveScrapRequest(data) {
  return request({
    url: '/scrap-requests/approve', 
    method: 'post',
    data
  })
}
// src/api/device.js 追加以下内容
// [管理员] 审批采购申请 (通过/驳回)
export function approvePurchase(data) {
  return request({
    url: '/purchase-requests/approve',
    method: 'post',
    data // 格式: { id: 1, status: 'approved' }
  })
}
// 提交采购申请 (对应 PurchaseRequestCreateDTO)
export function submitPurchase(data) {
  return request({
    url: '/purchase-requests/submit', // 确认你的 Controller 路径
    method: 'post',
    data
  })
}
// [管理员] 获取采购申请列表
export function getPurchaseList(params) {
  return request({
    url: '/purchase-requests/list',
    method: 'get',
    params // 可以传 { status: 'pending' } 只看待审批
  })
}

