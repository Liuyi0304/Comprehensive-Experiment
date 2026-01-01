import request from '@/utils/request'

// 获取我的报废申请列表
export function getMyScrapRequests() {
  return request({
    url: '/scrap-requests/my',
    method: 'get'
  })
}

// 提交报废申请 (之前可能在 device.js，这里统一一下)
export function createScrapRequest(data) {
  return request({
    url: '/scrap-requests',
    method: 'post',
    data
  })
}


// [管理员] 获取所有报废申请列表
export function getAllScrapRequests(params) {
  return request({
    url: '/scrap-requests/all', // 对应后端 @GetMapping("/all")
    method: 'get',
    params
  })
}

// [管理员] 审批报废申请
export function approveScrapRequest(data) {
  return request({
    url: '/scrap-requests/approve', // 对应后端 @PostMapping("/approve")
    method: 'post',
    data
  })
}