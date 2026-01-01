import request from '@/utils/request'

// 获取列表
export function getRepairList(params) {
  return request({ url: '/repair/list', method: 'get', params })
}

// 1. 提交报修 (reported)
export function reportRepair(data) {
  return request({ url: '/repair/report', method: 'post', data })
}

// 2. 审批安排 (reported -> assigned / rejected)
export function auditRepair(params) {
  return request({ url: '/repair/audit', method: 'put', params })
}

// 3. 开始维修 (assigned -> in_progress)
export function startRepair(repairId) {
  return request({ 
    url: '/repair/start', 
    method: 'put', 
    params: { repairId } 
  })
}

// 4. 完成维修 (in_progress -> completed)
export function completeRepair(params) {
  return request({
    url: '/repair/complete',
    method: 'post',
    params // 这里会包含 repairId, solution, cost, resultStatus
  })
}