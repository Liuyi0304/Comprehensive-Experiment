import request from '@/utils/request'

// 获取我的采购申请
export function getMyPurchaseRequests() {
  return request({
    url: '/purchase-requests/my', // 对应后端刚写的接口
    method: 'get'
  })
}