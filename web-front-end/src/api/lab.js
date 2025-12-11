// src/api/lab.js
import request from '@/utils/request'

// --- Auth ---
export function login(data) {
  return request({
    url: '/auth/login',
    method: 'post',
    data // 对应 @RequestBody LoginDTO
  })
}

// --- Device ---
export function getDeviceList() {
  return request({
    url: '/device/list',
    method: 'get'
  })
}

export function transferDevice(data) {
  return request({
    url: '/device/transfer',
    method: 'post',
    data // 对应 @RequestBody DeviceTransferDTO
  })
}

export function borrowDevice(params) {
  return request({
    url: '/device/borrow',
    method: 'post',
    params // 对应 @RequestParam (deviceId, userId, purpose)
  })
}

export function returnDevice(deviceId) {
  return request({
    url: '/device/return',
    method: 'post',
    params: { deviceId } // 对应 @RequestParam
  })
}


export function addDevice(data) {
  return request({
    url: '/device/add',
    method: 'post',
    data // 对应后端 @RequestBody DeviceAddDTO
  })
}
// --- Repair ---
export function reportRepair(data) {
  return request({
    url: '/repair/report',
    method: 'post',
    data // 对应 @RequestBody RepairReportDTO
  })
}

export function completeRepair(params) {
  return request({
    url: '/repair/complete',
    method: 'post',
    params // 对应 @RequestParam (repairId, solution, cost)
  })
}


// ================= 用户管理接口 =================

// 1. 获取用户列表 (支持模糊搜索)
export function getUserList(keyword) {
  return request({
    url: '/user/list',
    method: 'get',
    params: { keyword } // 对应后端 @RequestParam String keyword
  })
}

// 2. 新增用户
export function addUser(data) {
  return request({
    url: '/user/add',
    method: 'post',
    data // 对应后端 @RequestBody UserAddDTO
  })
}

// 3. 修改用户信息
export function updateUser(data) {
  return request({
    url: '/user/update',
    method: 'put', // 注意后端用的是 @PutMapping
    data // 对应后端 @RequestBody UserUpdateDTO
  })
}

// 4. 删除用户
export function deleteUser(id) {
  return request({
    url: `/user/delete/${id}`,
    method: 'delete' // 注意后端用的是 @DeleteMapping
  })
}

// 5. 重置密码
export function resetPassword(id) {
  return request({
    url: `/user/reset-pwd/${id}`,
    method: 'post'
  })
}

// ================= 实验室管理接口 =================

// 获取实验室列表
export function getLabList() {
  return request({
    url: '/lab/list',
    method: 'get'
  })
}

// 新增实验室
export function addLab(data) {
  return request({
    url: '/lab/add',
    method: 'post',
    data // { name, location, managerId }
  })
}