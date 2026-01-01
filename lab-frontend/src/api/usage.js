import request from '@/utils/request'

/**
 * 获取设备领用记录列表
 * @param {Object} params { keyword: string }
 */
export function getUsageList(params) {
  return request({
    url: '/usage/list',
    method: 'get',
    params
  })
}

/**
 * 登记领用设备 (开始使用)
 * @param {Object} data { deviceId: Long, purpose: String }
 */
export function startUsage(data) {
  return request({
    url: '/usage/start',
    method: 'post',
    data // 对应后端的 @RequestBody
  })
}

/**
 * 归还设备 (结束使用)
 * @param {Long} recordId 领用记录ID
 */
export function endUsage(recordId) {
  return request({
    url: '/usage/end',
    method: 'put',
    params: { recordId } // 对应后端的 @RequestParam
  })
}